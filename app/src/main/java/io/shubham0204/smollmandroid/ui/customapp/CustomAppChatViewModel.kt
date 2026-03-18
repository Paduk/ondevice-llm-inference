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
import java.util.Date
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.random.Random

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
const val BATCH_RUN_MODE_RANDOM_10 = "random_10"
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
            label = "First 1",
            description = "Run only the first valid TSV row.",
        ),
        BatchRunModeOption(
            key = BATCH_RUN_MODE_RANDOM_10,
            label = "Random 10",
            description = "Run up to 10 randomly selected TSV rows.",
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
    val inputText: String = "",
    val partialResponse: String = "",
    val isModelLoading: Boolean = false,
    val isGenerating: Boolean = false,
    val isModelReady: Boolean = false,
    val generationSpeedTokensPerSec: Float? = null,
    val generationTimeSecs: Int? = null,
    val contextLengthUsed: Int? = null,
    val parsedPrediction: ParsedPrediction? = null,
    val parseErrorMessage: String? = null,
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
    val batchLatestUniqueIdx: String? = null,
    val batchStatusMessage: String? = null,
    val evaluationHistory: List<EvaluationResult> = emptyList(),
    val latestEvaluationResult: EvaluationResult? = null,
    val macroAccuracy: Float? = null,
    val evaluationErrorMessage: String? = null,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
) {
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
}

@KoinViewModel
class CustomAppChatViewModel(
    private val appDB: AppDB,
    private val sharedPrefStore: SharedPrefStore,
    private val apiMetadataAssetStore: ApiMetadataAssetStore,
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
                parsedPrediction = null,
                parseErrorMessage = null,
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
                        generationTimeSecs = response.generationTimeSecs,
                        contextLengthUsed = response.contextLengthUsed,
                        parsedPrediction = parseResult.getOrNull(),
                        parseErrorMessage = parseResult.exceptionOrNull()?.message,
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
                        parsedPrediction = null,
                        parseErrorMessage = null,
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
                        parsedPrediction = null,
                        parseErrorMessage = null,
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
                partialResponse = "",
                isGenerating = false,
                isModelReady = false,
                generationSpeedTokensPerSec = null,
                generationTimeSecs = null,
                contextLengthUsed = null,
                parsedPrediction = null,
                parseErrorMessage = null,
                goldTsvLoadError = null,
                evaluationHistory = emptyList(),
                latestEvaluationResult = null,
                macroAccuracy = null,
                evaluationErrorMessage = null,
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
                _uiState.update {
                    it.copy(
                        isBatchRunning = true,
                        isGenerating = false,
                        batchTotalCount = selectedRows.size,
                        batchCompletedCount = 0,
                        batchFailedCount = 0,
                        batchLatestUniqueIdx = null,
                        batchStatusMessage = "Starting batch run...",
                        statusMessage = null,
                        errorMessage = null,
                    )
                }

                smolLMManager.unload()
                var tempChat: Chat? = null
                try {
                    val promptTemplate = originalChat.systemPrompt
                    val apiMetadataByPlan = apiMetadataAssetStore.getAllSimple()
                    tempChat = createBatchChat(originalChat)
                    loadModelSuspend(tempChat, selectedModel.path)

                    selectedRows.forEachIndexed { index, record ->
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
                                    "Running ${index + 1}/${selectedRows.size}: ${record.uniqueIdx}",
                                renderedPromptPreview = renderResult.prompt,
                                renderedToolsMissingPlans = renderResult.missingPlans,
                                renderedToolsCandidateCount = renderResult.parsedCandidateCount,
                                renderedToolsCount = renderResult.renderedToolCount,
                            )
                        }

                        appDB.deleteMessages(tempChat.id)
                        resetBatchStateSuspend(tempChat.systemPrompt)
                        val response = getResponseSuspend(renderResult.prompt)
                        val parseResult = runCatching { CustomAppJsonParser.parse(response.response) }
                        val currentUiState = _uiState.value
                        val evaluationSummary =
                            CustomAppEvaluator.evaluate(
                                parsedPrediction = parseResult.getOrNull(),
                                goldRecords = currentUiState.goldRecords,
                                priorResults = currentUiState.evaluationHistory,
                                parseErrorMessage = parseResult.exceptionOrNull()?.message,
                            )
                        val updatedHistory =
                            evaluationSummary.latestResult?.let { latest ->
                                currentUiState.evaluationHistory.filterNot {
                                    it.uniqueIdx == latest.uniqueIdx
                                } + latest
                            } ?: currentUiState.evaluationHistory
                        val isFailed =
                            parseResult.isFailure || evaluationSummary.errorMessage != null

                        _uiState.update {
                            it.copy(
                                partialResponse = "",
                                generationSpeedTokensPerSec = response.generationSpeed,
                                generationTimeSecs = response.generationTimeSecs,
                                contextLengthUsed = response.contextLengthUsed,
                                parsedPrediction = parseResult.getOrNull(),
                                parseErrorMessage = parseResult.exceptionOrNull()?.message,
                                evaluationHistory = updatedHistory,
                                latestEvaluationResult = evaluationSummary.latestResult,
                                macroAccuracy = evaluationSummary.macroAccuracy,
                                evaluationErrorMessage = evaluationSummary.errorMessage,
                                batchCompletedCount = index + 1,
                                batchFailedCount =
                                    if (isFailed) currentUiState.batchFailedCount + 1
                                    else currentUiState.batchFailedCount,
                                batchStatusMessage =
                                    "Completed ${index + 1}/${selectedRows.size}: ${record.uniqueIdx}",
                            )
                        }
                    }

                    _uiState.update {
                        it.copy(
                            isBatchRunning = false,
                            batchStatusMessage = "Batch run finished.",
                            statusMessage = "Batch run finished.",
                        )
                    }
                } catch (error: Exception) {
                    _uiState.update {
                        it.copy(
                            isBatchRunning = false,
                            batchStatusMessage = null,
                            errorMessage = error.message ?: "Batch run failed.",
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
                            sharedPrefStore.get(PREF_SETUP_TEMPERATURE, "0.8").toFloatOrNull()
                                ?: 0.8f,
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
                            sharedPrefStore.get(PREF_BATCH_RUN_MODE, BATCH_RUN_MODE_FIRST_1),
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
            BATCH_RUN_MODE_RANDOM_10 -> goldRecords.shuffled(Random(System.currentTimeMillis())).take(10)
            BATCH_RUN_MODE_ALL -> goldRecords
            else -> goldRecords.take(1)
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

    private suspend fun resetBatchStateSuspend(systemPrompt: String) =
        withContext(Dispatchers.Default) {
            smolLMManager.resetLoadedState(systemPrompt)
        }
}
