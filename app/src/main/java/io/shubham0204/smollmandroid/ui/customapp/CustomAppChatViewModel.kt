package io.shubham0204.smollmandroid.ui.customapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.shubham0204.smollm.SmolLM
import io.shubham0204.smollmandroid.data.AppDB
import io.shubham0204.smollmandroid.data.Chat
import io.shubham0204.smollmandroid.data.ChatMessage
import io.shubham0204.smollmandroid.data.LLMModel
import io.shubham0204.smollmandroid.data.SharedPrefStore
import io.shubham0204.smollmandroid.llm.SmolLMManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel
import java.util.Date

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
    val evaluationHistory: List<EvaluationResult> = emptyList(),
    val latestEvaluationResult: EvaluationResult? = null,
    val macroAccuracy: Float? = null,
    val evaluationErrorMessage: String? = null,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
)

@KoinViewModel
class CustomAppChatViewModel(
    private val appDB: AppDB,
    private val sharedPrefStore: SharedPrefStore,
    private val smolLMManager: SmolLMManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CustomAppChatUiState())
    val uiState: StateFlow<CustomAppChatUiState> = _uiState

    init {
        initializeSession()
    }

    fun updateInput(value: String) {
        _uiState.update { it.copy(inputText = value) }
    }

    fun sendMessage() {
        val currentState = _uiState.value
        val chat = currentState.chat ?: return
        val query = currentState.inputText.trim()
        if (query.isBlank() || !currentState.isModelReady || currentState.isGenerating) {
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
                _uiState.update {
                    it.copy(
                        chat = chat,
                        selectedModel = selectedModel,
                        goldRecords = goldTsvLoadResult.getOrDefault(emptyList()),
                        goldTsvName = goldTsvName,
                        goldTsvLoadError = goldTsvLoadResult.exceptionOrNull()?.message,
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
}
