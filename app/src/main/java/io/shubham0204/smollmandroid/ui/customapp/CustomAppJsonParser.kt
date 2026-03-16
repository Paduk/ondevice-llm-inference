package io.shubham0204.smollmandroid.ui.customapp

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

data class ParsedPrediction(
    val query: String,
    val rewritedQuery: String,
    val generated: String,
    val answer: String,
)

object CustomAppJsonParser {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = false
    }

    fun parse(rawResponse: String): ParsedPrediction {
        val element = json.parseToJsonElement(rawResponse)
        val jsonObject =
            element as? JsonObject
                ?: throw IllegalArgumentException("Model output must be a single JSON object.")

        return ParsedPrediction(
            query = jsonObject.requiredString("query"),
            rewritedQuery = jsonObject.requiredString("rewrited_query"),
            generated = jsonObject.requiredString("generated"),
            answer = jsonObject.requiredString("answer"),
        )
    }

    private fun JsonObject.requiredString(key: String): String {
        val value =
            this[key] as? JsonPrimitive
                ?: throw IllegalArgumentException("Missing or invalid '$key' field.")
        if (!value.isString) {
            throw IllegalArgumentException("Field '$key' must be a string.")
        }
        return value.jsonPrimitive.content
    }
}
