package io.shubham0204.smollmandroid.ui.customapp

import io.shubham0204.smollmandroid.data.LLMModel

internal const val BASELINE_GLM_SYSTEM_PROMPT_TEMPLATE =
    "You are a helpful assistant capable of selecting appropriate tools based on " +
        "user queries and generating corresponding parameters. Use information from " +
        "the conversation history when relevant. Only use parameter values that are " +
        "explicitly stated or can be reasonably inferred from the query. If no tool " +
        "matches the query, set the tool to 'None'.\n <|tool|>{tools}<|/tool|>"

internal const val READ_GLM_SYSTEM_PROMPT_TEMPLATE =
    "Given a conversation history, a query, and a list of available tools, " +
        "first write rewrited_query. Use only the dialogue in reference_turn " +
        "from conversation_history together with the query to resolve ambiguous " +
        "pronouns or omitted references. Then, based on the " +
        "rewrited_query, select the most appropriate tool and generate its arguments. " +
        "Only use parameter values that are explicitly stated or can be reasonably " +
        "inferred from the rewrited_query. Return compact JSON only with keys " +
        "\"rewrited_query\", \"plan\", and \"arguments\". Always include all three " +
        "keys. The value of \"arguments\" must always be an object.\n" +
        "<|tool|>{tools}<|/tool|>"

private const val DATA_PLACEHOLDER = "{data}"
private const val TOOLS_PLACEHOLDER = "{tools}"
private const val QUERY_PLACEHOLDER = "{query}"
private const val CONVERSATION_HISTORY_PLACEHOLDER = "{conversation_history}"
private const val REWRITED_QUERY_PLACEHOLDER = "{rewrited_query}"

private val GLM4_STOP_SEQUENCES =
    listOf(
        "<|observation|>",
        "<|system|>",
        "<|user|>",
        "<|assistant|>",
        "<|endoftext|>",
    )

internal sealed interface CustomAppInferencePrompt {
    data class Raw(
        val prompt: String,
    ) : CustomAppInferencePrompt

    data class Structured(
        val systemPrompt: String,
        val userContent: String,
    ) : CustomAppInferencePrompt
}

internal data class ModelAwarePromptRenderResult(
    val preview: String,
    val executionPrompt: CustomAppInferencePrompt,
    val missingPlans: List<String> = emptyList(),
    val parsedCandidateCount: Int = 0,
    val renderedToolCount: Int = 0,
)

internal object CustomAppMainPathPrompting {
    fun isGlmEdgeModel(model: LLMModel?): Boolean {
        val haystack =
            listOf(model?.name.orEmpty(), model?.path.orEmpty(), model?.chatTemplate.orEmpty())
                .joinToString(separator = " ")
                .lowercase()
        return "glm-edge-1.5b" in haystack || "glm-edge-4b" in haystack
    }

    fun isGlmEdge4BModel(model: LLMModel?): Boolean {
        val haystack =
            listOf(model?.name.orEmpty(), model?.path.orEmpty(), model?.chatTemplate.orEmpty())
                .joinToString(separator = " ")
                .lowercase()
        return "glm-edge-4b" in haystack
    }

    fun shouldUseStructuredBaselinePrompt(
        model: LLMModel?,
        presetKey: String,
    ): Boolean = presetKey == PROMPT_PRESET_BASE_QWEN3 && isGlmEdgeModel(model)

    fun shouldUseStructuredReadPrompt(
        model: LLMModel?,
        presetKey: String,
    ): Boolean = presetKey == PROMPT_PRESET_READ_QWEN3 && isGlmEdgeModel(model)

    fun normalizeTemplateForMainPath(
        template: String,
        presetKey: String,
        model: LLMModel?,
    ): String {
        val defaultTemplate =
            when {
                shouldUseStructuredBaselinePrompt(model, presetKey) -> BASELINE_GLM_SYSTEM_PROMPT_TEMPLATE
                shouldUseStructuredReadPrompt(model, presetKey) -> READ_GLM_SYSTEM_PROMPT_TEMPLATE
                else -> return template
            }
        return if (template.isBlank() || looksLikeRawChatTemplate(template)) defaultTemplate else template
    }

    fun renderBaseline(
        model: LLMModel?,
        presetKey: String,
        template: String,
        record: GoldTsvRecord,
        apiMetadataByPlan: Map<String, List<String>>,
    ): ModelAwarePromptRenderResult {
        if (!shouldUseStructuredBaselinePrompt(model, presetKey)) {
            val rawRender =
                CustomAppPromptTemplateRenderer.render(
                    template = template,
                    record = record,
                    apiMetadataByPlan = apiMetadataByPlan,
                )
            return ModelAwarePromptRenderResult(
                preview = rawRender.prompt,
                executionPrompt = CustomAppInferencePrompt.Raw(rawRender.prompt),
                missingPlans = rawRender.missingPlans,
                parsedCandidateCount = rawRender.parsedCandidateCount,
                renderedToolCount = rawRender.renderedToolCount,
            )
        }

        val toolRender = renderTools(record, apiMetadataByPlan)
        val systemPrompt =
            replacePromptPlaceholders(
                template =
                    normalizeTemplateForMainPath(
                        template = template,
                        presetKey = presetKey,
                        model = model,
                    ),
                record = record,
                toolsString = toolRender.toolsString,
                dataPayload = "",
            )
        val userContent =
            "Conversation History: ${record.conversationHistory}\nUser Query: ${record.query}"
        return ModelAwarePromptRenderResult(
            preview = renderStructuredPreview(systemPrompt, userContent),
            executionPrompt =
                CustomAppInferencePrompt.Structured(
                    systemPrompt = systemPrompt,
                    userContent = userContent,
                ),
            missingPlans = toolRender.missingPlans,
            parsedCandidateCount = toolRender.parsedCandidateCount,
            renderedToolCount = toolRender.renderedToolCount,
        )
    }

