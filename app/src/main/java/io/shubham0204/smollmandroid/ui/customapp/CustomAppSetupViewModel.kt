package io.shubham0204.smollmandroid.ui.customapp

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.shubham0204.smollm.GGUFReader
import io.shubham0204.smollm.SmolLM
import io.shubham0204.smollmandroid.data.AppDB
import io.shubham0204.smollmandroid.data.LLMModel
import io.shubham0204.smollmandroid.data.SharedPrefStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Paths

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

const val PROMPT_PRESET_CUSTOM = "custom"
const val PROMPT_PRESET_REWRITE_QWEN3 = "rewrite_qwen3"
const val PROMPT_PRESET_BASE_QWEN3 = "base_qwen3"
const val PROMPT_PRESET_REWRITE_PHI = "rewrite_phi"
const val PROMPT_PRESET_BASE_PHI = "base_phi"

private val modelAPromptTemplate =
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

private const val baseQwen3PromptTemplate = "You are a helpful assistant.\nAnswer in JSON when the task requires structured output."

private const val rewritePhiPromptTemplate =
    "You are a rewrite-focused assistant.\nUse the provided context carefully and answer in JSON."

private const val basePhiPromptTemplate =
    "You are an accuracy-focused assistant.\nReason conservatively and answer in JSON."

data class PromptPresetOption(
    val key: String,
    val label: String,
    val template: String?,
)

val promptPresetOptions =
    listOf(
        PromptPresetOption(
            key = PROMPT_PRESET_REWRITE_QWEN3,
            label = "Rewrite-Qwen3",
            template = modelAPromptTemplate,
        ),
        PromptPresetOption(
            key = PROMPT_PRESET_BASE_QWEN3,
            label = "Base-Qwen3",
            template = baseQwen3PromptTemplate,
        ),
        PromptPresetOption(
            key = PROMPT_PRESET_REWRITE_PHI,
            label = "Rewrite-Phi",
            template = rewritePhiPromptTemplate,
        ),
        PromptPresetOption(
            key = PROMPT_PRESET_BASE_PHI,
            label = "Base-Phi",
            template = basePhiPromptTemplate,
        ),
        PromptPresetOption(
            key = PROMPT_PRESET_CUSTOM,
            label = "Custom",
            template = "",
        ),
    )

data class CustomAppSetupUiState(
    val availableModels: List<LLMModel> = emptyList(),
    val selectedModelId: Long = -1L,
    val selectedModel: LLMModel? = null,
    val selectedPromptPresetKey: String = PROMPT_PRESET_CUSTOM,
    val systemPrompt: String = "You are a helpful assistant.",
    val temperatureText: String = "0.8",
    val minPText: String = "0.1",
    val contextSizeText: String = "2048",
    val numThreadsText: String = "4",
    val useMmap: Boolean = true,
    val useMlock: Boolean = false,
    val selectedTsvPath: String = "",
    val selectedTsvName: String = "",
    val isBusy: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
) {
    val canContinue: Boolean
        get() = selectedModel != null && errorMessage == null
}

