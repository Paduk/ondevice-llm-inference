package io.shubham0204.smollmandroid.data

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.koin.core.annotation.Single
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BatchExportSession(
    val runId: String,
    val resultsFile: File,
    val summaryFile: File,
)

data class PersistedBatchCaseResult(
    val uniqueIdx: String,
    val query: String,
    val rewritedQuery: String,
    val goldAnswer: String,
    val generated: String,
    val parseSuccess: Boolean,
    val planCorrect: Boolean,
    val argumentsCorrect: Boolean,
    val allCorrect: Boolean,
    val prefillTokensPerSec: Float,
    val generationTokensPerSec: Float,
    val overallTokensPerSec: Float,
    val prefillTimeMs: Long,
    val generationTimeMs: Long,
    val totalTimeMs: Long,
    val promptTokens: Int,
    val generatedTokens: Int,
    val status: String,
    val errorMessage: String,
    val evaluatedAt: String,
)

data class PersistedBatchSummary(
    val runId: String,
    val sourceTsvName: String,
    val sourceTsvRowCount: Int,
    val selectedRowCount: Int,
    val batchMode: String,
    val promptHash: String,
    val isResumed: Boolean,
    val resumeSkippedRows: Int,
    val completedRows: Int,
    val failedRows: Int,
    val macroAccuracy: Float?,
    val avgPrefillTokensPerSec: Float?,
    val avgGenerationTokensPerSec: Float?,
    val avgOverallTokensPerSec: Float?,
    val avgPrefillTimeMs: Long?,
    val avgGenerationTimeMs: Long?,
    val avgTotalTimeMs: Long?,
    val createdAt: String,
    val updatedAt: String,
)

data class BatchResumeCandidate(
    val session: BatchExportSession,
    val priorRows: List<PersistedBatchCaseResult>,
)

