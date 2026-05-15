package io.shubham0204.smollmandroid.ui.customapp

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.shubham0204.smollm.GGUFReader
import io.shubham0204.smollm.SmolLM
import io.shubham0204.smollmandroid.data.ApiMetadataAssetStore
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
private const val PREF_SETUP_TEST_TYPE = "custom_app.setup.test_type"
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
private const val DEFAULT_GOLD_TSV_ASSET_NAME = "tc.tsv"
private const val DEFAULT_GOLD_TSV_FILE_NAME = "tc.tsv"

private const val LEGACY_PROMPT_PRESET_RMA_QWEN3 = "rma_qwen3"
private const val LEGACY_PROMPT_PRESET_RMA_PHI = "rma_phi"
private const val LEGACY_PROMPT_PRESET_E2E_QWEN3_PIPELINE = "e2e_qwen3_pipeline"
private const val LEGACY_PROMPT_PRESET_E2E_PHI_PIPELINE = "e2e_phi_pipeline"
private const val LEGACY_PROMPT_PRESET_REWRITE_PHI = "rewrite_phi"
private const val LEGACY_PROMPT_PRESET_BASE_PHI = "base_phi"
private const val LEGACY_PROMPT_PRESET_READ_PHI = "read_phi"
private const val LEGACY_TEST_TYPE_RMA = "rma"
private const val LEGACY_TEST_TYPE_E2E = "e2e"

const val PROMPT_PRESET_REWRITE_QWEN3 = "rewrite_qwen3"
const val PROMPT_PRESET_BASE_QWEN3 = "base_qwen3"
const val PROMPT_PRESET_BASE2_QWEN3 = "base2_qwen3"
const val PROMPT_PRESET_READ_QWEN3 = "read_qwen3"
const val PROMPT_PRESET_READ2_QWEN3 = "read2_qwen3"

const val TEST_TYPE_TOOLCALLING = "toolcalling"
const val TEST_TYPE_TOOLCALLING_2 = "toolcalling2"
const val TEST_TYPE_READ = "read"
const val TEST_TYPE_READ_2 = "read2"

private val rewriteQwen3PromptTemplate =
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

private val baseQwen3PromptTemplate =
    """
    <|im_start|>system
    You are a helpful assistant capable of selecting appropriate tools based on user queries and generating corresponding parameters. Use information from the conversation history when relevant. Only use parameter values that are explicitly stated or can be reasonably inferred from the query. If no tool matches the query, set the tool to 'None'.
     <|tool|>{tools}<|/tool|><|im_end|>
    <|im_start|>user
    Conversation History: {conversation_history}
    User Query: {query}<|im_end|>
    <|im_start|>assistant
    <think>

    </think>

    """.trimIndent()

private val base2Qwen3PromptTemplate =
    """
    <|im_start|>system
    You are a planner for a mobile assistant. Predict the next action plan and its arguments from the conversation history and the current user query. Use the conversation history only when it is relevant for resolving references. Return exactly one JSON object with keys "plan" and "arguments". "plan" is the action/tool name to execute, and "arguments" is an object. Only use argument values that are explicitly stated or can be reasonably inferred from the query or conversation history. If no action is appropriate, return {"plan":"None","arguments":{}}.
    <|im_end|>
    <|im_start|>user
    Conversation History: {conversation_history}
    User Query: {query}<|im_end|>
    <|im_start|>assistant
    <think>

    </think>

    """.trimIndent()

private val legacyReadQwen3PromptTemplate =
    """
    <|im_start|>system
    Given a conversation history, a query, and a list of available tools, first write rewrited_query. Use only the provided conversation_history together with the query to resolve ambiguous pronouns or omitted references. Then, based on the rewrited_query, select the most appropriate tool and generate its arguments. Only use parameter values that are explicitly stated or can be reasonably inferred from the rewrited_query. Return compact JSON only with keys "rewrited_query", "plan", and "arguments". Always include all three keys. The value of "arguments" must always be an object.
    <|im_end|>
    <|im_start|>user
    Tools:
    {tools}
    {data}<|im_end|>
    <|im_start|>assistant
    """.trimIndent()

