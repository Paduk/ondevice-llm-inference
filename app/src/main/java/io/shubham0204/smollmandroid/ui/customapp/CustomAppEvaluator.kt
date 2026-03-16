package io.shubham0204.smollmandroid.ui.customapp

data class EvaluationResult(
    val uniqueIdx: String,
    val goldAnswer: String,
    val predictedAnswer: String,
    val isCorrect: Boolean,
)

data class EvaluationSummary(
    val latestResult: EvaluationResult? = null,
    val macroAccuracy: Float? = null,
    val evaluatedCount: Int = 0,
    val errorMessage: String? = null,
)

object CustomAppEvaluator {
    fun evaluate(
        parsedPrediction: ParsedPrediction?,
        goldRecords: List<GoldTsvRecord>,
        priorResults: List<EvaluationResult>,
        parseErrorMessage: String?,
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
            goldRecords.firstOrNull {
                it.query == prediction.query && it.rewritedQuery == prediction.rewritedQuery
            }
                ?: return EvaluationSummary(
                    latestResult = null,
                    macroAccuracy = computeMacroAccuracy(priorResults),
                    evaluatedCount = priorResults.size,
                    errorMessage = "No matching gold TSV row found for query and rewrited_query.",
                )

        val latestResult =
            EvaluationResult(
                uniqueIdx = matchingGold.uniqueIdx,
                goldAnswer = matchingGold.answer,
                predictedAnswer = prediction.answer,
                isCorrect = prediction.answer == matchingGold.answer,
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
}