@KoinViewModel
class CustomAppSetupViewModel(
    private val context: Context,
    private val appDB: AppDB,
    private val sharedPrefStore: SharedPrefStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(loadInitialState())
    val uiState: StateFlow<CustomAppSetupUiState> = _uiState

    init {
        refreshModels()
    }

    fun refreshModels() {
        viewModelScope.launch(Dispatchers.IO) {
            val models = appDB.getModelsList()
            withContext(Dispatchers.Main) {
                _uiState.update { state ->
                    val selectedModel =
                        models.firstOrNull { it.id == state.selectedModelId } ?: models.firstOrNull()
                    state.copy(
                        availableModels = models,
                        selectedModelId = selectedModel?.id ?: -1L,
                        selectedModel = selectedModel,
                        contextSizeText =
                            if (state.contextSizeText.isBlank() && selectedModel != null) {
                                selectedModel.contextSize.toString()
                            } else {
                                state.contextSizeText
                            },
                    )
                }
                persistCurrentState()
            }
        }
    }

    fun selectModel(modelId: Long) {
        _uiState.update { state ->
            val selectedModel = state.availableModels.firstOrNull { it.id == modelId }
            state.copy(
                selectedModelId = modelId,
                selectedModel = selectedModel,
                contextSizeText = selectedModel?.contextSize?.toString() ?: state.contextSizeText,
                errorMessage = null,
            )
        }
        persistCurrentState()
    }

    fun updateSystemPrompt(value: String) = updateAndPersist { it.copy(systemPrompt = value) }

    fun selectPromptPreset(presetKey: String) {
        val preset = promptPresetOptions.firstOrNull { it.key == presetKey } ?: return
        updateAndPersist { state ->
            if (preset.key == PROMPT_PRESET_CUSTOM) {
                state.copy(selectedPromptPresetKey = PROMPT_PRESET_CUSTOM, systemPrompt = "")
            } else {
                state.copy(
                    selectedPromptPresetKey = preset.key,
                    systemPrompt = preset.template ?: state.systemPrompt,
                )
            }
        }
    }

    fun updateTemperature(value: String) =
        updateAndPersist { it.copy(temperatureText = value, errorMessage = null) }

    fun updateMinP(value: String) =
        updateAndPersist { it.copy(minPText = value, errorMessage = null) }

    fun updateContextSize(value: String) =
        updateAndPersist { it.copy(contextSizeText = value, errorMessage = null) }

    fun updateNumThreads(value: String) =
        updateAndPersist { it.copy(numThreadsText = value, errorMessage = null) }

    fun updateUseMmap(value: Boolean) = updateAndPersist { it.copy(useMmap = value) }

    fun updateUseMlock(value: Boolean) = updateAndPersist { it.copy(useMlock = value) }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun importModel(uri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBusy = true,
                    statusMessage = "Importing GGUF model...",
                    errorMessage = null,
                )
            }
            val result =
                withContext(Dispatchers.IO) {
                    runCatching {
                        require(checkGgufFile(uri)) { "The selected file is not a valid GGUF file." }
                        val fileName = queryDisplayName(uri).ifBlank { "model.gguf" }
                        context.contentResolver.openInputStream(uri).use { inputStream ->
                            requireNotNull(inputStream) { "Unable to read the selected model file." }
                            FileOutputStream(File(context.filesDir, fileName)).use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        val copiedFile = File(context.filesDir, fileName)
                        val ggufReader = GGUFReader()
                        ggufReader.load(copiedFile.absolutePath)
                        val contextSize =
                            ggufReader.getContextSize() ?: SmolLM.DefaultInferenceParams.contextSize
                        val chatTemplate =
                            ggufReader.getChatTemplate() ?: SmolLM.DefaultInferenceParams.chatTemplate
                        appDB.addModel(
                            fileName,
                            "",
                            Paths.get(context.filesDir.absolutePath, fileName).toString(),
                            contextSize.toInt(),
                            chatTemplate,
                        )
                    }
                }
            result.onSuccess {
                refreshModels()
                _uiState.update {
                    it.copy(
                        isBusy = false,
                        statusMessage = "Model imported.",
                        errorMessage = null,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isBusy = false,
                        statusMessage = null,
                        errorMessage = error.message ?: "Failed to import GGUF model.",
                    )
                }
            }
        }
    }

    fun importGoldTsv(uri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBusy = true,
                    statusMessage = "Importing TSV gold file...",
                    errorMessage = null,
                )
            }
            val result =
                withContext(Dispatchers.IO) {
                    runCatching {
                        val fileName = queryDisplayName(uri).ifBlank { "gold.tsv" }
                        require(fileName.endsWith(".tsv", ignoreCase = true)) {
                            "The selected file must have a .tsv extension."
                        }
                        val customAppDir = File(context.filesDir, "custom_app")
                        if (!customAppDir.exists()) {
                            customAppDir.mkdirs()
                        }
                        val copiedFile = File(customAppDir, fileName)
                        context.contentResolver.openInputStream(uri).use { inputStream ->
                            requireNotNull(inputStream) { "Unable to read the selected TSV file." }
                            FileOutputStream(copiedFile).use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        copiedFile.absolutePath to fileName
                    }
                }
            result.onSuccess { (path, fileName) ->
                _uiState.update {
                    it.copy(
                        isBusy = false,
                        statusMessage = "TSV file imported.",
                        selectedTsvPath = path,
                        selectedTsvName = fileName,
                    )
                }
                persistCurrentState()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isBusy = false,
                        statusMessage = null,
                        errorMessage = error.message ?: "Failed to import TSV file.",
                    )
                }
            }
        }
    }

    private fun updateAndPersist(transform: (CustomAppSetupUiState) -> CustomAppSetupUiState) {
        _uiState.update(transform)
        persistCurrentState()
    }

    private fun persistCurrentState() {
        val state = _uiState.value
        sharedPrefStore.put(PREF_SETUP_MODEL_ID, state.selectedModelId)
        sharedPrefStore.put(PREF_SETUP_PROMPT_PRESET_KEY, state.selectedPromptPresetKey)
        sharedPrefStore.put(PREF_SETUP_SYSTEM_PROMPT, state.systemPrompt)
        sharedPrefStore.put(PREF_SETUP_TEMPERATURE, state.temperatureText)
        sharedPrefStore.put(PREF_SETUP_MIN_P, state.minPText)
        sharedPrefStore.put(PREF_SETUP_CONTEXT_SIZE, state.contextSizeText)
        sharedPrefStore.put(PREF_SETUP_NUM_THREADS, state.numThreadsText)
        sharedPrefStore.put(PREF_SETUP_USE_MMAP, state.useMmap)
        sharedPrefStore.put(PREF_SETUP_USE_MLOCK, state.useMlock)
        sharedPrefStore.put(PREF_SETUP_TSV_PATH, state.selectedTsvPath)
        sharedPrefStore.put(PREF_SETUP_TSV_NAME, state.selectedTsvName)
    }

    private fun loadInitialState(): CustomAppSetupUiState {
        val storedPresetKey =
            sharedPrefStore.get(PREF_SETUP_PROMPT_PRESET_KEY, PROMPT_PRESET_CUSTOM)
        val normalizedPresetKey =
            if (promptPresetOptions.any { it.key == storedPresetKey }) storedPresetKey
            else PROMPT_PRESET_CUSTOM
        return CustomAppSetupUiState(
            selectedModelId = sharedPrefStore.get(PREF_SETUP_MODEL_ID, -1L),
            selectedPromptPresetKey = normalizedPresetKey,
            systemPrompt = sharedPrefStore.get(PREF_SETUP_SYSTEM_PROMPT, "You are a helpful assistant."),
            temperatureText = sharedPrefStore.get(PREF_SETUP_TEMPERATURE, "0.8"),
            minPText = sharedPrefStore.get(PREF_SETUP_MIN_P, "0.1"),
            contextSizeText = sharedPrefStore.get(PREF_SETUP_CONTEXT_SIZE, "2048"),
            numThreadsText = sharedPrefStore.get(PREF_SETUP_NUM_THREADS, "4"),
            useMmap = sharedPrefStore.get(PREF_SETUP_USE_MMAP, true),
            useMlock = sharedPrefStore.get(PREF_SETUP_USE_MLOCK, false),
            selectedTsvPath = sharedPrefStore.get(PREF_SETUP_TSV_PATH, ""),
            selectedTsvName = sharedPrefStore.get(PREF_SETUP_TSV_NAME, ""),
        )
    }

    private fun checkGgufFile(uri: Uri): Boolean {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val ggufMagicNumberBytes = ByteArray(4)
            inputStream.read(ggufMagicNumberBytes)
            return ggufMagicNumberBytes.contentEquals(byteArrayOf(71, 71, 85, 70))
        }
        return false
    }

    private fun queryDisplayName(uri: Uri): String {
        var fileName = ""
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                fileName = cursor.getString(nameIndex)
            }
        }
        return fileName
    }
}
