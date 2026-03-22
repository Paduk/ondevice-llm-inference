package io.shubham0204.smollmandroid.ui.customapp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.shubham0204.smollm.SmolLM
import io.shubham0204.smollmandroid.data.ApiMetadataAssetStore
import io.shubham0204.smollmandroid.data.AppDB
import io.shubham0204.smollmandroid.data.BatchExportSession
import io.shubham0204.smollmandroid.data.BatchResultExportStore
import io.shubham0204.smollmandroid.data.Chat
import io.shubham0204.smollmandroid.data.ChatMessage
import io.shubham0204.smollmandroid.data.LLMModel
import io.shubham0204.smollmandroid.data.PersistedBatchCaseResult
import io.shubham0204.smollmandroid.data.PersistedBatchSummary
import io.shubham0204.smollmandroid.data.SharedPrefStore
import io.shubham0204.smollmandroid.llm.SmolLMManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val PREF_SETUP_MODEL_ID = "custom_app.setup.model_id"
private const val PREF_SETUP_SYSTEM_PROMPT = "custom_app.setup.system_prompt"
private const val PREF_SETUP_TEMPERATURE = "custom_app.setup.temperature"
private const val PREF_SETUP_MIN_P = "custom_app.setup.min_p"
private const val PREF_SETUP_CONTEXT_SIZE = "custom_app.setup.context_size"
private const val PREF_SETUP_NUM_THREADS = "custom_app.setup.num_threads"
private const val PREF_SETUP_USE_MMAP = "custom_app.setup.use_mmap"
private const val PREF_SETUP_USE_MLOCK = "custom_app.setup.use_mlock"
private const val PREF_SETUP_TSV_PATH = "custom_app.setup.tsv_path"
private const val PREF_SETUP_TSV_NAME = "custom_app.setup.tsv_name"
private const val PREF_BATCH_RUN_MODE = "custom_app.batch.run_mode"

const val BATCH_RUN_MODE_FIRST_1 = "first_1"
const val BATCH_RUN_MODE_TOP_50 = "top_50"
const val BATCH_RUN_MODE_ALL = "all"

data class BatchRunModeOption(
    val key: String,
    val label: String,
    val description: String,
)

val batchRunModeOptions =
    listOf(
        BatchRunModeOption(
            key = BATCH_RUN_MODE_FIRST_1,
            label = "Top 1",
            description = "Run only the first valid TSV row.",
        ),
        BatchRunModeOption(
            key = BATCH_RUN_MODE_TOP_50,
            label = "Top 50",
            description = "Run up to the first 50 valid TSV rows.",
        ),
        BatchRunModeOption(
            key = BATCH_RUN_MODE_ALL,
            label = "All",
            description = "Run the full TSV in file order.",
        ),
    )

data class CustomAppChatUiState(
    val chat: Chat? = null,
    val selectedModel: LLMModel? = null,
    val messages: List<ChatMessage> = emptyList(),
    val batchConversationMessages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val partialResponse: String = "",
    val isModelLoading: Boolean = false,
    val isGenerating: Boolean = false,
    val isModelReady: Boolean = false,
    val generationSpeedTokensPerSec: Float? = null,
    val prefillSpeedTokensPerSec: Float? = null,
    val prefillTimeMs: Long? = null,
    val generationTimeMs: Long? = null,
    val totalTimeMs: Long? = null,
    val promptTokenCount: Int? = null,
    val generatedTokenCount: Int? = null,
    val generationTimeSecs: Int? = null,
    val contextLengthUsed: Int? = null,
    val parsedPrediction: ParsedToolCall? = null,
    val parseErrorMessage: String? = null,
    val latestRawModelOutput: String? = null,
    val goldRecords: List<GoldTsvRecord> = emptyList(),
    val goldTsvName: String = "",
    val goldTsvLoadError: String? = null,
    val selectedBatchRunMode: String = BATCH_RUN_MODE_FIRST_1,
    val renderedPromptPreview: String? = null,
    val renderedToolsMissingPlans: List<String> = emptyList(),
    val renderedToolsCandidateCount: Int = 0,
    val renderedToolsCount: Int = 0,
    val isBatchRunning: Boolean = false,
    val batchTotalCount: Int = 0,
    val batchCompletedCount: Int = 0,
    val batchFailedCount: Int = 0,
    val batchTotalPrefillTimeMs: Long = 0,
    val batchTotalGenerationTimeMs: Long = 0,
    val batchTotalGeneratedTokens: Int = 0,
    val batchGenerationSpeedSum: Float = 0f,
    val batchPrefillSpeedSum: Float = 0f,
    val batchMeasuredCount: Int = 0,
    val batchLatestUniqueIdx: String? = null,
    val batchStatusMessage: String? = null,
    val batchResultFilePath: String? = null,
    val batchSummaryFilePath: String? = null,
    val batchLastFlushCompletedCount: Int = 0,
    val batchIsResumed: Boolean = false,
    val batchResumeSkippedCount: Int = 0,
    val evaluationHistory: List<EvaluationResult> = emptyList(),
    val latestEvaluationResult: EvaluationResult? = null,
    val macroAccuracy: Float? = null,
    val evaluationErrorMessage: String? = null,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
) {
    val conversationMessages: List<ChatMessage>
        get() = messages + batchConversationMessages.takeLast(10)

    val latestRawModelOutputEscaped: String?
        get() = latestRawModelOutput?.toEscapedDebugString()

    val batchSuccessCount: Int
        get() = (batchCompletedCount - batchFailedCount).coerceAtLeast(0)

    val batchCompletionPercent: Int
        get() =
            if (batchTotalCount <= 0) 0
            else ((batchCompletedCount.toFloat() / batchTotalCount.toFloat()) * 100).toInt()

    val batchSuccessRate: Float?
        get() =
            if (batchCompletedCount <= 0) null
            else batchSuccessCount.toFloat() / batchCompletedCount.toFloat()

    val batchAveragePrefillTimeMs: Long?
        get() = if (batchMeasuredCount <= 0) null else batchTotalPrefillTimeMs / batchMeasuredCount

    val batchAverageGenerationTimeMs: Long?
        get() = if (batchMeasuredCount <= 0) null else batchTotalGenerationTimeMs / batchMeasuredCount

    val batchAverageGenerationSpeed: Float?
        get() = if (batchMeasuredCount <= 0) null else batchGenerationSpeedSum / batchMeasuredCount.toFloat()

    val batchAveragePrefillSpeed: Float?
        get() = if (batchMeasuredCount <= 0) null else batchPrefillSpeedSum / batchMeasuredCount.toFloat()
}

