package com.ryinex.kotlin.csv

expect object CsvReadWrite {

    suspend fun open(): CsvFile?

    suspend fun open(name: String, content: String): CsvFile?

    suspend fun save(csvFile: CsvFile)

    suspend fun save(name: String, content: String)
}

suspend fun CsvReadWrite.save(name: String, list: List<List<Any?>>) {
    val content = list.joinToString("\n") { row ->
        row.joinToString(",") { cellItem -> cellItem.stringRepresent() }
    }

    save(name, content)
}

internal fun csvLines(content: String): List<MutableMap<String, String>> {
    val lines = content.split("\n").filter { it.isNotBlank() }
    val leader = lines.firstOrNull()?.let { line(it) } ?: return emptyList()

    val mapped = lines
        .map { line(it).ensureSize(leader.size) }
        .filter { it.isNotEmpty() }
        .map { strings -> mutableMapOf(*strings.mapIndexed { i, string -> "${i + 1}" to string }.toTypedArray()) }

    return mapped
}

internal fun String.ensureEndsWithCsv(): String {
    return if (endsWith(".csv")) this else "$this.csv"
}

private fun line(content: String): List<String> {
    val words = arrayListOf<String>()
    var counter = 0
    var lastSplitIndex = 0

    for (i in content.indices) {
        val char = content[i]
        if (i == content.length - 1) {
            words.add(content.substring(lastSplitIndex, i + 1))
        }

        if (char == '"' && (i == 0 || content[i - 1] != '\\')) {
            counter++
            continue
        }

        if ((char == ',' && counter % 2 == 0)) {
            words.add(content.substring(lastSplitIndex, i))
            lastSplitIndex = i + 1
            counter = 0
        }
    }
    return words.map { it.removeSurrounding("\"") }
}

private fun List<String>.ensureSize(size: Int): List<String> {
    if (size > this.size) return this + List(size - this.size) { "" }
    return this
}

internal fun Any?.stringRepresent(): String {
    return when (this) {
        is Number -> this.toString()
        is Boolean -> this.toString()
        is String -> this.represent()
        else -> "\"$this\""
    }
}

private fun String.represent(): String {
    toLongOrNull()?.let { return it.toString() }
    toDoubleOrNull()?.let { return it.toString() }
    toBooleanStrictOrNull()?.let { return it.toString() }
    return if (!this.contains(",")) this else "\"$this\""
}