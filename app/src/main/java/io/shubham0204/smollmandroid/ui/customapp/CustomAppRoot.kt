package io.shubham0204.smollmandroid.ui.customapp

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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

private object CustomAppRoutes {
    const val Setup = "setup"
    const val ChatEvaluate = "chat_evaluate"
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
                onContinue = { navController.navigate(CustomAppRoutes.ChatEvaluate) }
            )
        }
        composable(CustomAppRoutes.ChatEvaluate) {
            val viewModel: CustomAppChatViewModel = koinViewModel()
            ChatEvaluatePlaceholderScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetupPlaceholderScreen(
    viewModel: CustomAppSetupViewModel,
    onContinue: () -> Unit,
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
            TopAppBar(title = { Text("Custom App Setup") })
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
                OutlinedTextField(
                    value = uiState.systemPrompt,
                    onValueChange = viewModel::updateSystemPrompt,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    label = { Text("System prompt") },
                )
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
                onClick = onContinue,
                enabled = uiState.canContinue && !uiState.isBusy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Open Chat Flow")
            }
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

            SectionCard(title = "Runtime Metrics") {
                MetricRow(
                    label = "Generation speed",
                    value =
                        uiState.generationSpeedTokensPerSec?.let { "${"%.2f".format(it)} token/s" }
                            ?: "N/A",
                )
                MetricRow(
                    label = "Generation time",
                    value =
                        uiState.generationTimeSecs?.let { "$it s" }
                            ?: "N/A",
                )
                MetricRow(
                    label = "Prompt/context length",
                    value =
                        uiState.contextLengthUsed?.toString()
                            ?: "N/A",
                )
            }

            SectionCard(title = "Parse Result") {
                val parsedPrediction = uiState.parsedPrediction
                if (parsedPrediction != null) {
                    MetricRow(label = "query", value = parsedPrediction.query)
                    MetricRow(
                        label = "rewrited_query",
                        value = parsedPrediction.rewritedQuery,
                    )
                    MetricRow(label = "generated", value = parsedPrediction.generated)
                    MetricRow(label = "answer", value = parsedPrediction.answer)
                } else if (uiState.parseErrorMessage != null) {
                    Text(
                        text = "Parse failed: ${uiState.parseErrorMessage}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
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
                uiState.goldTsvLoadError?.let {
                    Text(
                        text = "TSV load failed: $it",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
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
                if (uiState.messages.isEmpty() && uiState.partialResponse.isBlank()) {
                    Text(
                        text = "No messages yet. Send a prompt to start the session.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        uiState.messages.forEach { message ->
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

@Preview
@Composable
private fun PreviewCustomAppRoot() {
    SmolLMAndroidTheme {
        CustomAppRoot()
    }
}
