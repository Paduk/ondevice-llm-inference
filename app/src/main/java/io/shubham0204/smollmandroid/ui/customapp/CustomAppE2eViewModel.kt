package io.shubham0204.smollmandroid.ui.customapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.shubham0204.smollm.SmolLM
import io.shubham0204.smollmandroid.data.ApiMetadataAssetStore
import io.shubham0204.smollmandroid.data.AppDB
import io.shubham0204.smollmandroid.data.Chat
import io.shubham0204.smollmandroid.data.ChatMessage
import io.shubham0204.smollmandroid.data.LLMModel
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
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.random.Random

private const val PREF_SETUP_PROMPT_PRESET_KEY = "custom_app.setup.prompt_preset_key"
private const val PREF_SETUP_TEMPERATURE = "custom_app.setup.temperature"
private const val PREF_SETUP_MIN_P = "custom_app.setup.min_p"
private const val PREF_SETUP_CONTEXT_SIZE = "custom_app.setup.context_size"
private const val PREF_SETUP_NUM_THREADS = "custom_app.setup.num_threads"
private const val PREF_SETUP_USE_MMAP = "custom_app.setup.use_mmap"
private const val PREF_SETUP_USE_MLOCK = "custom_app.setup.use_mlock"
private const val PREF_SETUP_TSV_PATH = "custom_app.setup.tsv_path"
private const val PREF_SETUP_TSV_NAME = "custom_app.setup.tsv_name"
private const val PREF_BATCH_RUN_MODE = "custom_app.batch.run_mode"
private const val PREF_E2E_RMA_MODEL_ID = "custom_app.e2e.rma_model_id"
private const val PREF_E2E_TOOL_MODEL_ID = "custom_app.e2e.tool_model_id"

private val e2eQwen3RmaPromptTemplate =
    """
    <|im_start|>system
    Rewrite the query clearly by replacing ambiguous pronouns (like "it", "that") with explicit information from the conversation history. Keep exactly the same sentence structure. Do NOT generate or include any information, words, or values outside of the provided conversation_history and query. <|im_end|>
    <|im_start|>user
    {data}<|im_end|><|im_start|>assistant
    """.trimIndent()

private const val e2ePhiRmaPromptTemplate =
    "<|system|>Rewrite the query clearly by replacing ambiguous pronouns (like \"it\", \"that\") with explicit information from the conversation history. Keep exactly the same sentence structure. Do NOT generate or include any information, words, or values outside of the provided conversation_history and query. <|end|><|user|>{data}<|end|><|assistant|>"

private val e2eQwen3ToolPromptTemplate =
    """
    <|im_start|>system
    Given a user query and a list of available tools, select the most appropriate tool and generate the corresponding parameters. If no tool matches the query, set the tool to 'None'. Only use parameter values that are explicitly stated or can be reasonably inferred from the query.
     <|tool|>{tools}<|/tool|><|im_end|>
    <|im_start|>user
    User Query: {rewrited_query}<|im_end|>
    <|im_start|>assistant
    <think>

    </think>
    """.trimIndent()

private const val e2ePhiToolPromptTemplate =
    "<|system|>Given a user query and a list of available tools, select the most appropriate tool and generate the corresponding parameters. If no tool matches the query, set the tool to 'None'. Only use parameter values that are explicitly stated or can be reasonably inferred from the query.\n <|tool|>{tools}<|/tool|><|end|><|user|>User Query: {rewrited_query}<|end|><|assistant|>"

data class E2eBatchResult(
    val uniqueIdx: String,
    val intermediateRewrite: String,
    val finalToolOutput: String,
    val isCorrect: Boolean? = null,
)

