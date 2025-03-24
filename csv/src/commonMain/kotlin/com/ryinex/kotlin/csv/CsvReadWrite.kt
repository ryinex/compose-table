package com.ryinex.kotlin.csv

expect object CsvReadWrite {

    suspend fun open(): CsvFile?

    suspend fun open(name: String, content: String): CsvFile?

    suspend fun save(csvFile: CsvFile)

    suspend fun save(name: String, content: String)
}

@Suppress("UnusedReceiverParameter", "RemoveRedundantQualifierName")
suspend fun CsvReadWrite.save(name: String, list: List<List<Any?>>) {
    val content = list.ensureSize(null).joinToString("\n") { row ->
        row.joinToString(",") { cellItem -> cellItem.stringRepresent() }
    }
    val file = CsvReadWrite.open(name, content) ?: return
    CsvReadWrite.save(file)
}

internal fun csvLines(content: String): List<MutableMap<String, String>> {
    return content
        .split("\n")
        .filter { it.isNotBlank() }
        .map { line(it) }
        .ensureSize("")
        .filter { it.isNotEmpty() }
        .map { strings -> mutableMapOf(*strings.mapIndexed { i, string -> "${i + 1}" to string }.toTypedArray()) }
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
        if (i == content.length - 1 && char != ',') {
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
    return words
}

internal fun <T> List<T>.ensureSize(size: Int, default: T): List<T> {
    if (size > this.size) return this + List(size - this.size) { default }
    return this
}

internal fun <T> List<List<T>>.ensureSize(default: T): List<List<T>> {
    val largest = this.maxOf { it.size }
    return this.map { it.ensureSize(size = largest, default = default) }
}

internal fun Any?.stringRepresent(): String {
    return when (this) {
        is Number -> this.toString()
        is Boolean -> this.toString()
        is String -> this.represent()
        else -> if (this == null) "" else "\"$this\""
    }
}

private fun String.represent(): String {
    toLongOrNull()?.let { return it.toString() }
    toDoubleOrNull()?.let { return it.toString() }
    toBooleanStrictOrNull()?.let { return it.toString() }
    return if (!this.contains(",") || this.doubleQuoted()) this else "\"$this\""
}

private fun String.doubleQuoted(): Boolean {
    return this.startsWith("\"") && this.endsWith("\"")
}