package io.shubham0204.smollmandroid.ui.customapp

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.RotateCcw
import org.koin.androidx.compose.koinViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.shubham0204.smollmandroid.data.ChatMessage
import io.shubham0204.smollmandroid.data.LLMModel
import io.shubham0204.smollmandroid.ui.theme.SmolLMAndroidTheme
import java.io.File

private object CustomAppRoutes {
    const val Setup = "setup"
    const val ChatEvaluate = "chat_evaluate"
    const val RmaEvaluate = "rma_evaluate"
    const val E2eEvaluate = "e2e_evaluate"
}

@Composable
fun CustomAppRoot() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = CustomAppRoutes.Setup,
    ) {
        composable(CustomAppRoutes.Setup) {
            val viewModel: CustomAppSetupViewModel = koinViewModel()
            SetupPlaceholderScreen(
                viewModel = viewModel,
                onContinue = { testType ->
                    when (testType) {
                        TEST_TYPE_RMA -> navController.navigate(CustomAppRoutes.RmaEvaluate)
                        TEST_TYPE_E2E -> navController.navigate(CustomAppRoutes.E2eEvaluate)
                        else -> navController.navigate(CustomAppRoutes.ChatEvaluate)
                    }
                }
            )
        }
        composable(CustomAppRoutes.ChatEvaluate) {
            val viewModel: CustomAppChatViewModel = koinViewModel()
            ChatEvaluatePlaceholderScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(CustomAppRoutes.RmaEvaluate) {
            val viewModel: CustomAppRmaViewModel = koinViewModel()
            RmaEvaluatePlaceholderScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
        composable(CustomAppRoutes.E2eEvaluate) {
            val viewModel: CustomAppE2eViewModel = koinViewModel()
            E2eEvaluateScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetupPlaceholderScreen(
    viewModel: CustomAppSetupViewModel,
    onContinue: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val modelPicker = rememberLauncherForActivityResult(GetContent()) { uri ->
        uri?.let(viewModel::importModel)
    }
    val tsvPicker = rememberLauncherForActivityResult(GetContent()) { uri ->
        uri?.let(viewModel::importGoldTsv)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Ondevice LLM Inference") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionCard(title = "Model") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { modelPicker.launch("*/*") }) {
                        Text("Import GGUF")
                    }
                    TextButton(onClick = viewModel::refreshModels) {
                        Text("Refresh")
                    }
                }
                if (uiState.availableModels.isEmpty()) {
                    Text(
                        text = "No GGUF models imported yet.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    uiState.availableModels.forEach { model ->
                        ModelSelectionRow(
                            model = model,
                            isSelected = model.id == uiState.selectedModelId,
                            onSelect = { viewModel.selectModel(model.id) },
                        )
                    }
                }
                uiState.selectedModel?.let { model ->
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text("Selected model: ${model.name}", fontWeight = FontWeight.SemiBold)
                    Text("Path: ${model.path}", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "Context size from GGUF: ${model.contextSize}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            SectionCard(title = "Prompt And Parameters") {
                Text(
                    text = "Test type",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    testTypeOptions.forEach { option ->
                        PromptPresetButton(
                            label = option.label,
                            selected = uiState.selectedTestType == option.key,
                            onClick = { viewModel.selectTestType(option.key) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Text(
                    text =
                        when (uiState.selectedTestType) {
                            TEST_TYPE_RMA -> "RMA model"
                            TEST_TYPE_E2E -> "E2E pipeline"
                            else -> "Toolcalling model"
                        },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                val evaluatorOptions =
                    when (uiState.selectedTestType) {
                        TEST_TYPE_RMA -> rmaPromptPresetOptions
                        TEST_TYPE_E2E -> e2ePipelineOptions
                        else -> toolcallingPromptPresetOptions
                    }
                evaluatorOptions.chunked(2).forEach { rowOptions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rowOptions.forEach { preset ->
                            PromptPresetButton(
                                label = preset.label,
                                selected = uiState.selectedPromptPresetKey == preset.key,
                                onClick = { viewModel.selectPromptPreset(preset.key) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (rowOptions.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                if (uiState.selectedTestType == TEST_TYPE_E2E) {
                    Text(
                        text = "E2E uses the selected pipeline configuration. Dedicated routing and evaluator wiring will be added in the next task.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else {
                    OutlinedTextField(
                        value = uiState.systemPrompt,
                        onValueChange = viewModel::updateSystemPrompt,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        label = { Text("System prompt") },
                    )
                }
                OutlinedTextField(
                    value = uiState.temperatureText,
                    onValueChange = viewModel::updateTemperature,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Temperature") },
                )
                OutlinedTextField(
                    value = uiState.minPText,
                    onValueChange = viewModel::updateMinP,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("min-p") },
                )
                OutlinedTextField(
                    value = uiState.contextSizeText,
                    onValueChange = viewModel::updateContextSize,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Context size") },
                )
                OutlinedTextField(
                    value = uiState.numThreadsText,
                    onValueChange = viewModel::updateNumThreads,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Number of threads") },
                )
                CheckboxRow(
                    label = "Use mmap",
                    checked = uiState.useMmap,
                    onCheckedChange = viewModel::updateUseMmap,
                )
                CheckboxRow(
                    label = "Use mlock",
                    checked = uiState.useMlock,
                    onCheckedChange = viewModel::updateUseMlock,
                )
            }

            SectionCard(title = "Gold TSV") {
                Button(onClick = { tsvPicker.launch("*/*") }) {
                    Text("Import TSV")
                }
                Text(
                    text =
                        if (uiState.selectedTsvName.isBlank()) {
                            "No TSV selected yet."
                        } else {
                            "Selected TSV: ${uiState.selectedTsvName}"
                        },
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (uiState.selectedTsvPath.isNotBlank()) {
                    Text(
                        text = uiState.selectedTsvPath,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            uiState.statusMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.primary)
            }
            uiState.errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.clickable { viewModel.clearError() },
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onContinue(uiState.selectedTestType) },
                enabled = uiState.canContinue && !uiState.isBusy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    when (uiState.selectedTestType) {
                        TEST_TYPE_RMA -> "Open RMA Flow"
                        TEST_TYPE_E2E -> "Open E2E Flow"
                        else -> "Open Chat Flow"
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun E2eEvaluateScreen(
    viewModel: CustomAppE2eViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("E2E Evaluate") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionCard(title = "Pipeline") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    e2ePipelineOptions.forEach { option ->
                        PromptPresetButton(
                            label = option.label,
                            selected = uiState.selectedPipelineKey == option.key,
                            onClick = { viewModel.selectPipeline(option.key) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                MetricRow(
                    label = "Selected pipeline",
                    value =
                        e2ePipelineOptions.firstOrNull { it.key == uiState.selectedPipelineKey }?.label
                            ?: "N/A",
                )
            }

            SectionCard(title = "Models") {
                Text(
                    text = "RMA model",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                if (uiState.availableRmaModels.isEmpty()) {
                    Text(
                        text = "No imported models matched the selected pipeline family.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                } else {
                    uiState.availableRmaModels.forEach { model ->
                        ModelSelectionRow(
                            model = model,
                            isSelected = model.id == uiState.selectedRmaModelId,
                            onSelect = { viewModel.selectRmaModel(model.id) },
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "Toolcalling model",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                if (uiState.availableToolModels.isEmpty()) {
                    Text(
                        text = "No imported models matched the selected pipeline family.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                } else {
                    uiState.availableToolModels.forEach { model ->
                        ModelSelectionRow(
                            model = model,
                            isSelected = model.id == uiState.selectedToolModelId,
                            onSelect = { viewModel.selectToolModel(model.id) },
                        )
                    }
                }
            }

            SectionCard(title = "Gold TSV") {
                MetricRow(label = "Selected TSV", value = uiState.goldTsvName.ifBlank { "N/A" })
                MetricRow(label = "Loaded records", value = uiState.goldRecords.size.toString())
                uiState.goldTsvLoadError?.let {
                    Text(
                        text = "TSV load failed: $it",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            SectionCard(title = "Batch Run Mode") {
                batchRunModeOptions.forEach { option ->
                    RadioSelectionRow(
                        label = option.label,
                        selected = uiState.selectedBatchRunMode == option.key,
                        onSelect = { viewModel.selectBatchRunMode(option.key) },
                    )
                    Text(
                        text = option.description,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 48.dp, bottom = 4.dp),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = viewModel::startBatchRun,
                        enabled = uiState.canRun,
                    ) {
                        Text("Start E2E Batch")
                    }
                    TextButton(
                        onClick = viewModel::stopBatchRun,
                        enabled = uiState.isBatchRunning,
                    ) {
                        Text("Stop")
                    }
                }
            }

            SectionCard(title = "Latest Outputs") {
                MetricRow(label = "Latest row", value = uiState.batchLatestUniqueIdx ?: "N/A")
                Text(
                    text = "Intermediate rewrite",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = uiState.latestIntermediateRewrite ?: "N/A",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = "Final tool output",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = uiState.latestFinalToolOutput ?: "N/A",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            SectionCard(title = "Final Evaluation") {
                MetricRow(
                    label = "Evaluated samples",
                    value = uiState.evaluationHistory.size.toString(),
                )
                MetricRow(
                    label = "Macro Accuracy",
                    value = uiState.macroAccuracy?.let { "${"%.4f".format(it)}" } ?: "N/A",
                )
                uiState.latestEvaluationResult?.let { result ->
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    MetricRow(label = "Latest row", value = result.uniqueIdx)
                    MetricRow(label = "Correct", value = if (result.isCorrect) "Yes" else "No")
                    Text(
                        text = "Predicted tool call",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = result.predictedAnswer,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = "Gold tool call",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = result.goldAnswer,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                uiState.evaluationErrorMessage?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            SectionCard(title = "Batch Summary") {
                MetricRow(
                    label = "Batch state",
                    value =
                        when {
                            uiState.isBatchRunning -> "Running"
                            uiState.batchCompletedCount > 0 -> "Completed"
                            else -> "Idle"
                        },
                )
                MetricRow(
                    label = "Progress",
                    value = "${uiState.batchCompletedCount}/${uiState.batchTotalCount} (${uiState.batchCompletionPercent}%)",
                )
                MetricRow(label = "Failed rows", value = uiState.batchFailedCount.toString())
                uiState.batchStatusMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            SectionCard(title = "Conversation") {
                if (uiState.conversationMessages.isEmpty()) {
                    Text(
                        text = "No E2E outputs yet.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    uiState.conversationMessages.forEach { message ->
                        MessageCard(message = message)
                    }
                }
                if (uiState.partialResponse.isNotBlank()) {
                    MessageCard(
                        message =
                            ChatMessage(
                                id = Long.MIN_VALUE + 1,
                                chatId = -1L,
                                message = uiState.partialResponse,
                                isUserMessage = false,
                            )
                    )
                }
            }

            uiState.errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RmaEvaluatePlaceholderScreen(
    viewModel: CustomAppRmaViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showMetricDetails by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RMA Evaluate") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionCard(title = "Session") {
                Text(
                    text = "Model: ${uiState.selectedModel?.name ?: "Not selected"}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text =
                        when (uiState.selectedPromptPresetKey) {
                            PROMPT_PRESET_RMA_QWEN3 -> "Preset: Qwen3-RMA"
                            PROMPT_PRESET_RMA_PHI -> "Preset: Phi-RMA"
                            else -> "Preset: ${uiState.selectedPromptPresetKey}"
                        },
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Selected TSV: ${uiState.goldTsvName.ifBlank { "N/A" }}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            SharedRuntimeMetricsSection(
                metrics =
                    SharedRuntimeMetricsState(
                        totalTimeMs = uiState.totalTimeMs,
                        generationSpeedTokensPerSec = uiState.generationSpeedTokensPerSec,
                        promptTokenCount = uiState.promptTokenCount,
                        prefillTimeMs = uiState.prefillTimeMs,
                        generationTimeMs = uiState.generationTimeMs,
                        generatedTokenCount = uiState.generatedTokenCount,
                        prefillSpeedTokensPerSec = uiState.prefillSpeedTokensPerSec,
                        contextLengthUsed = uiState.contextLengthUsed,
                    ),
                showMetricDetails = showMetricDetails,
                onToggleMetricDetails = { showMetricDetails = !showMetricDetails },
            )

            SectionCard(title = "Gold TSV") {
                MetricRow(
                    label = "Loaded records",
                    value = uiState.goldRecords.size.toString(),
                )
                uiState.goldTsvLoadError?.let {
                    Text(
                        text = "TSV load failed: $it",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            SectionCard(title = "RMA Prompt Preview") {
                Text(
                    text = "Prompt rendering follows the Python RMA preprocessing shape.",
                    style = MaterialTheme.typography.bodySmall,
                )
                uiState.renderedRmaPromptPreview?.let { preview ->
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "Rendered prompt preview",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = preview,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                uiState.rmaPromptPreviewError?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            SectionCard(title = "Batch Run Mode") {
                batchRunModeOptions.forEach { option ->
                    RadioSelectionRow(
                        label = option.label,
                        selected = uiState.selectedBatchRunMode == option.key,
                        onSelect = { viewModel.selectBatchRunMode(option.key) },
                    )
                    Text(
                        text = option.description,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 48.dp, bottom = 4.dp),
                    )
                }
                MetricRow(label = "Total rows", value = uiState.batchTotalCount.toString())
                MetricRow(label = "Completed rows", value = uiState.batchCompletedCount.toString())
                MetricRow(label = "Failed rows", value = uiState.batchFailedCount.toString())
                SharedBatchExportActionsSection(
                    context = context,
                    exportState =
                        SharedBatchExportUiState(
                            batchResultFilePath = uiState.batchResultFilePath,
                            batchSummaryFilePath = uiState.batchSummaryFilePath,
                            batchLastFlushCompletedCount = uiState.batchLastFlushCompletedCount,
                            batchIsResumed = uiState.batchIsResumed,
                            isBatchRunning = uiState.isBatchRunning,
                        ),
                    onDeleteSavedResults = viewModel::deleteSavedBatchResults,
                )
                uiState.batchStatusMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = viewModel::startBatchRun,
                        enabled = uiState.goldRecords.isNotEmpty() && !uiState.isBatchRunning,
                    ) {
                        Text("Start RMA Batch")
                    }
                    TextButton(
                        onClick = viewModel::stopBatchRun,
                        enabled = uiState.isBatchRunning,
                    ) {
                        Text("Stop")
                    }
                }
            }

            SectionCard(title = "Evaluation") {
                MetricRow(
                    label = "Exact match accuracy",
                    value = uiState.exactMatchAccuracy?.let { "${"%.4f".format(it)}" } ?: "N/A",
                )
                MetricRow(
                    label = "Evaluated samples",
                    value = uiState.evaluationHistory.size.toString(),
                )
                uiState.latestEvaluationResult?.let { result ->
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    MetricRow(label = "Latest row", value = result.uniqueIdx)
                    MetricRow(label = "Correct", value = if (result.isCorrect) "Yes" else "No")
                    Text(
                        text = "Predicted rewrite",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(text = result.predictedRewrite, style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "Gold rewrite",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(text = result.goldRewrite, style = MaterialTheme.typography.bodySmall)
                }
                uiState.evaluationErrorMessage?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            SectionCard(title = "Batch Summary") {
                MetricRow(
                    label = "Batch state",
                    value =
                        when {
                            uiState.isBatchRunning -> "Running"
                            uiState.batchCompletedCount > 0 -> "Completed"
                            else -> "Idle"
                        },
                )
                MetricRow(
                    label = "Progress",
                    value = "${uiState.batchCompletedCount}/${uiState.batchTotalCount} (${uiState.batchCompletionPercent}%)",
                )
                MetricRow(
                    label = "Success rate",
                    value = uiState.batchSuccessRate?.let { "${"%.2f".format(it * 100)}%" } ?: "N/A",
                )
                MetricRow(
                    label = "Evaluated samples",
                    value = uiState.evaluationHistory.size.toString(),
                )
                MetricRow(
                    label = "Exact match accuracy",
                    value = uiState.exactMatchAccuracy?.let { "${"%.4f".format(it)}" } ?: "N/A",
                )
                if (showMetricDetails) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    MetricRow(
                        label = "Batch total prefill",
                        value = formatMillis(uiState.batchTotalPrefillTimeMs),
                    )
                    MetricRow(
                        label = "Batch total generation",
                        value = formatMillis(uiState.batchTotalGenerationTimeMs),
                    )
                    MetricRow(
                        label = "Batch avg prefill",
                        value = uiState.batchAveragePrefillTimeMs?.let { formatMillis(it) } ?: "N/A",
                    )
                    MetricRow(
                        label = "Batch avg generation",
                        value = uiState.batchAverageGenerationTimeMs?.let { formatMillis(it) } ?: "N/A",
                    )
                    MetricRow(
                        label = "Batch avg gen speed",
                        value =
                            uiState.batchAverageGenerationSpeed?.let { "${"%.2f".format(it)} token/s" }
                                ?: "N/A",
                    )
                    MetricRow(
                        label = "Batch avg prefill speed",
                        value =
                            uiState.batchAveragePrefillSpeed?.let { "${"%.2f".format(it)} token/s" }
                                ?: "N/A",
                    )
                    MetricRow(
                        label = "Batch generated tokens",
                        value = uiState.batchTotalGeneratedTokens.toString(),
                    )
                }
                MetricRow(
                    label = "Latest processed row",
                    value = uiState.batchLatestUniqueIdx ?: "N/A",
                )
                uiState.batchStatusMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                uiState.evaluationErrorMessage?.let {
                    Text(
                        text = "Latest evaluation issue: $it",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            SectionCard(title = "Conversation") {
                if (uiState.conversationMessages.isEmpty()) {
                    Text(
                        text = "No RMA outputs yet.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    uiState.conversationMessages.forEach { message ->
                        MessageCard(message = message)
                    }
                }
                if (uiState.partialResponse.isNotBlank()) {
                    MessageCard(
                        message =
                            ChatMessage(
                                id = Long.MIN_VALUE,
                                chatId = -1L,
                                message = uiState.partialResponse,
                                isUserMessage = false,
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageCard(message: ChatMessage) {
    Card(
        colors = CardDefaults.cardColors(),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = if (message.isUserMessage) "User" else "Assistant",
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = message.message,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatEvaluatePlaceholderScreen(
    viewModel: CustomAppChatViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showMetricDetails by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat And Evaluate") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::clearConversation) {
                        Icon(FeatherIcons.RotateCcw, contentDescription = "Reset conversation")
                    }
                },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionCard(title = "Session") {
                Text(
                    text = "Model: ${uiState.selectedModel?.name ?: "Not loaded"}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text =
                        if (uiState.isModelLoading) {
                            "Loading model..."
                        } else if (uiState.isModelReady) {
                            "Model ready for multi-turn inference."
                        } else {
                            "Model is not ready."
                        },
                    style = MaterialTheme.typography.bodyMedium,
                )
                uiState.chat?.let { chat ->
                    Text(
                        text = "System prompt: ${chat.systemPrompt}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            SharedRuntimeMetricsSection(
                metrics =
                    SharedRuntimeMetricsState(
                        totalTimeMs = uiState.totalTimeMs,
                        generationSpeedTokensPerSec = uiState.generationSpeedTokensPerSec,
                        promptTokenCount = uiState.promptTokenCount,
                        prefillTimeMs = uiState.prefillTimeMs,
                        generationTimeMs = uiState.generationTimeMs,
                        generatedTokenCount = uiState.generatedTokenCount,
                        prefillSpeedTokensPerSec = uiState.prefillSpeedTokensPerSec,
                        contextLengthUsed = uiState.contextLengthUsed,
                    ),
                showMetricDetails = showMetricDetails,
                onToggleMetricDetails = { showMetricDetails = !showMetricDetails },
            )

            SectionCard(title = "Parse Result") {
                val parsedPrediction = uiState.parsedPrediction
                if (parsedPrediction != null) {
                    MetricRow(label = "plan", value = parsedPrediction.plan)
                    Text(
                        text = "arguments",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = parsedPrediction.argumentsAsDisplayString(),
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else if (uiState.parseErrorMessage != null) {
                    Text(
                        text = "Parse failed: ${uiState.parseErrorMessage}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    uiState.latestRawModelOutput?.let { raw ->
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            text = "Latest raw output",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = raw,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    uiState.latestRawModelOutputEscaped?.let { escaped ->
                        Text(
                            text = "Escaped debug view",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = escaped,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                } else {
                    Text(
                        text = "No parsed prediction yet.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            SectionCard(title = "Gold TSV") {
                MetricRow(
                    label = "Selected TSV",
                    value = if (uiState.goldTsvName.isBlank()) "N/A" else uiState.goldTsvName,
                )
                MetricRow(
                    label = "Loaded records",
                    value = uiState.goldRecords.size.toString(),
                )
                if (uiState.goldTsvName.isBlank() && uiState.goldRecords.isEmpty()) {
                    Text(
                        text = "No TSV loaded. Chat can continue, but evaluation is disabled.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                uiState.renderedPromptPreview?.let { preview ->
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "Rendered prompt preview",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = preview,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (uiState.renderedToolsMissingPlans.isNotEmpty()) {
                    Text(
                        text = "Missing tools metadata: ${uiState.renderedToolsMissingPlans.joinToString()}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                uiState.goldTsvLoadError?.let {
                    Text(
                        text = "TSV load failed: $it",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            SectionCard(title = "Tools Diagnostics") {
                MetricRow(
                    label = "Parsed candidates",
                    value = uiState.renderedToolsCandidateCount.toString(),
                )
                MetricRow(
                    label = "Rendered tools",
                    value = uiState.renderedToolsCount.toString(),
                )
                MetricRow(
                    label = "Render status",
                    value =
                        when {
                            uiState.renderedToolsCandidateCount == 0 -> "No candidates"
                            uiState.renderedToolsCount > 0 -> "Rendered"
                            uiState.renderedToolsMissingPlans.isNotEmpty() -> "Warnings"
                            else -> "Pending"
                        },
                )
                MetricRow(
                    label = "Missing plans",
                    value =
                        if (uiState.renderedToolsMissingPlans.isEmpty()) "0"
                        else uiState.renderedToolsMissingPlans.size.toString(),
                )
                if (uiState.renderedToolsMissingPlans.isNotEmpty()) {
                    Text(
                        text = uiState.renderedToolsMissingPlans.joinToString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            SectionCard(title = "Batch Run Mode") {
                batchRunModeOptions.forEach { option ->
                    RadioSelectionRow(
                        label = option.label,
                        selected = uiState.selectedBatchRunMode == option.key,
                        onSelect = { viewModel.selectBatchRunMode(option.key) },
                    )
                    Text(
                        text = option.description,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 48.dp, bottom = 4.dp),
                    )
                }
                MetricRow(
                    label = "Selected mode",
                    value =
                        batchRunModeOptions.firstOrNull {
                            it.key == uiState.selectedBatchRunMode
                        }?.label ?: "Top 1",
                )
                MetricRow(label = "Total rows", value = uiState.batchTotalCount.toString())
                MetricRow(label = "Completed rows", value = uiState.batchCompletedCount.toString())
                MetricRow(label = "Failed rows", value = uiState.batchFailedCount.toString())
                SharedBatchExportActionsSection(
                    context = context,
                    exportState =
                        SharedBatchExportUiState(
                            batchResultFilePath = uiState.batchResultFilePath,
                            batchSummaryFilePath = uiState.batchSummaryFilePath,
                            batchLastFlushCompletedCount = uiState.batchLastFlushCompletedCount,
                            batchIsResumed = uiState.batchIsResumed,
                            batchResumeSkippedCount = uiState.batchResumeSkippedCount,
                            isBatchRunning = uiState.isBatchRunning,
                            isGenerating = uiState.isGenerating,
                        ),
                    onDeleteSavedResults = viewModel::deleteSavedBatchResults,
                )
                MetricRow(
                    label = "Latest row",
                    value = uiState.batchLatestUniqueIdx ?: "N/A",
                )
                uiState.batchStatusMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = viewModel::startBatchRun,
                        enabled =
                            uiState.goldRecords.isNotEmpty() &&
                                !uiState.isBatchRunning &&
                                !uiState.isGenerating &&
                                uiState.isModelReady,
                    ) {
                        Text("Start Batch Test")
                    }
                    TextButton(
                        onClick = viewModel::stopBatchRun,
                        enabled = uiState.isBatchRunning,
                    ) {
                        Text("Stop")
                    }
                }
            }

            SectionCard(title = "Batch Summary") {
                MetricRow(
                    label = "Batch state",
                    value =
                        when {
                            uiState.isBatchRunning -> "Running"
                            uiState.batchCompletedCount > 0 -> "Completed"
                            else -> "Idle"
                        },
                )
                MetricRow(
                    label = "Progress",
                    value = "${uiState.batchCompletedCount}/${uiState.batchTotalCount} (${uiState.batchCompletionPercent}%)",
                )
                MetricRow(
                    label = "Successful rows",
                    value = uiState.batchSuccessCount.toString(),
                )
                MetricRow(
                    label = "Failed rows",
                    value = uiState.batchFailedCount.toString(),
                )
                MetricRow(
                    label = "Success rate",
                    value =
                        uiState.batchSuccessRate?.let { "${"%.2f".format(it * 100)}%" }
                            ?: "N/A",
                )
                MetricRow(
                    label = "Evaluated samples",
                    value = uiState.evaluationHistory.size.toString(),
                )
                MetricRow(
                    label = "Macro Accuracy",
                    value =
                        uiState.macroAccuracy?.let { "${"%.4f".format(it)}" }
                            ?: "N/A",
                )
                if (showMetricDetails) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    MetricRow(
                        label = "Batch total prefill",
                        value = formatMillis(uiState.batchTotalPrefillTimeMs),
                    )
                    MetricRow(
                        label = "Batch total generation",
                        value = formatMillis(uiState.batchTotalGenerationTimeMs),
                    )
                    MetricRow(
                        label = "Batch avg prefill",
                        value = uiState.batchAveragePrefillTimeMs?.let { formatMillis(it) } ?: "N/A",
                    )
                    MetricRow(
                        label = "Batch avg generation",
                        value = uiState.batchAverageGenerationTimeMs?.let { formatMillis(it) } ?: "N/A",
                    )
                    MetricRow(
                        label = "Batch avg gen speed",
                        value =
                            uiState.batchAverageGenerationSpeed?.let { "${"%.2f".format(it)} token/s" }
                                ?: "N/A",
                    )
                    MetricRow(
                        label = "Batch avg prefill speed",
                        value =
                            uiState.batchAveragePrefillSpeed?.let { "${"%.2f".format(it)} token/s" }
                                ?: "N/A",
                    )
                    MetricRow(
                        label = "Batch generated tokens",
                        value = uiState.batchTotalGeneratedTokens.toString(),
                    )
                }
                MetricRow(
                    label = "Latest processed row",
                    value = uiState.batchLatestUniqueIdx ?: "N/A",
                )
                uiState.batchStatusMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                uiState.evaluationErrorMessage?.let {
                    Text(
                        text = "Latest evaluation issue: $it",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            SectionCard(title = "Evaluation") {
                MetricRow(
                    label = "Macro Accuracy",
                    value =
                        uiState.macroAccuracy?.let { "${"%.4f".format(it)}" }
                            ?: "N/A",
                )
                MetricRow(
                    label = "Evaluated samples",
                    value = uiState.evaluationHistory.size.toString(),
                )
                if (uiState.goldRecords.isEmpty()) {
                    Text(
                        text = "Evaluation is disabled until a TSV gold file is loaded.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                uiState.latestEvaluationResult?.let { result ->
                    MetricRow(label = "Latest unique_idx", value = result.uniqueIdx)
                    MetricRow(label = "Gold answer", value = result.goldAnswer)
                    MetricRow(label = "Predicted answer", value = result.predictedAnswer)
                    MetricRow(
                        label = "Latest result",
                        value = if (result.isCorrect) "Correct" else "Incorrect",
                    )
                }
                uiState.evaluationErrorMessage?.let {
                    Text(
                        text = "Evaluation failed: $it",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            SectionCard(title = "Conversation") {
                if (uiState.conversationMessages.isEmpty() && uiState.partialResponse.isBlank()) {
                    Text(
                        text = "No messages yet. Send a prompt to start the session.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        uiState.conversationMessages.forEach { message ->
                            MessageBubble(message = message)
                        }
                        if (uiState.partialResponse.isNotBlank()) {
                            PartialResponseBubble(uiState.partialResponse)
                        }
                    }
                }
            }

            uiState.statusMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.primary)
            }
            uiState.errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            OutlinedTextField(
                value = uiState.inputText,
                onValueChange = viewModel::updateInput,
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                label = { Text("Next user message") },
            )

            Button(
                onClick = viewModel::sendMessage,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.isModelReady && !uiState.isGenerating && uiState.inputText.isNotBlank(),
            ) {
                Text(if (uiState.isGenerating) "Generating..." else "Send")
            }
        }
    }
}

@Composable
private fun PlaceholderContent(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    primaryActionLabel: String,
    onPrimaryAction: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        ) {
            Text(
                text = description,
                modifier = Modifier.padding(20.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Button(onClick = onPrimaryAction) {
            Text(primaryActionLabel)
        }
    }
}

@Composable
private fun PromptPresetButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier,
        ) {
            Text(label)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
        ) {
            Text(label)
        }
    }
}

private fun formatMillis(value: Long): String = "${"%.3f".format(value / 1000f)} s"

private data class SharedRuntimeMetricsState(
    val totalTimeMs: Long?,
    val generationSpeedTokensPerSec: Float?,
    val promptTokenCount: Int?,
    val prefillTimeMs: Long?,
    val generationTimeMs: Long?,
    val generatedTokenCount: Int?,
    val prefillSpeedTokensPerSec: Float?,
    val contextLengthUsed: Int?,
)

private data class SharedBatchExportUiState(
    val batchResultFilePath: String?,
    val batchSummaryFilePath: String?,
    val batchLastFlushCompletedCount: Int,
    val batchIsResumed: Boolean,
    val batchResumeSkippedCount: Int? = null,
    val isBatchRunning: Boolean,
    val isGenerating: Boolean = false,
)

@Composable
private fun SharedRuntimeMetricsSection(
    metrics: SharedRuntimeMetricsState,
    showMetricDetails: Boolean,
    onToggleMetricDetails: () -> Unit,
) {
    SectionCard(title = "Runtime Metrics") {
        MetricRow(
            label = "Total time",
            value = metrics.totalTimeMs?.let { formatMillis(it) } ?: "N/A",
        )
        MetricRow(
            label = "Generation speed",
            value =
                metrics.generationSpeedTokensPerSec?.let { "${"%.2f".format(it)} token/s" }
                    ?: "N/A",
        )
        MetricRow(
            label = "Prompt length",
            value = metrics.promptTokenCount?.toString() ?: "N/A",
        )
        TextButton(onClick = onToggleMetricDetails) {
            Text(if (showMetricDetails) "Hide detailed metrics" else "Show detailed metrics")
        }
        if (showMetricDetails) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            MetricRow(
                label = "Prefill time",
                value = metrics.prefillTimeMs?.let { formatMillis(it) } ?: "N/A",
            )
            MetricRow(
                label = "Generation time",
                value = metrics.generationTimeMs?.let { formatMillis(it) } ?: "N/A",
            )
            MetricRow(
                label = "Generated tokens",
                value = metrics.generatedTokenCount?.toString() ?: "N/A",
            )
            MetricRow(
                label = "Prefill speed",
                value =
                    metrics.prefillSpeedTokensPerSec?.let { "${"%.2f".format(it)} token/s" }
                        ?: "N/A",
            )
            MetricRow(
                label = "Prompt/context length",
                value = metrics.contextLengthUsed?.toString() ?: "N/A",
            )
        }
    }
}

@Composable
private fun SharedBatchExportActionsSection(
    context: Context,
    exportState: SharedBatchExportUiState,
    onDeleteSavedResults: () -> Unit,
) {
    MetricRow(
        label = "Run type",
        value = if (exportState.batchIsResumed) "Resumed" else "Fresh",
    )
    exportState.batchResumeSkippedCount?.takeIf { exportState.batchIsResumed }?.let {
        MetricRow(
            label = "Skipped rows",
            value = it.toString(),
        )
    }
    MetricRow(
        label = "Result file",
        value = exportState.batchResultFilePath?.substringAfterLast('/') ?: "N/A",
    )
    MetricRow(
        label = "Last flush",
        value =
            if (exportState.batchLastFlushCompletedCount > 0) {
                "${exportState.batchLastFlushCompletedCount} rows"
            } else {
                "Not saved yet"
            },
    )
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(
            onClick = {
                exportState.batchResultFilePath?.let {
                    shareExportFile(
                        context = context,
                        path = it,
                        mimeType = "text/tab-separated-values",
                        chooserTitle = "Share batch results TSV",
                    )
                }
            },
            enabled = exportState.batchResultFilePath != null,
        ) {
            Text("Share Results")
        }
        OutlinedButton(
            onClick = {
                exportState.batchSummaryFilePath?.let {
                    shareExportFile(
                        context = context,
                        path = it,
                        mimeType = "application/json",
                        chooserTitle = "Share batch summary JSON",
                    )
                }
            },
            enabled = exportState.batchSummaryFilePath != null,
        ) {
            Text("Share Summary")
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(
            onClick = onDeleteSavedResults,
            enabled =
                !exportState.isBatchRunning &&
                    !exportState.isGenerating &&
                    (exportState.batchResultFilePath != null || exportState.batchSummaryFilePath != null),
        ) {
            Text("Delete Saved Results")
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            content()
        }
    }
}

@Composable
private fun ModelSelectionRow(
    model: LLMModel,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
            ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(model.name, fontWeight = FontWeight.SemiBold)
            Text("Context: ${model.contextSize}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun CheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(
            text = label,
            modifier = Modifier
                .padding(top = 12.dp)
                .clickable { onCheckedChange(!checked) },
        )
    }
}

@Composable
private fun RadioSelectionRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val containerColor =
        if (message.isUserMessage) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = if (message.isUserMessage) "User" else "Assistant",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(text = message.message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun PartialResponseBubble(response: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "Assistant (streaming)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(text = response, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun shareExportFile(
    context: Context,
    path: String,
    mimeType: String,
    chooserTitle: String,
) {
    val file = File(path)
    if (!file.exists()) return
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent =
        Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    context.startActivity(Intent.createChooser(intent, chooserTitle))
}

@Preview
@Composable
private fun PreviewCustomAppRoot() {
    SmolLMAndroidTheme {
        CustomAppRoot()
    }
}