private val readQwen3PromptTemplate =
    """
    <|im_start|>system
    Given a conversation history, a query, and a list of available tools, first write rewrited_query. Use only the dialogue in reference_turn from conversation_history together with the query to resolve ambiguous pronouns or omitted references. Then, based on the rewrited_query, select the most appropriate tool and generate its arguments. Only use parameter values that are explicitly stated or can be reasonably inferred from the rewrited_query. Return compact JSON only with keys "rewrited_query", "plan", and "arguments". Always include all three keys. The value of "arguments" must always be an object.
     <|tool|>{tools}<|/tool|><|im_end|>
    <|im_start|>user
    {data}<|im_end|>
    <|im_start|>assistant
    <think>

    </think>

    """.trimIndent()

private val read2Qwen3PromptTemplate =
    """
    <|im_start|>system
    Given a conversation history and a query, first write rewrited_query. Use only the dialogue in reference_turn from conversation_history together with the query to resolve ambiguous pronouns or omitted references. Then, based on the rewrited_query, predict the next action plan and generate its arguments. Only use parameter values that are explicitly stated or can be reasonably inferred from the rewrited_query. Return compact JSON only with keys "rewrited_query", "plan", and "arguments". Always include all three keys. The value of "arguments" must always be an object.
    <|im_end|>
    <|im_start|>user
    {data}<|im_end|>
    <|im_start|>assistant
    <think>

    </think>

    """.trimIndent()

data class TestTypeOption(
    val key: String,
    val label: String,
)

data class EvaluatorOption(
    val key: String,
    val label: String,
    val template: String?,
)

val testTypeOptions =
    listOf(
        TestTypeOption(TEST_TYPE_TOOLCALLING, "Baseline"),
        TestTypeOption(TEST_TYPE_TOOLCALLING_2, "Baseline2"),
        TestTypeOption(TEST_TYPE_READ, "READ"),
        TestTypeOption(TEST_TYPE_READ_2, "READ2"),
    )

val toolcallingPromptPresetOptions =
    listOf(
        EvaluatorOption(
            key = PROMPT_PRESET_BASE_QWEN3,
            label = "Baseline-Qwen",
            template = baseQwen3PromptTemplate,
        ),
        EvaluatorOption(
            key = PROMPT_PRESET_BASE2_QWEN3,
            label = "Baseline2-Qwen",
            template = base2Qwen3PromptTemplate,
        ),
    )

val readPromptPresetOptions =
    listOf(
        EvaluatorOption(
            key = PROMPT_PRESET_READ_QWEN3,
            label = "READ-Qwen",
            template = readQwen3PromptTemplate,
        ),
        EvaluatorOption(
            key = PROMPT_PRESET_READ2_QWEN3,
            label = "READ2-Qwen",
            template = read2Qwen3PromptTemplate,
        ),
    )

private fun evaluatorOptionsForTestType(testType: String): List<EvaluatorOption> =
    when (testType) {
        TEST_TYPE_READ -> readPromptPresetOptions.filter { it.key == PROMPT_PRESET_READ_QWEN3 }
        TEST_TYPE_READ_2 -> readPromptPresetOptions.filter { it.key == PROMPT_PRESET_READ2_QWEN3 }
        TEST_TYPE_TOOLCALLING_2 ->
            toolcallingPromptPresetOptions.filter { it.key == PROMPT_PRESET_BASE2_QWEN3 }
        else -> toolcallingPromptPresetOptions.filter { it.key == PROMPT_PRESET_BASE_QWEN3 }
    }

private fun defaultEvaluatorOptionKeyForTestType(testType: String): String =
    when (testType) {
        TEST_TYPE_READ -> PROMPT_PRESET_READ_QWEN3
        TEST_TYPE_READ_2 -> PROMPT_PRESET_READ2_QWEN3
        TEST_TYPE_TOOLCALLING_2 -> PROMPT_PRESET_BASE2_QWEN3
        else -> PROMPT_PRESET_BASE_QWEN3
    }

private fun defaultTemplateForOption(optionKey: String): String =
    defaultTemplateForOption(optionKey, null)

