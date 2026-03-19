package io.shubham0204.smollmandroid.ui.customapp

object CustomAppRmaPromptRenderer {
    private const val DATA_PLACEHOLDER = "{data}"

    fun render(template: String, record: GoldTsvRecord): String =
        template.replace(
            DATA_PLACEHOLDER,
            buildRmaInputJson(
                conversationHistory = record.conversationHistory,
                query = record.query,
            ),
        )

    private fun buildRmaInputJson(
        conversationHistory: String,
        query: String,
    ): String =
        buildString {
            appendLine("{")
            append("  \"conversation_history\": \"")
            append(escapeJsonString(conversationHistory))
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
