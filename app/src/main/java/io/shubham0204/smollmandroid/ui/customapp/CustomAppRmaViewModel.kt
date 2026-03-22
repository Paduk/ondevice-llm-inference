package io.shubham0204.smollmandroid.ui.customapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.shubham0204.smollm.SmolLM
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.random.Random

private const val PREF_SETUP_MODEL_ID = "custom_app.setup.model_id"
private const val PREF_SETUP_PROMPT_PRESET_KEY = "custom_app.setup.prompt_preset_key"
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

data class RmaEvaluationResult(
    val uniqueIdx: String,
    val goldRewrite: String,
    val predictedRewrite: String,
    val isCorrect: Boolean,
)

data class CustomAppRmaUiState(
    val selectedModel: LLMModel? = null,
    val selectedPromptPresetKey: String = PROMPT_PRESET_RMA_QWEN3,
    val goldRecords: List<GoldTsvRecord> = emptyList(),
    val goldTsvName: String = "",
    val goldTsvLoadError: String? = null,
    val selectedBatchRunMode: String = BATCH_RUN_MODE_FIRST_1,
    val renderedRmaPromptPreview: String? = null,
    val rmaPromptPreviewError: String? = null,
    val batchConversationMessages: List<ChatMessage> = emptyList(),
    val partialResponse: String = "",
    val isBatchRunning: Boolean = false,
    val isBatchStopping: Boolean = false,
    val batchTotalCount: Int = 0,
    val batchCompletedCount: Int = 0,
    val batchFailedCount: Int = 0,
    val batchLatestUniqueIdx: String? = null,
    val batchStatusMessage: String? = null,
    val batchResultFilePath: String? = null,
    val batchSummaryFilePath: String? = null,
    val batchLastFlushCompletedCount: Int = 0,
    val batchIsResumed: Boolean = false,
    val evaluationHistory: List<RmaEvaluationResult> = emptyList(),
    val latestEvaluationResult: RmaEvaluationResult? = null,
    val exactMatchAccuracy: Float? = null,
    val evaluationErrorMessage: String? = null,
    val generationSpeedTokensPerSec: Float? = null,
    val prefillSpeedTokensPerSec: Float? = null,
    val prefillTimeMs: Long? = null,
    val generationTimeMs: Long? = null,
    val totalTimeMs: Long? = null,
    val promptTokenCount: Int? = null,
    val generatedTokenCount: Int? = null,
    val contextLengthUsed: Int? = null,
    val batchTotalPrefillTimeMs: Long = 0,
    val batchTotalGenerationTimeMs: Long = 0,
    val batchTotalGeneratedTokens: Int = 0,
    val batchGenerationSpeedSum: Float = 0f,
    val batchPrefillSpeedSum: Float = 0f,
    val errorMessage: String? = null,
    val statusMessage: String? = null,
) {
    val conversationMessages: List<ChatMessage>
        get() = batchConversationMessages.takeLast(10)

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
        get() = if (batchCompletedCount <= 0) null else batchTotalPrefillTimeMs / batchCompletedCount

    val batchAverageGenerationTimeMs: Long?
        get() = if (batchCompletedCount <= 0) null else batchTotalGenerationTimeMs / batchCompletedCount

    val batchAverageGenerationSpeed: Float?
        get() = if (batchCompletedCount <= 0) null else batchGenerationSpeedSum / batchCompletedCount.toFloat()

    val batchAveragePrefillSpeed: Float?
        get() = if (batchCompletedCount <= 0) null else batchPrefillSpeedSum / batchCompletedCount.toFloat()
}