data class CustomAppE2eUiState(
    val availableModels: List<LLMModel> = emptyList(),
    val selectedPipelineKey: String = PROMPT_PRESET_E2E_QWEN3_PIPELINE,
    val availableRmaModels: List<LLMModel> = emptyList(),
    val availableToolModels: List<LLMModel> = emptyList(),
    val selectedRmaModelId: Long = -1L,
    val selectedToolModelId: Long = -1L,
    val selectedRmaModel: LLMModel? = null,
    val selectedToolModel: LLMModel? = null,
    val goldRecords: List<GoldTsvRecord> = emptyList(),
    val goldTsvName: String = "",
    val goldTsvLoadError: String? = null,
    val selectedBatchRunMode: String = BATCH_RUN_MODE_FIRST_1,
    val latestIntermediateRewrite: String? = null,
    val latestFinalToolOutput: String? = null,
    val batchConversationMessages: List<ChatMessage> = emptyList(),
    val partialResponse: String = "",
    val isBatchRunning: Boolean = false,
    val isBatchStopping: Boolean = false,
    val batchTotalCount: Int = 0,
    val batchCompletedCount: Int = 0,
    val batchFailedCount: Int = 0,
    val batchLatestUniqueIdx: String? = null,
    val batchStatusMessage: String? = null,
    val latestBatchResult: E2eBatchResult? = null,
    val evaluationHistory: List<EvaluationResult> = emptyList(),
    val latestEvaluationResult: EvaluationResult? = null,
    val macroAccuracy: Float? = null,
    val evaluationErrorMessage: String? = null,
    val generationSpeedTokensPerSec: Float? = null,
    val prefillSpeedTokensPerSec: Float? = null,
    val prefillTimeMs: Long? = null,
    val generationTimeMs: Long? = null,
    val totalTimeMs: Long? = null,
    val promptTokenCount: Int? = null,
    val generatedTokenCount: Int? = null,
    val contextLengthUsed: Int? = null,
    val errorMessage: String? = null,
    val statusMessage: String? = null,
) {
    val conversationMessages: List<ChatMessage>
        get() = batchConversationMessages.takeLast(10)

    val canRun: Boolean
        get() = selectedRmaModel != null && selectedToolModel != null && goldRecords.isNotEmpty() && !isBatchRunning

    val batchCompletionPercent: Int
        get() =
            if (batchTotalCount <= 0) 0
            else ((batchCompletedCount.toFloat() / batchTotalCount.toFloat()) * 100).toInt()
}

