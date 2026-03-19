package io.shubham0204.smollmandroid.ui.customapp

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

data class EvaluationResult(
    val uniqueIdx: String,
    val goldAnswer: String,
    val predictedAnswer: String,
    val isPlanCorrect: Boolean,
    val isArgumentsCorrect: Boolean,
    val isCorrect: Boolean,
)

data class EvaluationSummary(
    val latestResult: EvaluationResult? = null,
    val macroAccuracy: Float? = null,
    val evaluatedCount: Int = 0,
    val errorMessage: String? = null,
)

object CustomAppEvaluator {
    private val compactJson = Json { prettyPrint = false }

    fun evaluate(
        parsedPrediction: ParsedToolCall?,
        goldRecords: List<GoldTsvRecord>,
        priorResults: List<EvaluationResult>,
        parseErrorMessage: String?,
        goldRecordHint: GoldTsvRecord? = null,
    ): EvaluationSummary {
        if (goldRecords.isEmpty()) {
            return EvaluationSummary(
                latestResult = null,
                macroAccuracy = computeMacroAccuracy(priorResults),
                evaluatedCount = priorResults.size,
                errorMessage = null,
            )
        }

        if (parseErrorMessage != null) {
            return EvaluationSummary(
                latestResult = null,
                macroAccuracy = computeMacroAccuracy(priorResults),
                evaluatedCount = priorResults.size,
                errorMessage = "Parse error: $parseErrorMessage",
            )
        }

        val prediction =
            parsedPrediction ?: return EvaluationSummary(
                latestResult = null,
                macroAccuracy = computeMacroAccuracy(priorResults),
                evaluatedCount = priorResults.size,
                errorMessage = "No parsed prediction available.",
            )

        val matchingGold =
            goldRecordHint
                ?: return EvaluationSummary(
                    latestResult = null,
                    macroAccuracy = computeMacroAccuracy(priorResults),
                    evaluatedCount = priorResults.size,
                    errorMessage = "No gold record hint available for structural evaluation.",
                )

        val goldToolCall =
            runCatching { CustomAppJsonParser.parse(matchingGold.answer) }.getOrElse { error ->
                return EvaluationSummary(
                    latestResult = null,
                    macroAccuracy = computeMacroAccuracy(priorResults),
                    evaluatedCount = priorResults.size,
                    errorMessage = "Gold TSV answer parse error: ${error.message}",
                )
            }

        val normalizedPrediction = canonicalizeToolCall(prediction)
        val normalizedGold = canonicalizeToolCall(goldToolCall)
        val isPlanCorrect = prediction.plan == goldToolCall.plan
        val isArgumentsCorrect =
            canonicalizeElement(prediction.arguments) == canonicalizeElement(goldToolCall.arguments)

        val latestResult =
            EvaluationResult(
                uniqueIdx = matchingGold.uniqueIdx,
                goldAnswer = compactJson.encodeToString(JsonObject.serializer(), normalizedGold),
                predictedAnswer = compactJson.encodeToString(JsonObject.serializer(), normalizedPrediction),
                isPlanCorrect = isPlanCorrect,
                isArgumentsCorrect = isArgumentsCorrect,
                isCorrect = normalizedPrediction == normalizedGold,
            )

        val updatedResults =
            priorResults.filterNot { it.uniqueIdx == latestResult.uniqueIdx } + latestResult

        return EvaluationSummary(
            latestResult = latestResult,
            macroAccuracy = computeMacroAccuracy(updatedResults),
            evaluatedCount = updatedResults.size,
            errorMessage = null,
        )
    }

    private fun computeMacroAccuracy(results: List<EvaluationResult>): Float? {
        if (results.isEmpty()) return null

        val byGoldAnswer = results.groupBy { it.goldAnswer }
        val perClassAccuracies =
            byGoldAnswer.values.map { classResults ->
                val correctCount = classResults.count { it.isCorrect }
                correctCount.toFloat() / classResults.size.toFloat()
            }
        return perClassAccuracies.average().toFloat()
    }

    private fun canonicalizeToolCall(toolCall: ParsedToolCall): JsonObject =
        buildJsonObject {
            put("arguments", canonicalizeElement(toolCall.arguments))
            put("plan", JsonPrimitive(toolCall.plan))
        }

    private fun canonicalizeElement(element: JsonElement): JsonElement =
        when (element) {
            is JsonObject -> {
                JsonObject(
                    element.entries
                        .sortedBy { it.key }
                        .associate { (key, value) -> key to canonicalizeElement(value) },
                )
            }
            is JsonArray -> JsonArray(element.map(::canonicalizeElement))
            is JsonPrimitive -> element
            JsonNull -> JsonNull
        }
}