    fun renderRead(
        model: LLMModel?,
        presetKey: String,
        template: String,
        record: GoldTsvRecord,
        apiMetadataByPlan: Map<String, List<String>>,
    ): ModelAwarePromptRenderResult {
        if (!shouldUseStructuredReadPrompt(model, presetKey)) {
            val rawRender =
                CustomAppReadPromptRenderer.render(
                    template = template,
                    record = record,
                    apiMetadataByPlan = apiMetadataByPlan,
                )
            return ModelAwarePromptRenderResult(
                preview = rawRender.prompt,
                executionPrompt = CustomAppInferencePrompt.Raw(rawRender.prompt),
                missingPlans = rawRender.missingPlans,
                parsedCandidateCount = rawRender.parsedCandidateCount,
                renderedToolCount = rawRender.renderedToolCount,
            )
        }

        val toolRender = renderTools(record, apiMetadataByPlan)
        val dataPayload =
            buildReadInputJson(
                conversationHistory = record.conversationHistory,
                referenceTurn = record.referenceTurn,
                query = record.query,
            )
        val systemPrompt =
            replacePromptPlaceholders(
                template =
                    normalizeTemplateForMainPath(
                        template = template,
                        presetKey = presetKey,
                        model = model,
                    ),
                record = record,
                toolsString = toolRender.toolsString,
                dataPayload = dataPayload,
            )
        return ModelAwarePromptRenderResult(
            preview = renderStructuredPreview(systemPrompt, dataPayload),
            executionPrompt =
                CustomAppInferencePrompt.Structured(
                    systemPrompt = systemPrompt,
                    userContent = dataPayload,
                ),
            missingPlans = toolRender.missingPlans,
            parsedCandidateCount = toolRender.parsedCandidateCount,
            renderedToolCount = toolRender.renderedToolCount,
        )
    }

    fun trimStopMarkers(
        text: String,
        model: LLMModel?,
    ): String {
        if (!isGlmEdge4BModel(model)) {
            return text.trim()
        }

        val cutoff =
            GLM4_STOP_SEQUENCES
                .mapNotNull { marker ->
                    text.indexOf(marker).takeIf { it >= 0 }
                }.minOrNull()

        return if (cutoff == null) text.trim() else text.substring(0, cutoff).trim()
    }

    private fun renderStructuredPreview(
        systemPrompt: String,
        userContent: String,
    ): String =
        buildString {
            appendLine("System:")
            appendLine(systemPrompt)
            appendLine()
            appendLine("User:")
            appendLine(userContent)
            appendLine()
            append("Assistant:")
        }

    private data class ToolRenderResult(
        val toolsString: String,
        val missingPlans: List<String>,
        val parsedCandidateCount: Int,
        val renderedToolCount: Int,
    )

    private fun renderTools(
        record: GoldTsvRecord,
        apiMetadataByPlan: Map<String, List<String>>,
    ): ToolRenderResult {
        val parsedCandidates = CustomAppPromptTemplateRenderer.parseCandidates(record.candidates)
        val uniqueCandidates = parsedCandidates.distinct()
        val missingPlans = uniqueCandidates.filterNot { apiMetadataByPlan.containsKey(it) }
        val renderedTools =
            uniqueCandidates.mapNotNull { plan ->
                apiMetadataByPlan[plan]?.let { parameters -> "$plan: $parameters" }
            }
        return ToolRenderResult(
            toolsString = renderedTools.joinToString(separator = "\n"),
            missingPlans = missingPlans,
            parsedCandidateCount = uniqueCandidates.size,
            renderedToolCount = renderedTools.size,
        )
    }

    private fun replacePromptPlaceholders(
        template: String,
        record: GoldTsvRecord,
        toolsString: String,
        dataPayload: String,
    ): String =
        template
            .replace(DATA_PLACEHOLDER, dataPayload)
            .replace(TOOLS_PLACEHOLDER, toolsString)
            .replace(QUERY_PLACEHOLDER, record.query)
            .replace(CONVERSATION_HISTORY_PLACEHOLDER, record.conversationHistory)
            .replace(REWRITED_QUERY_PLACEHOLDER, record.rewritedQuery)

    private fun looksLikeRawChatTemplate(template: String): Boolean =
        template.contains("<|im_start|>") || template.contains("<|im_end|>")

    private fun buildReadInputJson(
        conversationHistory: String,
        referenceTurn: String,
        query: String,
    ): String =
        buildString {
            appendLine("{")
            append("  \"conversation_history\": \"")
            append(escapeJsonString(conversationHistory))
            appendLine("\",")
            append("  \"reference_turn\": \"")
            append(escapeJsonString(referenceTurn))
            appendLine("\",")
            append("  \"query\": \"")
            append(escapeJsonString(query))
            appendLine("\"")
            append("}")
        }

    private fun escapeJsonString(value: String): String =
        buildString {
            value.forEach { ch ->
                when (ch) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    '\b' -> append("\\b")
                    '\u000C' -> append("\\f")
                    else ->
                        if (ch.code < 0x20) {
                            append("\\u%04x".format(ch.code))
                        } else {
                            append(ch)
                        }
                }
            }
        }
}