@KoinViewModel
class CustomAppE2eViewModel(
    private val appDB: AppDB,
    private val sharedPrefStore: SharedPrefStore,
    private val apiMetadataAssetStore: ApiMetadataAssetStore,
    private val smolLMManager: SmolLMManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CustomAppE2eUiState())
    val uiState: StateFlow<CustomAppE2eUiState> = _uiState
    private var batchRunJob: Job? = null

    init {
        initialize()
    }

    fun selectPipeline(pipelineKey: String) {
        if (pipelineKey != PROMPT_PRESET_E2E_QWEN3_PIPELINE && pipelineKey != PROMPT_PRESET_E2E_PHI_PIPELINE) return
        sharedPrefStore.put(PREF_SETUP_PROMPT_PRESET_KEY, pipelineKey)
        _uiState.update { it.copy(selectedPipelineKey = pipelineKey) }
        refreshFilteredModels(_uiState.value.availableModels, pipelineKey)
    }

    fun selectRmaModel(modelId: Long) {
        _uiState.update { state ->
            state.copy(
                selectedRmaModelId = modelId,
                selectedRmaModel = state.availableRmaModels.firstOrNull { it.id == modelId },
            )
        }
        sharedPrefStore.put(PREF_E2E_RMA_MODEL_ID, modelId)
    }

    fun selectToolModel(modelId: Long) {
        _uiState.update { state ->
            state.copy(
                selectedToolModelId = modelId,
                selectedToolModel = state.availableToolModels.firstOrNull { it.id == modelId },
            )
        }
        sharedPrefStore.put(PREF_E2E_TOOL_MODEL_ID, modelId)
    }

    fun selectBatchRunMode(modeKey: String) {
        if (batchRunModeOptions.none { it.key == modeKey }) return
        _uiState.update { it.copy(selectedBatchRunMode = modeKey) }
        sharedPrefStore.put(PREF_BATCH_RUN_MODE, modeKey)
    }

    fun startBatchRun() {
        val state = _uiState.value
        val rmaModel = state.selectedRmaModel ?: run {
            _uiState.update { it.copy(errorMessage = "Select an RMA model for the E2E pipeline.") }
            return
        }
        val toolModel = state.selectedToolModel ?: run {
            _uiState.update { it.copy(errorMessage = "Select a Toolcalling model for the E2E pipeline.") }
            return
        }
        if (state.isBatchRunning) return
        if (state.goldRecords.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "E2E batch run requires a loaded TSV gold file.") }
            return
        }

        val selectedRows = selectBatchRows(state.goldRecords, state.selectedBatchRunMode)
        if (selectedRows.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "No TSV rows available for the selected batch mode.") }
            return
        }

        batchRunJob?.cancel()
        batchRunJob =
            viewModelScope.launch(Dispatchers.Main) {
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
                        batchStatusMessage = "Starting E2E batch run...",
                        latestIntermediateRewrite = null,
                        latestFinalToolOutput = null,
                        latestBatchResult = null,
                        evaluationHistory = emptyList(),
                        latestEvaluationResult = null,
                        macroAccuracy = null,
                        evaluationErrorMessage = null,
                        errorMessage = null,
                        statusMessage = null,
                    )
                }

                val apiMetadataByPlan = apiMetadataAssetStore.getAllSimple()
                var rmaChat: Chat? = null
                var toolChat: Chat? = null
                try {
                    rmaChat = createBatchChat(rmaModel, "E2E RMA Session")
                    toolChat = createBatchChat(toolModel, "E2E Tool Session")

                    selectedRows.forEachIndexed { index, record ->
                        _uiState.update {
                            it.copy(
                                batchLatestUniqueIdx = record.uniqueIdx,
                                batchStatusMessage = "Running ${index + 1}/${selectedRows.size}: ${record.uniqueIdx}",
                            )
                        }

                        smolLMManager.unload()
                        loadModelSuspend(rmaChat, rmaModel.path)
                        resetLoadedStateSuspend()
                        val rmaPrompt =
                            CustomAppRmaPromptRenderer.render(
                                template = rmaTemplateForPipeline(state.selectedPipelineKey),
                                record = record,
                            )
                        val rmaResponse = getRawResponseSuspend(rmaPrompt)
                        val intermediateRewrite = rmaResponse.response.trim()

                        smolLMManager.unload()
                        loadModelSuspend(toolChat, toolModel.path)
                        resetLoadedStateSuspend()
                        val toolPrompt =
                            CustomAppPromptTemplateRenderer.render(
                                template = toolPromptTemplateForPipeline(state.selectedPipelineKey),
                                record = record.copy(rewritedQuery = intermediateRewrite),
                                apiMetadataByPlan = apiMetadataByPlan,
                            ).prompt
                        val toolResponse = getRawResponseSuspend(toolPrompt)
                        val finalToolOutput = toolResponse.response.trim()
                        val parseResult = runCatching { CustomAppJsonParser.parse(finalToolOutput) }
                        val evaluationSummary =
                            CustomAppEvaluator.evaluate(
                                parsedPrediction = parseResult.getOrNull(),
                                goldRecords = state.goldRecords,
                                priorResults = _uiState.value.evaluationHistory,
                                parseErrorMessage = parseResult.exceptionOrNull()?.message,
                                goldRecordHint = record,
                            )

                        val result =
                            E2eBatchResult(
                                uniqueIdx = record.uniqueIdx,
                                intermediateRewrite = intermediateRewrite,
                                finalToolOutput = finalToolOutput,
                                isCorrect = evaluationSummary.latestResult?.isCorrect,
                            )
                        val messagesToAdd =
                            listOf(
                                ChatMessage(
                                    id = -(((index + 1) * 2L) - 1L),
                                    chatId = toolChat.id,
                                    message = "RMA: $intermediateRewrite",
                                    isUserMessage = false,
                                ),
                                ChatMessage(
                                    id = -((index + 1) * 2L),
                                    chatId = toolChat.id,
                                    message = "Tool: $finalToolOutput",
                                    isUserMessage = false,
                                ),
                            )
                        val failed =
                            intermediateRewrite.isBlank() ||
                                finalToolOutput.isBlank() ||
                                evaluationSummary.errorMessage != null
                        val current = _uiState.value
                        val updatedHistory =
                            evaluationSummary.latestResult?.let { latest ->
                                current.evaluationHistory.filterNot { it.uniqueIdx == latest.uniqueIdx } + latest
                            } ?: current.evaluationHistory
                        _uiState.update {
                            it.copy(
                                partialResponse = "",
                                generationSpeedTokensPerSec = toolResponse.generationSpeed,
                                prefillSpeedTokensPerSec = toolResponse.prefillSpeed,
                                prefillTimeMs = toolResponse.prefillTimeMs,
                                generationTimeMs = toolResponse.generationTimeMs,
                                totalTimeMs = toolResponse.totalTimeMs,
                                promptTokenCount = toolResponse.promptTokenCount,
                                generatedTokenCount = toolResponse.generatedTokenCount,
                                contextLengthUsed = toolResponse.contextLengthUsed,
                                batchConversationMessages = current.batchConversationMessages + messagesToAdd,
                                latestIntermediateRewrite = intermediateRewrite,
                                latestFinalToolOutput = finalToolOutput,
                                latestBatchResult = result,
                                evaluationHistory = updatedHistory,
                                latestEvaluationResult = evaluationSummary.latestResult,
                                macroAccuracy = evaluationSummary.macroAccuracy,
                                evaluationErrorMessage = evaluationSummary.errorMessage,
                                batchCompletedCount = index + 1,
                                batchFailedCount =
                                    if (failed) current.batchFailedCount + 1 else current.batchFailedCount,
                                batchStatusMessage = "Completed ${index + 1}/${selectedRows.size}: ${record.uniqueIdx}",
                            )
                        }
                    }

                    _uiState.update {
                        it.copy(
                            isBatchRunning = false,
                            batchStatusMessage = "E2E batch run finished.",
                            statusMessage = "E2E batch run finished.",
                        )
                    }
                } catch (_: CancellationException) {
                    _uiState.update {
                        it.copy(
                            isBatchRunning = false,
                            isBatchStopping = false,
                            batchStatusMessage = "E2E batch run stopped.",
                            statusMessage = "E2E batch run stopped.",
                            errorMessage = null,
                        )
                    }
                } catch (error: Exception) {
                    _uiState.update {
                        it.copy(
                            isBatchRunning = false,
                            isBatchStopping = false,
                            batchStatusMessage = null,
                            errorMessage = error.message ?: "E2E batch run failed.",
                        )
                    }
                } finally {
                    withContext(NonCancellable) {
                        smolLMManager.unloadSafely()
                        rmaChat?.let {
                            appDB.deleteMessages(it.id)
                            appDB.deleteChat(it)
                        }
                        toolChat?.let {
                            appDB.deleteMessages(it.id)
                            appDB.deleteChat(it)
                        }
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
                batchStatusMessage = "Stopping E2E batch run...",
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

    override fun onCleared() {
        smolLMManager.unload()
        super.onCleared()
    }

    private fun initialize() {
        viewModelScope.launch(Dispatchers.IO) {
            val availableModels = appDB.getModelsList()
            val pipelineKey =
                sharedPrefStore.get(PREF_SETUP_PROMPT_PRESET_KEY, PROMPT_PRESET_E2E_QWEN3_PIPELINE)
            val goldTsvPath = sharedPrefStore.get(PREF_SETUP_TSV_PATH, "")
            val goldTsvName = sharedPrefStore.get(PREF_SETUP_TSV_NAME, "")
            val goldTsvLoadResult =
                if (goldTsvPath.isBlank()) Result.success(emptyList())
                else runCatching { CustomAppTsvLoader.load(goldTsvPath) }
            val storedRmaModelId = sharedPrefStore.get(PREF_E2E_RMA_MODEL_ID, -1L)
            val storedToolModelId = sharedPrefStore.get(PREF_E2E_TOOL_MODEL_ID, -1L)

            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        availableModels = availableModels,
                        goldRecords = goldTsvLoadResult.getOrDefault(emptyList()),
                        goldTsvName = goldTsvName,
                        goldTsvLoadError = goldTsvLoadResult.exceptionOrNull()?.message,
                        selectedBatchRunMode = sharedPrefStore.get(PREF_BATCH_RUN_MODE, BATCH_RUN_MODE_FIRST_1),
                    )
                }
                refreshFilteredModels(
                    availableModels = availableModels,
                    pipelineKey = pipelineKey,
                    preferredRmaModelId = storedRmaModelId,
                    preferredToolModelId = storedToolModelId,
                )
            }
        }
    }

    private fun refreshFilteredModels(
        availableModels: List<LLMModel>,
        pipelineKey: String,
        preferredRmaModelId: Long = -1L,
        preferredToolModelId: Long = -1L,
    ) {
        val familyKeyword =
            when (pipelineKey) {
                PROMPT_PRESET_E2E_PHI_PIPELINE -> "phi"
                else -> "qwen"
            }
        val filteredModels =
            availableModels.filter { it.name.contains(familyKeyword, ignoreCase = true) }
        val selectedRmaModel =
            filteredModels.firstOrNull { it.id == preferredRmaModelId } ?: filteredModels.firstOrNull()
        val selectedToolModel =
            filteredModels.firstOrNull { it.id == preferredToolModelId } ?: filteredModels.getOrNull(
                if (filteredModels.size > 1) 1 else 0
            ) ?: filteredModels.firstOrNull()

        _uiState.update {
            it.copy(
                selectedPipelineKey = pipelineKey,
                availableRmaModels = filteredModels,
                availableToolModels = filteredModels,
                selectedRmaModelId = selectedRmaModel?.id ?: -1L,
                selectedToolModelId = selectedToolModel?.id ?: -1L,
                selectedRmaModel = selectedRmaModel,
                selectedToolModel = selectedToolModel,
                errorMessage =
                    if (filteredModels.isEmpty()) {
                        "No imported models matched the selected E2E pipeline family."
                    } else {
                        null
                    },
            )
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

    private fun rmaTemplateForPipeline(pipelineKey: String): String =
        when (pipelineKey) {
            PROMPT_PRESET_E2E_PHI_PIPELINE -> e2ePhiRmaPromptTemplate
            else -> e2eQwen3RmaPromptTemplate
        }

    private fun toolPromptTemplateForPipeline(pipelineKey: String): String =
        when (pipelineKey) {
            PROMPT_PRESET_E2E_PHI_PIPELINE -> e2ePhiToolPromptTemplate
            else -> e2eQwen3ToolPromptTemplate
        }

    private fun createBatchChat(model: LLMModel, chatName: String): Chat =
        appDB.addChat(
            chatName = chatName,
            chatTemplate = model.chatTemplate,
            systemPrompt = "",
            llmModelId = model.id,
            isTask = false,
        ).copy(
            minP = sharedPrefStore.get(PREF_SETUP_MIN_P, "0.1").toFloatOrNull() ?: 0.1f,
            temperature = sharedPrefStore.get(PREF_SETUP_TEMPERATURE, "0.0").toFloatOrNull() ?: 0.0f,
            nThreads = sharedPrefStore.get(PREF_SETUP_NUM_THREADS, "4").toIntOrNull() ?: 4,
            useMmap = sharedPrefStore.get(PREF_SETUP_USE_MMAP, true),
            useMlock = sharedPrefStore.get(PREF_SETUP_USE_MLOCK, false),
            contextSize =
                sharedPrefStore.get(PREF_SETUP_CONTEXT_SIZE, model.contextSize.toString()).toIntOrNull()
                    ?: model.contextSize,
            contextSizeConsumed = 0,
            chatTemplate = model.chatTemplate,
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
                        continuation.resumeWithException(IllegalStateException("E2E batch run cancelled."))
                    }
                },
                onError = { error ->
                    if (continuation.isActive) continuation.resumeWithException(error)
                },
            )
        }

    private suspend fun resetLoadedStateSuspend() =
        withContext(Dispatchers.Default) {
            smolLMManager.resetLoadedState("")
        }
}
