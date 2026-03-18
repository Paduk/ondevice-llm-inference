package io.shubham0204.smollmandroid.data

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import org.koin.core.annotation.Single

private const val API_METADATA_ASSET_FILE = "simple_api.json"

@Single
class ApiMetadataAssetStore(
    private val context: Context,
) {
    @Volatile
    private var cachedSimpleMetadata: Map<String, List<String>>? = null

    fun getAllSimple(): Map<String, List<String>> {
        cachedSimpleMetadata?.let { return it }
        synchronized(this) {
            cachedSimpleMetadata?.let { return it }
            val loaded = loadFromAssets()
            cachedSimpleMetadata = loaded
            return loaded
        }
    }

    fun getSimple(plan: String): List<String>? = getAllSimple()[plan]

    fun getAll(): Map<String, JsonObject> =
        getAllSimple().mapValues { (_, parameters) ->
            buildJsonObject {
                put(
                    "parameters",
                    buildJsonArray {
                        parameters.forEach { add(JsonPrimitive(it)) }
                    },
                )
            }
        }

    fun get(plan: String): JsonObject? = getAll()[plan]

    private fun loadFromAssets(): Map<String, List<String>> {
        val jsonText =
            context.assets.open(API_METADATA_ASSET_FILE).bufferedReader(Charsets.UTF_8).use {
                it.readText()
            }
        require(jsonText.isNotBlank()) { "API metadata asset is empty: $API_METADATA_ASSET_FILE" }

        val json = Json { ignoreUnknownKeys = false }
        val rootObject = json.parseToJsonElement(jsonText).jsonObject
        require(rootObject.isNotEmpty()) { "API metadata asset is empty: $API_METADATA_ASSET_FILE" }

        return rootObject.mapValues { (plan, value) ->
            require(plan.isNotBlank()) { "API metadata contains an empty plan key." }
            value.jsonArray.map { element ->
                element.toString().removePrefix("\"").removeSuffix("\"")
            }
        }
    }
}