@KoinViewModel
class CustomAppRmaViewModel(
    private val appDB: AppDB,
    private val sharedPrefStore: SharedPrefStore,
    private val batchResultExportStore: BatchResultExportStore,
    private val smolLMManager: SmolLMManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CustomAppRmaUiState())
    val uiState: StateFlow<CustomAppRmaUiState> = _uiState
    private var batchRunJob: Job? = null

    init {
        initialize()
    }

    fun selectBatchRunMode(modeKey: String) {
        if (batchRunModeOptions.none { it.key == modeKey }) return
        _uiState.update { it.copy(selectedBatchRunMode = modeKey) }
        sharedPrefStore.put(PREF_BATCH_RUN_MODE, modeKey)
    }

    fun startBatchRun() {
        val currentState = _uiState.value
        val selectedModel = currentState.selectedModel ?: run {
            _uiState.update { it.copy(errorMessage = "No model selected for RMA batch run.") }
            return
        }
        if (currentState.isBatchRunning) return
        if (currentState.goldRecords.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "RMA batch run requires a loaded TSV gold file.") }
            return
        }

        val selectedRows = selectBatchRows(currentState.goldRecords, currentState.selectedBatchRunMode)
        if (selectedRows.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "No TSV rows available for the selected batch mode.") }
            return
        }

        batchRunJob?.cancel()
        batchRunJob =
            viewModelScope.launch(Dispatchers.Main) {
                var exportSession: BatchExportSession? = null
                val persistedResults = mutableListOf<PersistedBatchCaseResult>()
                val runCreatedAt = nowIsoString()
                _uiState.update {
                    it.copy(
                        isBatchRunning = true,
                        isBatchStopping = false,
                        batchConversationMessages = emptyList(),
                        partialResponse = "",
                        batchTotalCount = selectedRows.size,
                        batchCompletedCount = 0,
                        batchFailedCount = 0,
                        batchLatestUniqueIdx = null,
                        batchStatusMessage = "Starting RMA batch run...",
                        batchResultFilePath = null,
                        batchSummaryFilePath = null,
                        batchLastFlushCompletedCount = 0,
                        batchIsResumed = false,
                        evaluationHistory = emptyList(),
                        latestEvaluationResult = null,
                        exactMatchAccuracy = null,
                        evaluationErrorMessage = null,
                        errorMessage = null,
                        statusMessage = null,
                        generationSpeedTokensPerSec = null,
                        prefillSpeedTokensPerSec = null,
                        prefillTimeMs = null,
                        generationTimeMs = null,
                        totalTimeMs = null,
                        promptTokenCount = null,
                        generatedTokenCount = null,
                        contextLengthUsed = null,
                        batchTotalPrefillTimeMs = 0,
                        batchTotalGenerationTimeMs = 0,
                        batchTotalGeneratedTokens = 0,
                        batchGenerationSpeedSum = 0f,
                        batchPrefillSpeedSum = 0f,
                    )
                }

                var tempChat: Chat? = null
                try {
                    val promptTemplate = sharedPrefStore.get(PREF_SETUP_SYSTEM_PROMPT, "")
                    exportSession =
                        batchResultExportStore.createSession(
                            sourceTsvName = currentState.goldTsvName.ifBlank { "gold.tsv" },
                            testType = "RMA",
                            modelName = selectedModel.name,
                        )
                    tempChat = createBatchChat(selectedModel)
                    loadModelSuspend(tempChat, selectedModel.path)
                    _uiState.update {
                        it.copy(
                            batchResultFilePath = exportSession.resultsFile.absolutePath,
                            batchSummaryFilePath = exportSession.summaryFile.absolutePath,
                        )
                    }

                    selectedRows.forEachIndexed { index, record ->
                        val renderedPrompt = CustomAppRmaPromptRenderer.render(promptTemplate, record)
                        _uiState.update {
                            it.copy(
                                renderedRmaPromptPreview = renderedPrompt,
                                rmaPromptPreviewError = null,
                                batchLatestUniqueIdx = record.uniqueIdx,
                                batchStatusMessage =
                                    "Running ${index + 1}/${selectedRows.size}: ${record.uniqueIdx}",
                            )
                        }

                        resetBatchStateSuspend()
                        val response = getRawResponseSuspend(renderedPrompt)
                        val predicted = response.response.trim()
                        val gold = record.rewritedQuery.trim()
                        val result =
                            RmaEvaluationResult(
                                uniqueIdx = record.uniqueIdx,
                                goldRewrite = gold,
                                predictedRewrite = predicted,
                                isCorrect = predicted == gold,
                            )
                        val currentUiState = _uiState.value
                        val updatedHistory = currentUiState.evaluationHistory + result
                        val accuracy =
                            if (updatedHistory.isEmpty()) null
                            else updatedHistory.count { it.isCorrect }.toFloat() / updatedHistory.size.toFloat()
                        val batchOutputMessage =
                            ChatMessage(
                                id = -((index + 1).toLong()),
                                chatId = tempChat.id,
                                message = predicted,
                                isUserMessage = false,
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
                                contextLengthUsed = response.contextLengthUsed,
                                batchConversationMessages = currentUiState.batchConversationMessages + batchOutputMessage,
                                evaluationHistory = updatedHistory,
                                latestEvaluationResult = result,
                                exactMatchAccuracy = accuracy,
                                batchCompletedCount = index + 1,
                                batchFailedCount =
                                    if (result.isCorrect) currentUiState.batchFailedCount
                                    else currentUiState.batchFailedCount + 1,
                                batchTotalPrefillTimeMs =
                                    currentUiState.batchTotalPrefillTimeMs + response.prefillTimeMs,
                                batchTotalGenerationTimeMs =
                                    currentUiState.batchTotalGenerationTimeMs + response.generationTimeMs,
                                batchTotalGeneratedTokens =
                                    currentUiState.batchTotalGeneratedTokens + response.generatedTokenCount,
                                batchGenerationSpeedSum =
                                    currentUiState.batchGenerationSpeedSum + response.generationSpeed,
                                batchPrefillSpeedSum =
                                    currentUiState.batchPrefillSpeedSum + response.prefillSpeed,
                                batchStatusMessage =
                                    "Completed ${index + 1}/${selectedRows.size}: ${record.uniqueIdx}",
                            )
                        }

                        persistedResults +=
                            PersistedBatchCaseResult(
                                uniqueIdx = record.uniqueIdx,
                                query = record.query,
                                rewritedQuery = record.rewritedQuery,
                                goldAnswer = record.rewritedQuery,
                                generated = predicted,
                                parseSuccess = true,
                                planCorrect = result.isCorrect,
                                argumentsCorrect = result.isCorrect,
                                allCorrect = result.isCorrect,
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
                                status = if (result.isCorrect) "success" else "evaluation_failed",
                                errorMessage = if (result.isCorrect) "" else "Rewrite mismatch",
                                evaluatedAt = nowIsoString(),
                            )

                        if (persistedResults.size % 10 == 0) {
                            flushBatchExport(
                                exportSession = exportSession,
                                rows = persistedResults,
                                sourceTsvName = currentState.goldTsvName.ifBlank { "gold.tsv" },
                                sourceTsvRowCount = currentState.goldRecords.size,
                                selectedRowCount = selectedRows.size,
                                batchMode = currentState.selectedBatchRunMode,
                                promptTemplate = promptTemplate,
                                exactMatchAccuracy = accuracy,
                                failedRows = persistedResults.count { it.status != "success" },
                                runCreatedAt = runCreatedAt,
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
                        exactMatchAccuracy = _uiState.value.exactMatchAccuracy,
                        failedRows = persistedResults.count { it.status != "success" },
                        runCreatedAt = runCreatedAt,
                    )

                    _uiState.update {
                        it.copy(
                            isBatchRunning = false,
                            batchStatusMessage = "RMA batch run finished.",
                            statusMessage = "RMA batch run finished.",
                            batchLastFlushCompletedCount = persistedResults.size,
                        )
                    }
                } catch (_: CancellationException) {
                    withContext(NonCancellable) {
                        flushBatchExport(
                            exportSession = exportSession,
                            rows = persistedResults,
                            sourceTsvName = currentState.goldTsvName.ifBlank { "gold.tsv" },
                            sourceTsvRowCount = currentState.goldRecords.size,
                            selectedRowCount = selectedRows.size,
                            batchMode = currentState.selectedBatchRunMode,
                            promptTemplate = sharedPrefStore.get(PREF_SETUP_SYSTEM_PROMPT, ""),
                            exactMatchAccuracy = _uiState.value.exactMatchAccuracy,
                            failedRows = persistedResults.count { it.status != "success" },
                            runCreatedAt = runCreatedAt,
                        )
                    }
                    _uiState.update {
                        it.copy(
                            isBatchRunning = false,
                            isBatchStopping = false,
                            batchStatusMessage = "RMA batch run stopped.",
                            statusMessage = "RMA batch run stopped.",
                            errorMessage = null,
                            batchLastFlushCompletedCount = persistedResults.size,
                        )
                    }
                } catch (error: Exception) {
                    withContext(NonCancellable) {
                        flushBatchExport(
                            exportSession = exportSession,
                            rows = persistedResults,
                            sourceTsvName = currentState.goldTsvName.ifBlank { "gold.tsv" },
                            sourceTsvRowCount = currentState.goldRecords.size,
                            selectedRowCount = selectedRows.size,
                            batchMode = currentState.selectedBatchRunMode,
                            promptTemplate = sharedPrefStore.get(PREF_SETUP_SYSTEM_PROMPT, ""),
                            exactMatchAccuracy = _uiState.value.exactMatchAccuracy,
                            failedRows = persistedResults.count { it.status != "success" },
                            runCreatedAt = runCreatedAt,
                        )
                    }
                    _uiState.update {
                        it.copy(
                            isBatchRunning = false,
                            isBatchStopping = false,
                            batchStatusMessage = null,
                            errorMessage = error.message ?: "RMA batch run failed.",
                            batchLastFlushCompletedCount = persistedResults.size,
                        )
                    }
                } finally {
                    withContext(NonCancellable) {
                        tempChat?.let {
                            appDB.deleteMessages(it.id)
                            appDB.deleteChat(it)
                        }
                        smolLMManager.unloadSafely()
                        batchRunJob = null
                        _uiState.update {
                            it.copy(
                                isBatchRunning = false,
                                isBatchStopping = false,
                            )
                        }
                    }
                }
            }
    }

    fun stopBatchRun() {
        _uiState.update {
            it.copy(
                isBatchStopping = true,
                batchStatusMessage = "Stopping RMA batch run...",
                statusMessage = null,
                errorMessage = null,
            )
        }
        viewModelScope.launch(Dispatchers.Main) {
            smolLMManager.stopResponseGenerationAndWait()
            batchRunJob?.cancelAndJoin()
            batchRunJob = null
        }
    }

    fun deleteSavedBatchResults() {
        val currentState = _uiState.value
        if (currentState.isBatchRunning) {
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
                batchResultFilePath = null,
                batchSummaryFilePath = null,
                batchLastFlushCompletedCount = 0,
                batchIsResumed = false,
                evaluationHistory = emptyList(),
                latestEvaluationResult = null,
                exactMatchAccuracy = null,
                statusMessage = "Saved batch results deleted. The next run will start fresh.",
                errorMessage = null,
            )
        }
    }

    override fun onCleared() {
        smolLMManager.unload()
        super.onCleared()
    }

    private fun initialize() {
        viewModelScope.launch(Dispatchers.IO) {
            val modelId = sharedPrefStore.get(PREF_SETUP_MODEL_ID, -1L)
            if (modelId == -1L) {
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(errorMessage = "No model selected in setup.") }
                }
                return@launch
            }

            val selectedModel = appDB.getModel(modelId)
            val goldTsvPath = sharedPrefStore.get(PREF_SETUP_TSV_PATH, "")
            val goldTsvName = sharedPrefStore.get(PREF_SETUP_TSV_NAME, "")
            val goldTsvLoadResult =
                if (goldTsvPath.isBlank()) Result.success(emptyList())
                else runCatching { CustomAppTsvLoader.load(goldTsvPath) }
            val systemPrompt = sharedPrefStore.get(PREF_SETUP_SYSTEM_PROMPT, "")
            val selectedPromptPresetKey =
                sharedPrefStore.get(PREF_SETUP_PROMPT_PRESET_KEY, PROMPT_PRESET_RMA_QWEN3)
            val goldRecords = goldTsvLoadResult.getOrDefault(emptyList())
            val previewResult =
                runCatching {
                    goldRecords.firstOrNull()?.let { CustomAppRmaPromptRenderer.render(systemPrompt, it) }
                }

            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        selectedModel = selectedModel,
                        selectedPromptPresetKey = selectedPromptPresetKey,
                        goldRecords = goldRecords,
                        goldTsvName = goldTsvName,
                        goldTsvLoadError = goldTsvLoadResult.exceptionOrNull()?.message,
                        selectedBatchRunMode = sharedPrefStore.get(PREF_BATCH_RUN_MODE, BATCH_RUN_MODE_FIRST_1),
                        renderedRmaPromptPreview = previewResult.getOrNull(),
                        rmaPromptPreviewError = previewResult.exceptionOrNull()?.message,
                    )
                }
            }
        }
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

    private fun createBatchChat(selectedModel: LLMModel): Chat =
        appDB.addChat(
            chatName = "RMA Batch Session",
            chatTemplate = selectedModel.chatTemplate,
            systemPrompt = "",
            llmModelId = selectedModel.id,
            isTask = false,
        ).copy(
            minP = sharedPrefStore.get(PREF_SETUP_MIN_P, "0.1").toFloatOrNull() ?: 0.1f,
            temperature = sharedPrefStore.get(PREF_SETUP_TEMPERATURE, "0.0").toFloatOrNull() ?: 0.0f,
            nThreads = sharedPrefStore.get(PREF_SETUP_NUM_THREADS, "4").toIntOrNull() ?: 4,
            useMmap = sharedPrefStore.get(PREF_SETUP_USE_MMAP, true),
            useMlock = sharedPrefStore.get(PREF_SETUP_USE_MLOCK, false),
            contextSize =
                sharedPrefStore.get(PREF_SETUP_CONTEXT_SIZE, selectedModel.contextSize.toString()).toIntOrNull()
                    ?: selectedModel.contextSize,
            contextSizeConsumed = 0,
            chatTemplate = selectedModel.chatTemplate,
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
                        storeChats = false,
                        contextSize = chat.contextSize.toLong(),
                        chatTemplate = chat.chatTemplate,
                        numThreads = chat.nThreads,
                        useMmap = chat.useMmap,
                        useMlock = chat.useMlock,
                    ),
                onError = { error ->
                    if (continuation.isActive) continuation.resumeWithException(error)
                },
                onSuccess = {
                    if (continuation.isActive) continuation.resume(Unit)
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
                    if (continuation.isActive) continuation.resume(response)
                },
                onCancelled = {
                    if (continuation.isActive) {
                        continuation.resumeWithException(IllegalStateException("RMA batch run cancelled."))
                    }
                },
                onError = { error ->
                    if (continuation.isActive) continuation.resumeWithException(error)
                },
            )
        }

    private suspend fun resetBatchStateSuspend() =
        withContext(Dispatchers.Default) {
            smolLMManager.resetLoadedState("")
        }

    private suspend fun flushBatchExport(
        exportSession: BatchExportSession?,
        rows: List<PersistedBatchCaseResult>,
        sourceTsvName: String,
        sourceTsvRowCount: Int,
        selectedRowCount: Int,
        batchMode: String,
        promptTemplate: String,
        exactMatchAccuracy: Float?,
        failedRows: Int,
        runCreatedAt: String,
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
                        isResumed = false,
                        resumeSkippedRows = 0,
                        completedRows = rows.size,
                        failedRows = failedRows,
                        macroAccuracy = exactMatchAccuracy,
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

private fun computeOverallTokensPerSec(
    promptTokens: Int,
    generatedTokens: Int,
    totalTimeMs: Long,
): Float {
    if (totalTimeMs <= 0L) return 0f
    return ((promptTokens + generatedTokens).toFloat() / totalTimeMs.toFloat()) * 1000f
}
