package io.shubham0204.smollmandroid.ui.customapp

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

data class ParsedReadOutput(
    val rewritedQuery: String,
    override val plan: String,
    override val arguments: JsonObject,
) : EvaluatableToolCall {
    fun argumentsAsDisplayString(): String = prettyJson.encodeToString(JsonObject.serializer(), arguments)

    companion object {
        private val prettyJson = Json { prettyPrint = true }
    }
}

object CustomAppReadJsonParser {
    fun parse(rawResponse: String): ParsedReadOutput {
        val jsonObject = CustomAppJsonObjectParser.parse(rawResponse)
        return ParsedReadOutput(
            rewritedQuery = jsonObject.requiredStringField("rewrited_query"),
            plan = jsonObject.requiredStringField("plan"),
            arguments = jsonObject.requiredObjectField("arguments"),
        )
    }
}
