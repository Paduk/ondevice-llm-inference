package io.shubham0204.smollmandroid.data

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.koin.core.annotation.Single

private const val API_METADATA_ASSET_FILE = "api_v3.0.1.jsonl"

@Single
class ApiMetadataAssetStore(
    private val context: Context,
) {
    @Volatile
    private var cachedMetadata: Map<String, JsonObject>? = null

    fun getAll(): Map<String, JsonObject> {
        cachedMetadata?.let { return it }
        synchronized(this) {
            cachedMetadata?.let { return it }
            val loaded = loadFromAssets()
            cachedMetadata = loaded
            return loaded
        }
    }

    fun get(plan: String): JsonObject? = getAll()[plan]

    private fun loadFromAssets(): Map<String, JsonObject> {
        val lines =
            context.assets.open(API_METADATA_ASSET_FILE).bufferedReader(Charsets.UTF_8).use {
                it.readLines()
            }
        require(lines.isNotEmpty()) { "API metadata asset is empty: $API_METADATA_ASSET_FILE" }

        val json = Json { ignoreUnknownKeys = false }
        return lines
            .filter { it.isNotBlank() }
            .associate { line ->
                val jsonObject = json.parseToJsonElement(line).jsonObject
                val plan =
                    jsonObject["plan"]?.toString()?.removePrefix("\"")?.removeSuffix("\"")
                        ?.takeIf { it.isNotBlank() }
                        ?: error("API metadata row is missing a valid plan key.")
                plan to normalize(jsonObject)
            }
    }

    private fun normalize(jsonObject: JsonObject): JsonObject =
        JsonObject(
            jsonObject.filterKeys { key ->
                key != "examples" && key != "returns" && key != "next_turn_plans"
            }
        )
}
