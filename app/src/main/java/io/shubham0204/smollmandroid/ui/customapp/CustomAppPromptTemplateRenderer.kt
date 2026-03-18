package io.shubham0204.smollmandroid.ui.customapp

data class PromptRenderResult(
    val prompt: String,
    val missingPlans: List<String> = emptyList(),
    val parsedCandidateCount: Int = 0,
    val renderedToolCount: Int = 0,
)

object CustomAppPromptTemplateRenderer {
    private const val QUERY_PLACEHOLDER = "{query}"
    private const val REWRITED_QUERY_PLACEHOLDER = "{rewrited_query}"
    private const val CONVERSATION_HISTORY_PLACEHOLDER = "{conversation_history}"
    private const val TOOLS_PLACEHOLDER = "{tools}"

    fun render(
        template: String,
        record: GoldTsvRecord,
        apiMetadataByPlan: Map<String, List<String>> = emptyMap(),
    ): PromptRenderResult {
        val parsedCandidates = parseCandidates(record.candidates)
        val uniqueCandidates = parsedCandidates.distinct()
        val missingPlans = uniqueCandidates.filterNot { apiMetadataByPlan.containsKey(it) }
        val renderedTools =
            uniqueCandidates.mapNotNull { plan ->
                apiMetadataByPlan[plan]?.let { parameters -> "$plan: $parameters" }
            }
        val toolsString = renderedTools.joinToString(separator = "\n")

        return PromptRenderResult(
            prompt =
                template
                    .replace(QUERY_PLACEHOLDER, record.query)
                    .replace(REWRITED_QUERY_PLACEHOLDER, record.rewritedQuery)
                    .replace(CONVERSATION_HISTORY_PLACEHOLDER, record.conversationHistory)
                    .replace(TOOLS_PLACEHOLDER, toolsString),
            missingPlans = missingPlans,
            parsedCandidateCount = uniqueCandidates.size,
            renderedToolCount = renderedTools.size,
        )
    }

    internal fun parseCandidates(rawCandidates: String): List<String> {
        val trimmed = rawCandidates.trim()
        if (trimmed.isBlank() || trimmed == "[]") {
            return emptyList()
        }
        require(trimmed.startsWith("[") && trimmed.endsWith("]")) {
            "Candidates must be a Python-style list string."
        }

        return trimmed
            .removePrefix("[")
            .removeSuffix("]")
            .split(',')
            .map { candidate ->
                candidate.trim().removePrefix("'").removeSuffix("'").removePrefix("\"").removeSuffix("\"")
            }
            .filter { it.isNotBlank() }
    }
}