@KoinViewModel
class CustomAppChatViewModel(
    @Suppress("UnusedPrivateProperty")
    private val context: Context,
    private val appDB: AppDB,
    private val sharedPrefStore: SharedPrefStore,
    private val apiMetadataAssetStore: ApiMetadataAssetStore,
    private val batchResultExportStore: BatchResultExportStore,
    private val smolLMManager: SmolLMManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CustomAppChatUiState())
    val uiState: StateFlow<CustomAppChatUiState> = _uiState
    private var batchRunJob: Job? = null

    init {
        initializeSession()
    }

    fun updateInput(value: String) {
        _uiState.update { it.copy(inputText = value) }
    }

    fun selectBatchRunMode(modeKey: String) {
        if (batchRunModeOptions.none { it.key == modeKey }) {
            return
        }
        _uiState.update { it.copy(selectedBatchRunMode = modeKey) }
        sharedPrefStore.put(PREF_BATCH_RUN_MODE, modeKey)
    }

    fun sendMessage() {
        val currentState = _uiState.value
        val chat = currentState.chat ?: return
        val query = currentState.inputText.trim()
        if (
            query.isBlank() ||
                !currentState.isModelReady ||
                currentState.isGenerating ||
                currentState.isBatchRunning
        ) {
            return
        }

        appDB.addUserMessage(chat.id, query)
        _uiState.update {
            it.copy(
                inputText = "",
                partialResponse = "",
                isGenerating = true,
                prefillSpeedTokensPerSec = null,
                prefillTimeMs = null,
                generationTimeMs = null,
                totalTimeMs = null,
                promptTokenCount = null,
                generatedTokenCount = null,
                parsedPrediction = null,
                parseErrorMessage = null,
                latestRawModelOutput = null,
                errorMessage = null,
                statusMessage = "Generating response...",
            )
        }
        refreshMessages(chat.id)

        smolLMManager.getResponse(
            query = query,
            responseTransform = { it.trim() },
            onPartialResponseGenerated = { partial ->
                _uiState.update { state -> state.copy(partialResponse = partial) }
            },
            onSuccess = { response ->
                val parseResult = runCatching { CustomAppJsonParser.parse(response.response) }
                val evaluationSummary =
                    CustomAppEvaluator.evaluate(
                        parsedPrediction = parseResult.getOrNull(),
                        goldRecords = _uiState.value.goldRecords,
                        priorResults = _uiState.value.evaluationHistory,
                        parseErrorMessage = parseResult.exceptionOrNull()?.message,
                        goldRecordHint = null,
                    )
                val updatedChat =
                    chat.copy(
                        dateUsed = Date(),
                        contextSizeConsumed = response.contextLengthUsed,
                    )
                appDB.updateChat(updatedChat)
                _uiState.update {
                    it.copy(
                        chat = updatedChat,
                        partialResponse = "",
                        isGenerating = false,
                        generationSpeedTokensPerSec = response.generationSpeed,
                        prefillSpeedTokensPerSec = response.prefillSpeed,
                        prefillTimeMs = response.prefillTimeMs,
                        generationTimeMs = response.generationTimeMs,
                        totalTimeMs = response.totalTimeMs,
                        promptTokenCount = response.promptTokenCount,
                        generatedTokenCount = response.generatedTokenCount,
                        generationTimeSecs = response.generationTimeSecs,
                        contextLengthUsed = response.contextLengthUsed,
                        parsedPrediction = parseResult.getOrNull(),
                        parseErrorMessage = parseResult.exceptionOrNull()?.message,
                        latestRawModelOutput = response.response,
                        evaluationHistory =
                            evaluationSummary.latestResult?.let { latest ->
                                _uiState.value.evaluationHistory.filterNot {
                                    it.uniqueIdx == latest.uniqueIdx
                                } + latest
                            } ?: _uiState.value.evaluationHistory,
                        latestEvaluationResult = evaluationSummary.latestResult,
                        macroAccuracy = evaluationSummary.macroAccuracy,
                        evaluationErrorMessage = evaluationSummary.errorMessage,
                        statusMessage = "Response generated.",
                    )
                }
                refreshMessages(updatedChat.id)
            },
            onCancelled = {
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        partialResponse = "",
                        prefillSpeedTokensPerSec = null,
                        prefillTimeMs = null,
                        generationTimeMs = null,
                        totalTimeMs = null,
                        promptTokenCount = null,
                        generatedTokenCount = null,
                        parsedPrediction = null,
                        parseErrorMessage = null,
                        latestRawModelOutput = null,
                        latestEvaluationResult = null,
                        evaluationErrorMessage = null,
                        statusMessage = "Generation stopped.",
                    )
                }
            },
            onError = { error ->
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        partialResponse = "",
                        prefillSpeedTokensPerSec = null,
                        prefillTimeMs = null,
                        generationTimeMs = null,
                        totalTimeMs = null,
                        promptTokenCount = null,
                        generatedTokenCount = null,
                        parsedPrediction = null,
                        parseErrorMessage = null,
                        latestRawModelOutput = null,
                        latestEvaluationResult = null,
                        evaluationErrorMessage = null,
                        errorMessage = error.message ?: "Failed to generate response.",
                        statusMessage = null,
                    )
                }
            },
        )
    }

    fun clearConversation() {
        val chat = _uiState.value.chat ?: return
        if (_uiState.value.isBatchRunning) {
            stopBatchRun()
            return
        }
        smolLMManager.stopResponseGeneration()
        smolLMManager.unload()
        appDB.deleteMessages(chat.id)
        _uiState.update {
            it.copy(
                messages = emptyList(),
                batchConversationMessages = emptyList(),
                partialResponse = "",
                isGenerating = false,
                isModelReady = false,
                generationSpeedTokensPerSec = null,
                prefillSpeedTokensPerSec = null,
                prefillTimeMs = null,
                generationTimeMs = null,
                totalTimeMs = null,
                promptTokenCount = null,
                generatedTokenCount = null,
                generationTimeSecs = null,
                contextLengthUsed = null,
                parsedPrediction = null,
                parseErrorMessage = null,
                latestRawModelOutput = null,
                goldTsvLoadError = null,
                evaluationHistory = emptyList(),
                latestEvaluationResult = null,
                macroAccuracy = null,
                evaluationErrorMessage = null,
                batchTotalPrefillTimeMs = 0,
                batchTotalGenerationTimeMs = 0,
                batchTotalGeneratedTokens = 0,
                batchGenerationSpeedSum = 0f,
                batchPrefillSpeedSum = 0f,
                batchMeasuredCount = 0,
                batchResultFilePath = null,
                batchSummaryFilePath = null,
                batchLastFlushCompletedCount = 0,
                batchIsResumed = false,
                batchResumeSkippedCount = 0,
                statusMessage = "Conversation cleared.",
                errorMessage = null,
            )
        }
        loadModel(chat)
    }

    fun startBatchRun() {
        val currentState = _uiState.value
        val originalChat = currentState.chat ?: run {
            _uiState.update { it.copy(errorMessage = "No active chat session available.") }
            return
        }
        val selectedModel = currentState.selectedModel ?: run {
            _uiState.update { it.copy(errorMessage = "No model selected for batch run.") }
            return
        }
        if (currentState.isBatchRunning || currentState.isGenerating) {
            return
        }
        if (currentState.goldRecords.isEmpty()) {
            _uiState.update {
                it.copy(errorMessage = "Batch run requires a loaded TSV gold file.")
            }
            return
        }

        val selectedRows = selectBatchRows(currentState.goldRecords, currentState.selectedBatchRunMode)
        if (selectedRows.isEmpty()) {
            _uiState.update {
                it.copy(errorMessage = "No TSV rows available for the selected batch mode.")
            }
            return
        }

        batchRunJob?.cancel()
        batchRunJob =
            viewModelScope.launch(Dispatchers.Main) {
                var exportSession: BatchExportSession? = null
                val persistedResults = mutableListOf<PersistedBatchCaseResult>()
                var runCreatedAt = nowIsoString()
                val promptTemplate = originalChat.systemPrompt
                val promptHash = batchResultExportStore.hashPrompt(promptTemplate)
                val resumeCandidate =
                    batchResultExportStore.findLatestResumeCandidate(
                        sourceTsvName = currentState.goldTsvName.ifBlank { "gold.tsv" },
                        batchMode = currentState.selectedBatchRunMode,
                        promptHash = promptHash,
                    )
                val resumedRows = resumeCandidate?.priorRows.orEmpty()
                val resumedIds = resumedRows.map { it.uniqueIdx }.toSet()
                val rowsToRun = selectedRows.filterNot { it.uniqueIdx in resumedIds }
                val resumedHistory = resumedRows.toEvaluationHistory()
                val resumedMacroAccuracy = computeMacroAccuracy(resumedHistory)
                val resumedFailedCount = resumedRows.count { it.status != "success" }
                val resumedMeasuredRows = resumedRows.successOnly()
                _uiState.update {
                    it.copy(
                        isBatchRunning = true,
                        isGenerating = false,
                        batchConversationMessages = emptyList(),
                        prefillSpeedTokensPerSec = null,
                        prefillTimeMs = null,
                        generationTimeMs = null,
                        totalTimeMs = null,
                        promptTokenCount = null,
                        generatedTokenCount = null,
                        latestRawModelOutput = null,
                        batchTotalCount = selectedRows.size,
                        batchCompletedCount = resumedRows.size,
                        batchFailedCount = resumedFailedCount,
                        batchTotalPrefillTimeMs = resumedMeasuredRows.sumOf { it.prefillTimeMs },
                        batchTotalGenerationTimeMs = resumedMeasuredRows.sumOf { it.generationTimeMs },
                        batchTotalGeneratedTokens = resumedMeasuredRows.sumOf { it.generatedTokens },
                        batchGenerationSpeedSum =
                            resumedMeasuredRows.sumOf { it.generationTokensPerSec.toDouble() }.toFloat(),
                        batchPrefillSpeedSum =
                            resumedMeasuredRows.sumOf { it.prefillTokensPerSec.toDouble() }.toFloat(),
                        batchMeasuredCount = resumedMeasuredRows.size,
                        batchLatestUniqueIdx = null,
                        batchResultFilePath = resumeCandidate?.session?.resultsFile?.absolutePath,
                        batchSummaryFilePath = resumeCandidate?.session?.summaryFile?.absolutePath,
                        batchLastFlushCompletedCount = resumedRows.size,
                        batchIsResumed = resumeCandidate != null,
                        batchResumeSkippedCount = resumedRows.size,
                        evaluationHistory = resumedHistory,
                        macroAccuracy = resumedMacroAccuracy,
                        batchStatusMessage =
                            if (resumeCandidate != null) {
                                "Resuming batch run from ${resumedRows.size}/${selectedRows.size}..."
                            } else {
                                "Starting batch run..."
                            },
                        statusMessage = null,
                        errorMessage = null,
                    )
                }

                if (rowsToRun.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isBatchRunning = false,
                            batchStatusMessage = "Batch run already complete.",
                            statusMessage = "Batch run already complete.",
                        )
                    }
                    return@launch
                }

                smolLMManager.unload()
                var tempChat: Chat? = null
                try {
                    val apiMetadataByPlan = apiMetadataAssetStore.getAllSimple()
                    exportSession =
                        resumeCandidate?.session
                            ?: batchResultExportStore.createSession(
                                sourceTsvName = currentState.goldTsvName.ifBlank { "gold.tsv" },
                                testType = "Toolcalling",
                                modelName = selectedModel.name,
                            )
                    if (resumeCandidate != null) {
                        persistedResults += resumedRows
                        runCreatedAt = resumedRows.firstOrNull()?.evaluatedAt ?: runCreatedAt
                    }
                    tempChat = createBatchChat(originalChat)
                    loadModelSuspend(tempChat, selectedModel.path)
                    _uiState.update {
                        it.copy(
                            batchResultFilePath = exportSession.resultsFile.absolutePath,
                            batchSummaryFilePath = exportSession.summaryFile.absolutePath,
                        )
                    }

                    rowsToRun.forEachIndexed { _, record ->
                        val renderResult =
                            CustomAppPromptTemplateRenderer.render(
                                template = promptTemplate,
                                record = record,
                                apiMetadataByPlan = apiMetadataByPlan,
                            )
                        _uiState.update {
                            it.copy(
                                batchLatestUniqueIdx = record.uniqueIdx,
                                batchStatusMessage =
                                    "Running ${persistedResults.size + 1}/${selectedRows.size}: ${record.uniqueIdx}",
                                renderedPromptPreview = renderResult.prompt,
                                renderedToolsMissingPlans = renderResult.missingPlans,
                                renderedToolsCandidateCount = renderResult.parsedCandidateCount,
                                renderedToolsCount = renderResult.renderedToolCount,
                            )
                        }

                        appDB.deleteMessages(tempChat.id)
                        resetBatchStateSuspend(tempChat.systemPrompt)
                        val response = getRawResponseSuspend(renderResult.prompt)
                        val batchOutputMessage =
                            ChatMessage(
                                id = -((persistedResults.size + 1).toLong()),
                                chatId = originalChat.id,
                                message = response.response,
                                isUserMessage = false,
                            )
                        val parseResult = runCatching { CustomAppJsonParser.parse(response.response) }
                        val currentUiState = _uiState.value
                        val evaluationSummary =
                            CustomAppEvaluator.evaluate(
                                parsedPrediction = parseResult.getOrNull(),
                                goldRecords = currentUiState.goldRecords,
                                priorResults = currentUiState.evaluationHistory,
                                parseErrorMessage = parseResult.exceptionOrNull()?.message,
                                goldRecordHint = record,
                            )
                        val updatedHistory =
                            evaluationSummary.latestResult?.let { latest ->
                                currentUiState.evaluationHistory.filterNot {
                                    it.uniqueIdx == latest.uniqueIdx
                                } + latest
                            } ?: currentUiState.evaluationHistory
                        val isFailed =
                            parseResult.isFailure || evaluationSummary.errorMessage != null
                        val shouldMeasureMetrics = !isFailed
                        val latestResult = evaluationSummary.latestResult
                        persistedResults +=
                            PersistedBatchCaseResult(
                                uniqueIdx = record.uniqueIdx,
                                query = record.query,
                                rewritedQuery = record.rewritedQuery,
                                goldAnswer = record.answer,
                                generated = response.response,
                                parseSuccess = parseResult.isSuccess,
                                planCorrect = latestResult?.isPlanCorrect ?: false,
                                argumentsCorrect = latestResult?.isArgumentsCorrect ?: false,
                                allCorrect = latestResult?.isCorrect ?: false,
                                prefillTokensPerSec = response.prefillSpeed,
                                generationTokensPerSec = response.generationSpeed,
                                overallTokensPerSec =
                                    computeOverallTokensPerSec(
                                        promptTokens = response.promptTokenCount,
                                        generatedTokens = response.generatedTokenCount,
                                        totalTimeMs = response.totalTimeMs,
                                    ),
                                prefillTimeMs = response.prefillTimeMs,
                                generationTimeMs = response.generationTimeMs,
                                totalTimeMs = response.totalTimeMs,
                                promptTokens = response.promptTokenCount,
                                generatedTokens = response.generatedTokenCount,
                                status =
                                    when {
                                        parseResult.isFailure -> "parse_failed"
                                        evaluationSummary.errorMessage != null -> "evaluation_failed"
                                        else -> "success"
                                    },
                                errorMessage =
                                    evaluationSummary.errorMessage
                                        ?: parseResult.exceptionOrNull()?.message
                                        ?: "",
                                evaluatedAt = nowIsoString(),
                            )

                        _uiState.update {
                            it.copy(
                                partialResponse = "",
                                generationSpeedTokensPerSec = response.generationSpeed,
                                prefillSpeedTokensPerSec = response.prefillSpeed,
                                prefillTimeMs = response.prefillTimeMs,
                                generationTimeMs = response.generationTimeMs,
                                totalTimeMs = response.totalTimeMs,
                                promptTokenCount = response.promptTokenCount,
                                generatedTokenCount = response.generatedTokenCount,
                                generationTimeSecs = response.generationTimeSecs,
                                contextLengthUsed = response.contextLengthUsed,
                                batchConversationMessages =
                                    currentUiState.batchConversationMessages + batchOutputMessage,
                                parsedPrediction = parseResult.getOrNull(),
                                parseErrorMessage = parseResult.exceptionOrNull()?.message,
                                latestRawModelOutput = response.response,
                                evaluationHistory = updatedHistory,
                                latestEvaluationResult = evaluationSummary.latestResult,
                                macroAccuracy = evaluationSummary.macroAccuracy,
                                evaluationErrorMessage = evaluationSummary.errorMessage,
                                batchCompletedCount = persistedResults.size,
                                batchFailedCount =
                                    if (isFailed) currentUiState.batchFailedCount + 1
                                    else currentUiState.batchFailedCount,
                                batchTotalPrefillTimeMs =
                                    currentUiState.batchTotalPrefillTimeMs +
                                        if (shouldMeasureMetrics) response.prefillTimeMs else 0,
                                batchTotalGenerationTimeMs =
                                    currentUiState.batchTotalGenerationTimeMs +
                                        if (shouldMeasureMetrics) response.generationTimeMs else 0,
                                batchTotalGeneratedTokens =
                                    currentUiState.batchTotalGeneratedTokens +
                                        if (shouldMeasureMetrics) response.generatedTokenCount else 0,
                                batchGenerationSpeedSum =
                                    currentUiState.batchGenerationSpeedSum +
                                        if (shouldMeasureMetrics) response.generationSpeed else 0f,
                                batchPrefillSpeedSum =
                                    currentUiState.batchPrefillSpeedSum +
                                        if (shouldMeasureMetrics) response.prefillSpeed else 0f,
                                batchMeasuredCount =
                                    currentUiState.batchMeasuredCount + if (shouldMeasureMetrics) 1 else 0,
                                batchStatusMessage =
                                    "Completed ${persistedResults.size}/${selectedRows.size}: ${record.uniqueIdx}",
                            )
                        }

                        if (persistedResults.size % 10 == 0) {
                            flushBatchExport(
                                exportSession = exportSession,
                                rows = persistedResults,
                                sourceTsvName = currentState.goldTsvName.ifBlank { "gold.tsv" },
                                sourceTsvRowCount = currentState.goldRecords.size,
                                selectedRowCount = selectedRows.size,
                                batchMode = currentState.selectedBatchRunMode,
                                promptTemplate = promptTemplate,
                                macroAccuracy = evaluationSummary.macroAccuracy,
                                failedRows = persistedResults.count { it.status != "success" },
                                runCreatedAt = runCreatedAt,
                                isResumed = resumeCandidate != null,
                                resumeSkippedRows = resumedRows.size,
                            )
                            _uiState.update {
                                it.copy(
                                    batchLastFlushCompletedCount = persistedResults.size,
                                    batchStatusMessage =
                                        "Completed ${persistedResults.size}/${selectedRows.size}: ${record.uniqueIdx} (saved ${persistedResults.size})",
                                )
                            }
                        }
                    }

                    flushBatchExport(
                        exportSession = exportSession,
                        rows = persistedResults,
                        sourceTsvName = currentState.goldTsvName.ifBlank { "gold.tsv" },
                        sourceTsvRowCount = currentState.goldRecords.size,
                        selectedRowCount = selectedRows.size,
                        batchMode = currentState.selectedBatchRunMode,
                        promptTemplate = promptTemplate,
                        macroAccuracy = _uiState.value.macroAccuracy,
                        failedRows = persistedResults.count { it.status != "success" },
                        runCreatedAt = runCreatedAt,
                        isResumed = resumeCandidate != null,
                        resumeSkippedRows = resumedRows.size,
                    )

                    _uiState.update {
                        it.copy(
                            isBatchRunning = false,
                            batchStatusMessage = "Batch run finished.",
                            statusMessage = "Batch run finished.",
                            batchLastFlushCompletedCount = persistedResults.size,
                        )
                    }
                } catch (error: Exception) {
                    flushBatchExport(
                        exportSession = exportSession,
                        rows = persistedResults,
                        sourceTsvName = currentState.goldTsvName.ifBlank { "gold.tsv" },
                        sourceTsvRowCount = currentState.goldRecords.size,
                        selectedRowCount = selectedRows.size,
                        batchMode = currentState.selectedBatchRunMode,
                        promptTemplate = originalChat.systemPrompt,
                        macroAccuracy = _uiState.value.macroAccuracy,
                        failedRows = persistedResults.count { it.status != "success" },
                        runCreatedAt = runCreatedAt,
                        isResumed = resumeCandidate != null,
                        resumeSkippedRows = resumedRows.size,
                    )
                    _uiState.update {
                        it.copy(
                            isBatchRunning = false,
                            batchStatusMessage = null,
                            errorMessage = error.message ?: "Batch run failed.",
                            batchLastFlushCompletedCount = persistedResults.size,
                        )
                    }
                } finally {
                    tempChat?.let {
                        appDB.deleteMessages(it.id)
                        appDB.deleteChat(it)
                    }
                    smolLMManager.unload()
                    loadModel(originalChat)
                }
            }
    }

    fun stopBatchRun() {
        batchRunJob?.cancel()
        batchRunJob = null
        smolLMManager.stopResponseGeneration()
        _uiState.update {
            it.copy(
                isBatchRunning = false,
                batchStatusMessage = "Batch run stopped.",
                statusMessage = "Batch run stopped.",
            )
        }
    }

    fun deleteSavedBatchResults() {
        val currentState = _uiState.value
        if (currentState.isBatchRunning || currentState.isGenerating) {
            _uiState.update {
                it.copy(errorMessage = "Stop the current run before deleting saved results.")
            }
            return
        }

        val resultDeleted = currentState.batchResultFilePath?.let { File(it).delete() } ?: false
        val summaryDeleted = currentState.batchSummaryFilePath?.let { File(it).delete() } ?: false
        if (!resultDeleted && !summaryDeleted) {
            _uiState.update {
                it.copy(
                    statusMessage = null,
                    errorMessage = "No saved batch result files were found to delete.",
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                batchConversationMessages = emptyList(),
                generationSpeedTokensPerSec = null,
                prefillSpeedTokensPerSec = null,
                prefillTimeMs = null,
                generationTimeMs = null,
                totalTimeMs = null,
                promptTokenCount = null,
                generatedTokenCount = null,
                generationTimeSecs = null,
                contextLengthUsed = null,
                parsedPrediction = null,
                parseErrorMessage = null,
                latestRawModelOutput = null,
                batchTotalCount = 0,
                batchCompletedCount = 0,
                batchFailedCount = 0,
                batchTotalPrefillTimeMs = 0,
                batchTotalGenerationTimeMs = 0,
                batchTotalGeneratedTokens = 0,
                batchGenerationSpeedSum = 0f,
                batchPrefillSpeedSum = 0f,
                batchMeasuredCount = 0,
                batchLatestUniqueIdx = null,
                batchStatusMessage = null,
                batchResultFilePath = null,
                batchSummaryFilePath = null,
                batchLastFlushCompletedCount = 0,
                batchIsResumed = false,
                batchResumeSkippedCount = 0,
                evaluationHistory = emptyList(),
                latestEvaluationResult = null,
                macroAccuracy = null,
                evaluationErrorMessage = null,
                statusMessage = "Saved batch results deleted. The next run will start fresh.",
                errorMessage = null,
            )
        }
    }

    override fun onCleared() {
        smolLMManager.unload()
        super.onCleared()
    }

    private fun initializeSession() {
        viewModelScope.launch(Dispatchers.IO) {
            val modelId = sharedPrefStore.get(PREF_SETUP_MODEL_ID, -1L)
            if (modelId == -1L) {
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(errorMessage = "No model selected in setup.")
                    }
                }
                return@launch
            }

            val selectedModel = appDB.getModel(modelId)
            val goldTsvPath = sharedPrefStore.get(PREF_SETUP_TSV_PATH, "")
            val goldTsvName = sharedPrefStore.get(PREF_SETUP_TSV_NAME, "")
            val goldTsvLoadResult =
                if (goldTsvPath.isBlank()) {
                    Result.success(emptyList())
                } else {
                    runCatching { CustomAppTsvLoader.load(goldTsvPath) }
                }
            val existingChat = appDB.getRecentlyUsedChat()
            val chat =
                if (
                    existingChat != null &&
                        existingChat.name == "Custom Session" &&
                        existingChat.llmModelId == modelId &&
                        !existingChat.isTask
                ) {
                    existingChat.copy(
                        systemPrompt = sharedPrefStore.get(
                            PREF_SETUP_SYSTEM_PROMPT,
                            existingChat.systemPrompt,
                        ),
                        temperature = sharedPrefStore.get(
                            PREF_SETUP_TEMPERATURE,
                            existingChat.temperature.toString(),
                        ).toFloatOrNull() ?: existingChat.temperature,
                        minP = sharedPrefStore.get(
                            PREF_SETUP_MIN_P,
                            existingChat.minP.toString(),
                        ).toFloatOrNull() ?: existingChat.minP,
                        contextSize = sharedPrefStore.get(
                            PREF_SETUP_CONTEXT_SIZE,
                            existingChat.contextSize.toString(),
                        ).toIntOrNull() ?: existingChat.contextSize,
                        nThreads = sharedPrefStore.get(
                            PREF_SETUP_NUM_THREADS,
                            existingChat.nThreads.toString(),
                        ).toIntOrNull() ?: existingChat.nThreads,
                        useMmap = sharedPrefStore.get(PREF_SETUP_USE_MMAP, existingChat.useMmap),
                        useMlock = sharedPrefStore.get(PREF_SETUP_USE_MLOCK, existingChat.useMlock),
                        chatTemplate = selectedModel.chatTemplate,
                    ).also(appDB::updateChat)
                } else {
                    appDB.addChat(
                        chatName = "Custom Session",
                        chatTemplate = selectedModel.chatTemplate,
                        systemPrompt = sharedPrefStore.get(
                            PREF_SETUP_SYSTEM_PROMPT,
                            "You are a helpful assistant.",
                        ),
                        llmModelId = modelId,
                        isTask = false,
                    ).copy(
                        temperature =
                            sharedPrefStore.get(PREF_SETUP_TEMPERATURE, "0.0").toFloatOrNull()
                                ?: 0.0f,
                        minP =
                            sharedPrefStore.get(PREF_SETUP_MIN_P, "0.1").toFloatOrNull() ?: 0.1f,
                        contextSize =
                            sharedPrefStore.get(PREF_SETUP_CONTEXT_SIZE, "2048").toIntOrNull()
                                ?: selectedModel.contextSize,
                        nThreads =
                            sharedPrefStore.get(PREF_SETUP_NUM_THREADS, "4").toIntOrNull() ?: 4,
                        useMmap = sharedPrefStore.get(PREF_SETUP_USE_MMAP, true),
                        useMlock = sharedPrefStore.get(PREF_SETUP_USE_MLOCK, false),
                    ).also(appDB::updateChat)
                }

            withContext(Dispatchers.Main) {
                val goldRecords = goldTsvLoadResult.getOrDefault(emptyList())
                val systemPrompt = chat.systemPrompt
                val apiMetadataByPlan =
                    runCatching { apiMetadataAssetStore.getAllSimple() }.getOrDefault(emptyMap())
                val previewResult =
                    goldRecords.firstOrNull()?.let { record ->
                        CustomAppPromptTemplateRenderer.render(
                            template = systemPrompt,
                            record = record,
                            apiMetadataByPlan = apiMetadataByPlan,
                        )
                    }
                _uiState.update {
                    it.copy(
                        chat = chat,
                        selectedModel = selectedModel,
                        goldRecords = goldRecords,
                        goldTsvName = goldTsvName,
                        goldTsvLoadError = goldTsvLoadResult.exceptionOrNull()?.message,
                        selectedBatchRunMode =
                            sharedPrefStore.get(PREF_BATCH_RUN_MODE, BATCH_RUN_MODE_FIRST_1)
                                .takeIf { mode -> batchRunModeOptions.any { it.key == mode } }
                                ?: BATCH_RUN_MODE_FIRST_1,
                        renderedPromptPreview = previewResult?.prompt,
                        renderedToolsMissingPlans = previewResult?.missingPlans ?: emptyList(),
                        renderedToolsCandidateCount = previewResult?.parsedCandidateCount ?: 0,
                        renderedToolsCount = previewResult?.renderedToolCount ?: 0,
                        statusMessage = "Loading model...",
                        errorMessage = null,
                    )
                }
            }

            refreshMessages(chat.id)
            loadModel(chat)
        }
    }

    private fun refreshMessages(chatId: Long) {
        viewModelScope.launch {
            appDB.getMessages(chatId).collectLatest { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
    }

    private fun loadModel(chat: Chat) {
        val selectedModel = _uiState.value.selectedModel ?: return
        _uiState.update {
            it.copy(
                isModelLoading = true,
                isModelReady = false,
                statusMessage = "Loading model...",
                errorMessage = null,
            )
        }
        smolLMManager.load(
            chat = chat,
            modelPath = selectedModel.path,
            params =
                SmolLM.InferenceParams(
                    minP = chat.minP,
                    temperature = chat.temperature,
                    storeChats = true,
                    contextSize = chat.contextSize.toLong(),
                    chatTemplate = chat.chatTemplate,
                    numThreads = chat.nThreads,
                    useMmap = chat.useMmap,
                    useMlock = chat.useMlock,
                ),
            onError = { error ->
                _uiState.update {
                    it.copy(
                        isModelLoading = false,
                        isModelReady = false,
                        statusMessage = null,
                        errorMessage = error.message ?: "Failed to load model.",
                    )
                }
            },
            onSuccess = {
                _uiState.update {
                    it.copy(
                        isModelLoading = false,
                        isModelReady = true,
                        statusMessage = "Model ready.",
                        errorMessage = null,
                    )
                }
            },
        )
    }

    private fun selectBatchRows(
        goldRecords: List<GoldTsvRecord>,
        mode: String,
    ): List<GoldTsvRecord> =
        when (mode) {
            BATCH_RUN_MODE_FIRST_1 -> goldRecords.take(1)
            BATCH_RUN_MODE_TOP_50 -> goldRecords.take(50)
            BATCH_RUN_MODE_ALL -> goldRecords
            else -> goldRecords.take(1)
        }

    private suspend fun flushBatchExport(
        exportSession: BatchExportSession?,
        rows: List<PersistedBatchCaseResult>,
        sourceTsvName: String,
        sourceTsvRowCount: Int,
        selectedRowCount: Int,
        batchMode: String,
        promptTemplate: String,
        macroAccuracy: Float?,
        failedRows: Int,
        runCreatedAt: String,
        isResumed: Boolean,
        resumeSkippedRows: Int,
    ) {
        if (exportSession == null || rows.isEmpty()) return
        withContext(Dispatchers.IO) {
            val measuredRows = rows.successOnly()
            batchResultExportStore.writeResults(
                session = exportSession,
                rows = rows,
                summary =
                    PersistedBatchSummary(
                        runId = exportSession.runId,
                        sourceTsvName = sourceTsvName,
                        sourceTsvRowCount = sourceTsvRowCount,
                        selectedRowCount = selectedRowCount,
                        batchMode = batchMode,
                        promptHash = batchResultExportStore.hashPrompt(promptTemplate),
                        isResumed = isResumed,
                        resumeSkippedRows = resumeSkippedRows,
                        completedRows = rows.size,
                        failedRows = failedRows,
                        macroAccuracy = macroAccuracy,
                        avgPrefillTokensPerSec = measuredRows.map { it.prefillTokensPerSec }.averageOrNull(),
                        avgGenerationTokensPerSec = measuredRows.map { it.generationTokensPerSec }.averageOrNull(),
                        avgOverallTokensPerSec = measuredRows.map { it.overallTokensPerSec }.averageOrNull(),
                        avgPrefillTimeMs = measuredRows.map { it.prefillTimeMs }.averageLongOrNull(),
                        avgGenerationTimeMs = measuredRows.map { it.generationTimeMs }.averageLongOrNull(),
                        avgTotalTimeMs = measuredRows.map { it.totalTimeMs }.averageLongOrNull(),
                        createdAt = runCreatedAt,
                        updatedAt = nowIsoString(),
                    ),
            )
        }
    }

    private fun createBatchChat(originalChat: Chat): Chat =
        appDB.addChat(
            chatName = "Batch Session",
            chatTemplate = originalChat.chatTemplate,
            systemPrompt = "",
            llmModelId = originalChat.llmModelId,
            isTask = false,
        ).copy(
            minP = originalChat.minP,
            temperature = originalChat.temperature,
            nThreads = originalChat.nThreads,
            useMmap = originalChat.useMmap,
            useMlock = originalChat.useMlock,
            contextSize = originalChat.contextSize,
            contextSizeConsumed = 0,
            chatTemplate = originalChat.chatTemplate,
        ).also(appDB::updateChat)

    private suspend fun loadModelSuspend(chat: Chat, modelPath: String) =
        suspendCancellableCoroutine<Unit> { continuation ->
            smolLMManager.load(
                chat = chat,
                modelPath = modelPath,
                params =
                    SmolLM.InferenceParams(
                        minP = chat.minP,
                        temperature = chat.temperature,
                        storeChats = true,
                        contextSize = chat.contextSize.toLong(),
                        chatTemplate = chat.chatTemplate,
                        numThreads = chat.nThreads,
                        useMmap = chat.useMmap,
                        useMlock = chat.useMlock,
                    ),
                onError = { error ->
                    if (continuation.isActive) {
                        continuation.resumeWithException(error)
                    }
                },
                onSuccess = {
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                },
            )
        }

    private suspend fun getResponseSuspend(query: String): SmolLMManager.SmolLMResponse =
        suspendCancellableCoroutine { continuation ->
            smolLMManager.getResponse(
                query = query,
                responseTransform = { it.trim() },
                onPartialResponseGenerated = { partial ->
                    _uiState.update { state -> state.copy(partialResponse = partial) }
                },
                onSuccess = { response ->
                    if (continuation.isActive) {
                        continuation.resume(response)
                    }
                },
                onCancelled = {
                    if (continuation.isActive) {
                        continuation.resumeWithException(IllegalStateException("Batch run cancelled."))
                    }
                },
                onError = { error ->
                    if (continuation.isActive) {
                        continuation.resumeWithException(error)
                    }
                },
            )
        }

    private suspend fun getRawResponseSuspend(prompt: String): SmolLMManager.SmolLMResponse =
        suspendCancellableCoroutine { continuation ->
            smolLMManager.getRawPromptResponse(
                prompt = prompt,
                responseTransform = { it.trim() },
                onPartialResponseGenerated = { partial ->
                    _uiState.update { state -> state.copy(partialResponse = partial) }
                },
                onSuccess = { response ->
                    if (continuation.isActive) {
                        continuation.resume(response)
                    }
                },
                onCancelled = {
                    if (continuation.isActive) {
                        continuation.resumeWithException(IllegalStateException("Batch run cancelled."))
                    }
                },
                onError = { error ->
                    if (continuation.isActive) {
                        continuation.resumeWithException(error)
                    }
                },
            )
        }

    private suspend fun resetBatchStateSuspend(systemPrompt: String) =
        withContext(Dispatchers.Default) {
            smolLMManager.resetLoadedState(systemPrompt)
        }
}

private fun String.toEscapedDebugString(): String {
    val builder = StringBuilder(length)
    for (char in this) {
        when (char) {
            '\n' -> builder.append("\\n")
            '\r' -> builder.append("\\r")
            '\t' -> builder.append("\\t")
            '\b' -> builder.append("\\b")
            '\u000C' -> builder.append("\\f")
            '\\' -> builder.append("\\\\")
            else -> {
                if (char.isISOControl()) {
                    builder.append("\\u").append(char.code.toString(16).padStart(4, '0'))
                } else {
                    builder.append(char)
                }
            }
        }
    }
    return builder.toString()
}

private fun computeOverallTokensPerSec(
    promptTokens: Int,
    generatedTokens: Int,
    totalTimeMs: Long,
): Float {
    if (totalTimeMs <= 0L) return 0f
    return ((promptTokens + generatedTokens).toFloat() / totalTimeMs.toFloat()) * 1000f
}

private fun List<PersistedBatchCaseResult>.successOnly(): List<PersistedBatchCaseResult> =
    filter { it.status == "success" }

private fun Iterable<Float>.averageOrNull(): Float? {
    val list = this.toList()
    if (list.isEmpty()) return null
    return list.sum() / list.size.toFloat()
}

private fun Iterable<Long>.averageLongOrNull(): Long? {
    val list = this.toList()
    if (list.isEmpty()) return null
    return list.sum() / list.size
}

private fun nowIsoString(): String =
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())

private fun List<PersistedBatchCaseResult>.toEvaluationHistory(): List<EvaluationResult> =
    this.map {
        EvaluationResult(
            uniqueIdx = it.uniqueIdx,
            goldAnswer = it.goldAnswer,
            predictedAnswer = it.generated,
            isPlanCorrect = it.planCorrect,
            isArgumentsCorrect = it.argumentsCorrect,
            isCorrect = it.allCorrect,
        )
    }

private fun computeMacroAccuracy(results: List<EvaluationResult>): Float? {
    if (results.isEmpty()) return null
    val byGoldAnswer = results.groupBy { it.goldAnswer }
    return byGoldAnswer.values
        .map { classResults ->
            classResults.count { it.isCorrect }.toFloat() / classResults.size.toFloat()
        }
        .average()
        .toFloat()
}