@Single
class BatchResultExportStore(
    private val context: Context,
) {
    private val json = Json { prettyPrint = true }
    private val resultsDir = File(context.filesDir, "custom_app/results/toolcalling")

    fun createSession(
        sourceTsvName: String,
        testType: String,
        modelName: String,
    ): BatchExportSession {
        val dir = resultsDir.apply { mkdirs() }
        val sanitizedSource = sourceTsvName.substringBeforeLast('.').sanitizeForFileName()
        val sanitizedTestType = testType.sanitizeForFileName()
        val sanitizedModel = modelName.substringBeforeLast('.').sanitizeForFileName()
        val runId = "${sanitizedSource}_${sanitizedTestType}_${sanitizedModel}"
        return BatchExportSession(
            runId = runId,
            resultsFile = File(dir, "${runId}_results.tsv"),
            summaryFile = File(dir, "${runId}_summary.json"),
        )
    }

    fun findLatestResumeCandidate(
        sourceTsvName: String,
        batchMode: String,
        promptHash: String,
    ): BatchResumeCandidate? {
        val dir = resultsDir
        if (!dir.exists()) return null

        val summaryFile =
            dir.listFiles()
                ?.filter { it.isFile && it.name.endsWith("_summary.json") }
                ?.sortedByDescending { it.lastModified() }
                ?.firstOrNull { file ->
                    runCatching {
                        val obj = json.parseToJsonElement(file.readText()).jsonObject
                        obj["source_tsv_name"]?.jsonPrimitive?.content == sourceTsvName &&
                            obj["batch_mode"]?.jsonPrimitive?.content == batchMode &&
                            obj["prompt_hash"]?.jsonPrimitive?.content == promptHash
                    }.getOrDefault(false)
                }
                ?: return null

        val runId = summaryFile.name.removeSuffix("_summary.json")
        val resultsFile = File(dir, "${runId}_results.tsv")
        if (!resultsFile.exists()) return null

        val priorRows = loadResults(resultsFile)
        return BatchResumeCandidate(
            session =
                BatchExportSession(
                    runId = runId,
                    resultsFile = resultsFile,
                    summaryFile = summaryFile,
                ),
            priorRows = priorRows,
        )
    }

    fun writeResults(
        session: BatchExportSession,
        rows: List<PersistedBatchCaseResult>,
        summary: PersistedBatchSummary,
    ) {
        session.resultsFile.writeText(buildResultsTsv(rows))
        session.summaryFile.writeText(buildSummaryJson(summary))
    }

    fun hashPrompt(prompt: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(prompt.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun loadResults(file: File): List<PersistedBatchCaseResult> {
        if (!file.exists()) return emptyList()
        val lines = file.readLines()
        if (lines.isEmpty()) return emptyList()
        val header = lines.first().split('\t')
        val columnIndex = header.withIndex().associate { it.value to it.index }

        fun value(cells: List<String>, name: String): String =
            cells.getOrNull(columnIndex.getValue(name))?.fromTsvCell().orEmpty()

        return lines.drop(1)
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val cells = line.split('\t')
                if (cells.size < header.size) return@mapNotNull null
                PersistedBatchCaseResult(
                    uniqueIdx = value(cells, "unique_idx"),
                    query = value(cells, "query"),
                    rewritedQuery = value(cells, "rewrited_query"),
                    goldAnswer = value(cells, "gold_answer"),
                    generated = value(cells, "generated"),
                    parseSuccess = value(cells, "parse_success").toBoolean(),
                    planCorrect = value(cells, "plan_correct").toBoolean(),
                    argumentsCorrect = value(cells, "arguments_correct").toBoolean(),
                    allCorrect = value(cells, "all_correct").toBoolean(),
                    prefillTokensPerSec = value(cells, "prefill_tokens_per_sec").toFloatOrNull() ?: 0f,
                    generationTokensPerSec = value(cells, "generation_tokens_per_sec").toFloatOrNull() ?: 0f,
                    overallTokensPerSec = value(cells, "overall_tokens_per_sec").toFloatOrNull() ?: 0f,
                    prefillTimeMs = value(cells, "prefill_time_ms").toLongOrNull() ?: 0L,
                    generationTimeMs = value(cells, "generation_time_ms").toLongOrNull() ?: 0L,
                    totalTimeMs = value(cells, "total_time_ms").toLongOrNull() ?: 0L,
                    promptTokens = value(cells, "prompt_tokens").toIntOrNull() ?: 0,
                    generatedTokens = value(cells, "generated_tokens").toIntOrNull() ?: 0,
                    status = value(cells, "status"),
                    errorMessage = value(cells, "error_message"),
                    evaluatedAt = value(cells, "evaluated_at"),
                )
            }
    }

    private fun buildResultsTsv(rows: List<PersistedBatchCaseResult>): String {
        val header =
            listOf(
                "unique_idx",
                "query",
                "rewrited_query",
                "gold_answer",
                "generated",
                "parse_success",
                "plan_correct",
                "arguments_correct",
                "all_correct",
                "prefill_tokens_per_sec",
                "generation_tokens_per_sec",
                "overall_tokens_per_sec",
                "prefill_time_ms",
                "generation_time_ms",
                "total_time_ms",
                "prompt_tokens",
                "generated_tokens",
                "status",
                "error_message",
                "evaluated_at",
            )
        val body =
            rows.joinToString("\n") { row ->
                listOf(
                    row.uniqueIdx,
                    row.query,
                    row.rewritedQuery,
                    row.goldAnswer,
                    row.generated,
                    row.parseSuccess.toString(),
                    row.planCorrect.toString(),
                    row.argumentsCorrect.toString(),
                    row.allCorrect.toString(),
                    row.prefillTokensPerSec.toString(),
                    row.generationTokensPerSec.toString(),
                    row.overallTokensPerSec.toString(),
                    row.prefillTimeMs.toString(),
                    row.generationTimeMs.toString(),
                    row.totalTimeMs.toString(),
                    row.promptTokens.toString(),
                    row.generatedTokens.toString(),
                    row.status,
                    row.errorMessage,
                    row.evaluatedAt,
                ).joinToString("\t") { it.toTsvCell() }
            }

        return buildString {
            append(header.joinToString("\t"))
            if (body.isNotEmpty()) {
                append('\n')
                append(body)
            }
        }
    }

    private fun buildSummaryJson(summary: PersistedBatchSummary): String =
        buildJsonObject {
            put("run_id", summary.runId)
            put("source_tsv_name", summary.sourceTsvName)
            put("source_tsv_row_count", summary.sourceTsvRowCount)
            put("selected_row_count", summary.selectedRowCount)
            put("batch_mode", summary.batchMode)
            put("prompt_hash", summary.promptHash)
            put("is_resumed", summary.isResumed)
            put("resume_skipped_rows", summary.resumeSkippedRows)
            put("completed_rows", summary.completedRows)
            put("failed_rows", summary.failedRows)
            put("macro_accuracy", summary.macroAccuracy)
            put("avg_prefill_tokens_per_sec", summary.avgPrefillTokensPerSec)
            put("avg_generation_tokens_per_sec", summary.avgGenerationTokensPerSec)
            put("avg_overall_tokens_per_sec", summary.avgOverallTokensPerSec)
            put("avg_prefill_time_ms", summary.avgPrefillTimeMs)
            put("avg_generation_time_ms", summary.avgGenerationTimeMs)
            put("avg_total_time_ms", summary.avgTotalTimeMs)
            put("created_at", summary.createdAt)
            put("updated_at", summary.updatedAt)
        }.toString()
}

private fun String.toTsvCell(): String =
    this.replace("\\", "\\\\")
        .replace("\t", "\\t")
        .replace("\n", "\\n")
        .replace("\r", "\\r")

private fun String.fromTsvCell(): String =
    this.replace("\\r", "\r")
        .replace("\\n", "\n")
        .replace("\\t", "\t")
        .replace("\\\\", "\\")

private fun String.sanitizeForFileName(): String =
    this.replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "batch" }
