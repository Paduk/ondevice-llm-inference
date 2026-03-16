package io.shubham0204.smollmandroid.ui.customapp

import java.io.File

data class GoldTsvRecord(
    val conversationHistory: String,
    val query: String,
    val rewritedQuery: String,
    val answer: String,
    val uniqueIdx: String,
    val candidates: String,
)

object CustomAppTsvLoader {
    private val requiredColumns =
        listOf(
            "conversation_history",
            "query",
            "rewrited_query",
            "answer",
            "unique_idx",
            "candidates",
        )

    fun load(path: String): List<GoldTsvRecord> {
        require(path.isNotBlank()) { "No TSV file selected." }
        val file = File(path)
        require(file.exists()) { "TSV file does not exist." }

        val lines = file.readLines()
        require(lines.isNotEmpty()) { "TSV file is empty." }

        val header = lines.first().split('\t')
        requiredColumns.forEach { column ->
            require(column in header) { "Missing required TSV column: $column" }
        }

        val columnIndex = header.withIndex().associate { it.value to it.index }
        val seenUniqueIds = mutableSetOf<String>()
        val records = mutableListOf<GoldTsvRecord>()

        lines.drop(1)
            .filter { it.isNotBlank() }
            .forEachIndexed { rowIndex, line ->
                val cells = line.split('\t')
                require(cells.size >= header.size) {
                    "Invalid TSV row ${rowIndex + 2}: column count does not match header."
                }

                fun value(name: String): String = cells[columnIndex.getValue(name)].trim()

                val uniqueIdx = value("unique_idx")
                val answer = value("answer")

                require(uniqueIdx.isNotBlank()) { "TSV row ${rowIndex + 2} has empty unique_idx." }
                require(answer.isNotBlank()) { "TSV row ${rowIndex + 2} has empty answer." }
                require(seenUniqueIds.add(uniqueIdx)) {
                    "Duplicate unique_idx found in TSV: $uniqueIdx"
                }

                records +=
                    GoldTsvRecord(
                        conversationHistory = value("conversation_history"),
                        query = value("query"),
                        rewritedQuery = value("rewrited_query"),
                        answer = answer,
                        uniqueIdx = uniqueIdx,
                        candidates = value("candidates"),
                    )
            }

        return records
    }
}
