package io.shubham0204.smollmandroid.ui.customapp

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive

data class ParsedToolCall(
    val plan: String,
    val arguments: JsonObject,
) {
    fun argumentsAsDisplayString(): String = prettyJson.encodeToString(JsonObject.serializer(), arguments)

    companion object {
        private val prettyJson = Json { prettyPrint = true }
    }
}

object CustomAppJsonParser {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = false
    }

    fun parse(rawResponse: String): ParsedToolCall {
        val strictFailure =
            try {
                return parseStrictJson(rawResponse)
            } catch (exception: Exception) {
                exception
            }

        try {
            return parseQuotedObjectFallback(rawResponse)
        } catch (fallbackException: Exception) {
            throw IllegalArgumentException(
                "Model output could not be parsed as JSON or Python-style quoted object. " +
                    "Strict error: ${strictFailure.message}; fallback error: ${fallbackException.message}",
            )
        }
    }

    private fun parseStrictJson(rawResponse: String): ParsedToolCall {
        val element = json.parseToJsonElement(rawResponse)
        val jsonObject =
            element as? JsonObject
                ?: throw IllegalArgumentException("Model output must be a single JSON object.")
        return jsonObject.toParsedToolCall()
    }

    private fun parseQuotedObjectFallback(rawResponse: String): ParsedToolCall {
        val parser = PythonLikeParser(rawResponse)
        val element = parser.parseValue()
        parser.ensureFullyConsumed()
        val jsonObject =
            element as? JsonObject
                ?: throw IllegalArgumentException("Model output must be a single quoted object.")
        return jsonObject.toParsedToolCall()
    }

    private fun JsonObject.toParsedToolCall(): ParsedToolCall {
        val plan = requiredString("plan")
        val arguments =
            this["arguments"] as? JsonObject
                ?: throw IllegalArgumentException("Field 'arguments' must be an object.")
        return ParsedToolCall(
            plan = plan,
            arguments = arguments,
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

    private class PythonLikeParser(
        raw: String,
    ) {
        private val input = raw.trim()
        private var index = 0

        fun parseValue(): JsonElement {
            skipWhitespace()
            if (index >= input.length) {
                throw IllegalArgumentException("Unexpected end of input.")
            }

            return when (val current = input[index]) {
                '{' -> parseObject()
                '[' -> parseArray()
                '\'', '"' -> JsonPrimitive(parseQuotedString())
                't', 'f' -> parseBoolean()
                'n', 'N' -> parseNull()
                '-', in '0'..'9' -> parseNumber()
                else -> throw IllegalArgumentException("Unexpected token '$current'.")
            }
        }

        fun ensureFullyConsumed() {
            skipWhitespace()
            if (index != input.length) {
                throw IllegalArgumentException("Unexpected trailing content after object.")
            }
        }

        private fun parseObject(): JsonObject {
            expect('{')
            skipWhitespace()
            val content = linkedMapOf<String, JsonElement>()
            if (peek() == '}') {
                index++
                return JsonObject(content)
            }

            while (true) {
                skipWhitespace()
                val key = parseQuotedString()
                skipWhitespace()
                expect(':')
                val value = parseValue()
                content[key] = value
                skipWhitespace()
                when (peek()) {
                    ',' -> {
                        index++
                        continue
                    }
                    '}' -> {
                        index++
                        break
                    }
                    else -> throw IllegalArgumentException("Expected ',' or '}' after object entry.")
                }
            }

            return JsonObject(content)
        }

        private fun parseArray(): JsonArray {
            expect('[')
            skipWhitespace()
            if (peek() == ']') {
                index++
                return buildJsonArray {}
            }

            val values = mutableListOf<JsonElement>()
            while (true) {
                values += parseValue()
                skipWhitespace()
                when (peek()) {
                    ',' -> {
                        index++
                        continue
                    }
                    ']' -> {
                        index++
                        break
                    }
                    else -> throw IllegalArgumentException("Expected ',' or ']' after array item.")
                }
            }

            return JsonArray(values)
        }

        private fun parseQuotedString(): String {
            skipWhitespace()
            if (index >= input.length) {
                throw IllegalArgumentException("Unexpected end of quoted string.")
            }

            val quote = input[index]
            if (quote != '"' && quote != '\'') {
                throw IllegalArgumentException("Expected quoted string token.")
            }
            index++

            val builder = StringBuilder()
            while (index < input.length) {
                val current = input[index]
                when {
                    current == '\\' -> {
                        if (index + 1 >= input.length) {
                            throw IllegalArgumentException("Unterminated escape sequence.")
                        }
                        val escaped = input[index + 1]
                        builder.append(
                            when (escaped) {
                                '\\' -> '\\'
                                '\'' -> '\''
                                '"' -> '"'
                                'n' -> '\n'
                                'r' -> '\r'
                                't' -> '\t'
                                'b' -> '\b'
                                'f' -> '\u000C'
                                else -> escaped
                            },
                        )
                        index += 2
                    }
                    current == quote -> {
                        index++
                        return builder.toString()
                    }
                    else -> {
                        builder.append(current)
                        index++
                    }
                }
            }

            throw IllegalArgumentException("Unterminated quoted string token.")
        }

        private fun parseBoolean(): JsonPrimitive {
            return when {
                input.startsWith("true", index) -> {
                    index += 4
                    JsonPrimitive(true)
                }
                input.startsWith("false", index) -> {
                    index += 5
                    JsonPrimitive(false)
                }
                else -> throw IllegalArgumentException("Invalid boolean token.")
            }
        }

        private fun parseNull(): JsonElement {
            return when {
                input.startsWith("null", index) -> {
                    index += 4
                    JsonNull
                }
                input.startsWith("None", index) -> {
                    index += 4
                    JsonNull
                }
                else -> throw IllegalArgumentException("Invalid null token.")
            }
        }

        private fun parseNumber(): JsonPrimitive {
            val start = index
            if (input[index] == '-') {
                index++
            }
            while (index < input.length && input[index].isDigit()) {
                index++
            }
            if (index < input.length && input[index] == '.') {
                index++
                while (index < input.length && input[index].isDigit()) {
                    index++
                }
            }
            if (index < input.length && (input[index] == 'e' || input[index] == 'E')) {
                index++
                if (index < input.length && (input[index] == '+' || input[index] == '-')) {
                    index++
                }
                while (index < input.length && input[index].isDigit()) {
                    index++
                }
            }
            return JsonPrimitive(input.substring(start, index))
        }

        private fun skipWhitespace() {
            while (index < input.length && input[index].isWhitespace()) {
                index++
            }
        }

        private fun expect(expected: Char) {
            skipWhitespace()
            if (peek() != expected) {
                throw IllegalArgumentException("Expected '$expected'.")
            }
            index++
        }

        private fun peek(): Char? = input.getOrNull(index)
    }
}