private fun defaultTemplateForOption(
    optionKey: String,
    selectedModel: LLMModel?,
): String =
    when (optionKey) {
        PROMPT_PRESET_BASE_QWEN3 ->
            if (CustomAppMainPathPrompting.shouldUseStructuredBaselinePrompt(selectedModel, optionKey)) {
                BASELINE_GLM_SYSTEM_PROMPT_TEMPLATE
            } else {
                baseQwen3PromptTemplate
            }

        PROMPT_PRESET_READ_QWEN3 ->
            if (CustomAppMainPathPrompting.shouldUseStructuredReadPrompt(selectedModel, optionKey)) {
                READ_GLM_SYSTEM_PROMPT_TEMPLATE
            } else {
                readQwen3PromptTemplate
            }

        PROMPT_PRESET_BASE2_QWEN3 -> base2Qwen3PromptTemplate
        PROMPT_PRESET_READ2_QWEN3 -> read2Qwen3PromptTemplate
        else -> baseQwen3PromptTemplate
    }

private fun knownDefaultTemplatesForOption(optionKey: String): Set<String> =
    when (optionKey) {
        PROMPT_PRESET_BASE_QWEN3 ->
            setOf(baseQwen3PromptTemplate, BASELINE_GLM_SYSTEM_PROMPT_TEMPLATE)

        PROMPT_PRESET_READ_QWEN3 ->
            setOf(readQwen3PromptTemplate, legacyReadQwen3PromptTemplate, READ_GLM_SYSTEM_PROMPT_TEMPLATE)

        PROMPT_PRESET_BASE2_QWEN3 -> setOf(base2Qwen3PromptTemplate)
        PROMPT_PRESET_READ2_QWEN3 -> setOf(read2Qwen3PromptTemplate)
        else -> emptySet()
    }

private fun adjustedTemplateForModel(
    optionKey: String,
    currentTemplate: String,
    selectedModel: LLMModel?,
): String {
    val defaultTemplate = defaultTemplateForOption(optionKey, selectedModel)
    return if (currentTemplate in knownDefaultTemplatesForOption(optionKey)) {
        defaultTemplate
    } else {
        currentTemplate
    }
}

private fun shouldReplaceStoredPrompt(
    presetKey: String,
    storedPrompt: String,
): Boolean =
    when (presetKey) {
        PROMPT_PRESET_READ_QWEN3 -> storedPrompt == legacyReadQwen3PromptTemplate
        else -> false
    }

private fun normalizeStoredTestType(storedTestType: String): String =
    when (storedTestType) {
        TEST_TYPE_TOOLCALLING,
        TEST_TYPE_TOOLCALLING_2,
        TEST_TYPE_READ,
        TEST_TYPE_READ_2,
        -> storedTestType
        LEGACY_TEST_TYPE_RMA,
        LEGACY_TEST_TYPE_E2E,
        -> TEST_TYPE_READ
        else -> TEST_TYPE_TOOLCALLING
    }

private fun normalizeStoredPresetKey(storedPresetKey: String): String =
    when (storedPresetKey) {
        LEGACY_PROMPT_PRESET_RMA_QWEN3,
        LEGACY_PROMPT_PRESET_E2E_QWEN3_PIPELINE,
        -> PROMPT_PRESET_READ_QWEN3
        LEGACY_PROMPT_PRESET_RMA_PHI,
        LEGACY_PROMPT_PRESET_E2E_PHI_PIPELINE,
        LEGACY_PROMPT_PRESET_READ_PHI,
        -> PROMPT_PRESET_READ_QWEN3
        PROMPT_PRESET_REWRITE_QWEN3,
        LEGACY_PROMPT_PRESET_REWRITE_PHI,
        -> PROMPT_PRESET_BASE_QWEN3
        LEGACY_PROMPT_PRESET_BASE_PHI,
        -> PROMPT_PRESET_BASE_QWEN3
        else -> storedPresetKey
    }

private fun isReadTestType(testType: String): Boolean =
    testType == TEST_TYPE_READ || testType == TEST_TYPE_READ_2

