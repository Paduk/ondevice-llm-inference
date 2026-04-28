package io.shubham0204.smollmandroid.ui.customapp

object CustomAppReadPromptRenderer {
    private const val DATA_PLACEHOLDER = "{data}"
    private const val TOOLS_PLACEHOLDER = "{tools}"
    private const val QUERY_PLACEHOLDER = "{query}"
    private const val CONVERSATION_HISTORY_PLACEHOLDER = "{conversation_history}"
    private const val REWRITED_QUERY_PLACEHOLDER = "{rewrited_query}"

    fun render(
        template: String,
        record: GoldTsvRecord,
        apiMetadataByPlan: Map<String, List<String>> = emptyMap(),
    ): PromptRenderResult {
        val parsedCandidates = CustomAppPromptTemplateRenderer.parseCandidates(record.candidates)
        val uniqueCandidates = parsedCandidates.distinct()
        val missingPlans = uniqueCandidates.filterNot { apiMetadataByPlan.containsKey(it) }
        val renderedTools =
            uniqueCandidates.mapNotNull { plan ->
                apiMetadataByPlan[plan]?.let { parameters -> "$plan: $parameters" }
            }
        val toolsString = renderedTools.joinToString(separator = "\n")
        val dataPayload =
            buildReadInputJson(
                conversationHistory = record.conversationHistory,
                referenceTurn = record.referenceTurn,
                query = record.query,
            )

        return PromptRenderResult(
            prompt =
                template
                    .replace(DATA_PLACEHOLDER, dataPayload)
                    .replace(TOOLS_PLACEHOLDER, toolsString)
                    .replace(QUERY_PLACEHOLDER, record.query)
                    .replace(CONVERSATION_HISTORY_PLACEHOLDER, record.conversationHistory)
                    .replace(REWRITED_QUERY_PLACEHOLDER, record.rewritedQuery),
            missingPlans = missingPlans,
            parsedCandidateCount = uniqueCandidates.size,
            renderedToolCount = renderedTools.size,
        )
    }

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