data class CustomAppSetupUiState(
    val availableModels: List<LLMModel> = emptyList(),
    val selectedModelId: Long = -1L,
    val selectedModel: LLMModel? = null,
    val selectedTestType: String = TEST_TYPE_TOOLCALLING,
    val selectedPromptPresetKey: String = PROMPT_PRESET_BASE_QWEN3,
    val systemPrompt: String = baseQwen3PromptTemplate,
    val temperatureText: String = "0.0",
    val minPText: String = "0.1",
    val contextSizeText: String = "2048",
    val numThreadsText: String = "4",
    val useMmap: Boolean = true,
    val useMlock: Boolean = false,
    val selectedTsvPath: String = "",
    val selectedTsvName: String = "",
    val renderedReadPromptPreview: String? = null,
    val readPromptPreviewError: String? = null,
    val isBusy: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
) {
    val canContinue: Boolean
        get() = errorMessage == null && selectedModel != null
}

@KoinViewModel
class CustomAppSetupViewModel(
    private val context: Context,
    private val appDB: AppDB,
    private val sharedPrefStore: SharedPrefStore,
    private val apiMetadataAssetStore: ApiMetadataAssetStore,
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
                    val adjustedTemplate =
                        adjustedTemplateForModel(
                            optionKey = state.selectedPromptPresetKey,
                            currentTemplate = state.systemPrompt,
                            selectedModel = selectedModel,
                        )
                    state.copy(
                        availableModels = models,
                        selectedModelId = selectedModel?.id ?: -1L,
                        selectedModel = selectedModel,
                        systemPrompt = adjustedTemplate,
                        contextSizeText =
                            if (state.contextSizeText.isBlank() && selectedModel != null) {
                                selectedModel.contextSize.toString()
                            } else {
                                state.contextSizeText
                            },
                    )
                }
                refreshReadPromptPreview()
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
                systemPrompt =
                    adjustedTemplateForModel(
                        optionKey = state.selectedPromptPresetKey,
                        currentTemplate = state.systemPrompt,
                        selectedModel = selectedModel,
                    ),
                contextSizeText = selectedModel?.contextSize?.toString() ?: state.contextSizeText,
                errorMessage = null,
            )
        }
        refreshReadPromptPreview()
        persistCurrentState()
    }

    fun updateSystemPrompt(value: String) = updateAndPersist { it.copy(systemPrompt = value) }

    fun selectTestType(testType: String) {
        val defaultOptionKey = defaultEvaluatorOptionKeyForTestType(testType)
        val defaultTemplate =
            defaultTemplateForOption(defaultOptionKey, _uiState.value.selectedModel)
        updateAndPersist { state ->
            state.copy(
                selectedTestType = testType,
                selectedPromptPresetKey = defaultOptionKey,
                systemPrompt = defaultTemplate,
            )
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
                refreshReadPromptPreview()
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
        refreshReadPromptPreview()
        persistCurrentState()
    }

    private fun refreshReadPromptPreview() {
        val state = _uiState.value
        if (!isReadTestType(state.selectedTestType)) {
            _uiState.update { it.copy(renderedReadPromptPreview = null, readPromptPreviewError = null) }
            return
        }

        val template = state.systemPrompt
        val tsvPath = state.selectedTsvPath
        if (template.isBlank()) {
            _uiState.update {
                it.copy(
                    renderedReadPromptPreview = null,
                    readPromptPreviewError = "No READ prompt template is selected.",
                )
            }
            return
        }
        if (tsvPath.isBlank()) {
            _uiState.update {
                it.copy(
                    renderedReadPromptPreview = null,
                    readPromptPreviewError = "Import a TSV file to render a READ preview.",
                )
            }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val previewResult =
                runCatching {
                    val firstRecord =
                        CustomAppTsvLoader.load(tsvPath).firstOrNull()
                            ?: error("TSV file has no usable rows.")
                    val apiMetadataByPlan = apiMetadataAssetStore.getAllSimple()
                    when (state.selectedTestType) {
                        TEST_TYPE_READ ->
                            CustomAppMainPathPrompting.renderRead(
                                model = state.selectedModel,
                                presetKey = state.selectedPromptPresetKey,
                                template = template,
                                record = firstRecord,
                                apiMetadataByPlan = apiMetadataByPlan,
                            ).preview

                        else ->
                            CustomAppReadPromptRenderer.render(
                                template = template,
                                record = firstRecord,
                                apiMetadataByPlan = apiMetadataByPlan,
                            ).prompt
                    }
                }
            withContext(Dispatchers.Main) {
                previewResult.onSuccess { preview ->
                    _uiState.update {
                        it.copy(
                            renderedReadPromptPreview = preview,
                            readPromptPreviewError = null,
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            renderedReadPromptPreview = null,
                            readPromptPreviewError = error.message ?: "Failed to render READ prompt preview.",
                        )
                    }
                }
            }
        }
    }

    private fun persistCurrentState() {
        val state = _uiState.value
        sharedPrefStore.put(PREF_SETUP_MODEL_ID, state.selectedModelId)
        sharedPrefStore.put(PREF_SETUP_TEST_TYPE, state.selectedTestType)
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
        val (initialTsvPath, initialTsvName) = resolveInitialTsvSelection()
        val storedTestType = sharedPrefStore.get(PREF_SETUP_TEST_TYPE, TEST_TYPE_TOOLCALLING)
        val normalizedTestType = normalizeStoredTestType(storedTestType)
        val storedPresetKey =
            sharedPrefStore.get(PREF_SETUP_PROMPT_PRESET_KEY, defaultEvaluatorOptionKeyForTestType(normalizedTestType))
        val normalizedStoredPresetKey = normalizeStoredPresetKey(storedPresetKey)
        val availableOptions = evaluatorOptionsForTestType(normalizedTestType)
        val normalizedPresetKey =
            if (availableOptions.any { it.key == normalizedStoredPresetKey }) {
                normalizedStoredPresetKey
            } else {
                defaultEvaluatorOptionKeyForTestType(normalizedTestType)
            }
        val defaultPromptTemplate =
            availableOptions.firstOrNull { it.key == normalizedPresetKey }?.template
                ?: defaultTemplateForOption(normalizedPresetKey)
        val didMigrate = normalizedTestType != storedTestType || normalizedPresetKey != storedPresetKey
        val storedSystemPrompt = sharedPrefStore.get(PREF_SETUP_SYSTEM_PROMPT, defaultPromptTemplate)
        val shouldRefreshStoredPrompt = shouldReplaceStoredPrompt(normalizedPresetKey, storedSystemPrompt)
        return CustomAppSetupUiState(
            selectedModelId = sharedPrefStore.get(PREF_SETUP_MODEL_ID, -1L),
            selectedTestType = normalizedTestType,
            selectedPromptPresetKey = normalizedPresetKey,
            systemPrompt =
                if (didMigrate || shouldRefreshStoredPrompt) {
                    defaultPromptTemplate
                } else {
                    storedSystemPrompt
                },
            temperatureText = sharedPrefStore.get(PREF_SETUP_TEMPERATURE, "0.0"),
            minPText = sharedPrefStore.get(PREF_SETUP_MIN_P, "0.1"),
            contextSizeText = sharedPrefStore.get(PREF_SETUP_CONTEXT_SIZE, "2048"),
            numThreadsText = sharedPrefStore.get(PREF_SETUP_NUM_THREADS, "4"),
            useMmap = sharedPrefStore.get(PREF_SETUP_USE_MMAP, true),
            useMlock = sharedPrefStore.get(PREF_SETUP_USE_MLOCK, false),
            selectedTsvPath = initialTsvPath,
            selectedTsvName = initialTsvName,
        )
    }

    private fun resolveInitialTsvSelection(): Pair<String, String> {
        val storedPath = sharedPrefStore.get(PREF_SETUP_TSV_PATH, "")
        val storedName = sharedPrefStore.get(PREF_SETUP_TSV_NAME, "")
        if (storedPath.isNotBlank()) {
            val storedFile = File(storedPath)
            if (storedFile.exists()) {
                return storedFile.absolutePath to storedName.ifBlank { storedFile.name }
            }
        }
        return ensureBundledDefaultGoldTsv()
    }

    private fun ensureBundledDefaultGoldTsv(): Pair<String, String> {
        val customAppDir = File(context.filesDir, "custom_app")
        if (!customAppDir.exists()) {
            customAppDir.mkdirs()
        }
        val bundledFile = File(customAppDir, DEFAULT_GOLD_TSV_FILE_NAME)
        if (!bundledFile.exists()) {
            context.assets.open(DEFAULT_GOLD_TSV_ASSET_NAME).use { input ->
                FileOutputStream(bundledFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
        return bundledFile.absolutePath to DEFAULT_GOLD_TSV_FILE_NAME
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
