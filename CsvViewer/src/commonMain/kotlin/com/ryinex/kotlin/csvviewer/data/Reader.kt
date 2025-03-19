package com.ryinex.kotlin.csvviewer.data

internal fun csvLines(isFirstHeader: Boolean, content: String): List<MutableMap<String, String>> {
    val lines = content.split("\n").filter { it.isNotBlank() }
    val leader = lines.firstOrNull()?.let { line(it) } ?: return emptyList()

    val mapped =
        lines
            .drop(if (isFirstHeader) 1 else 0)
            .map { line(it).ensureSize(leader.size) }
            .filter { it.isNotEmpty() }
            .map { strings ->
                val map =
                    strings.mapIndexed { index, it ->
                        val header = if (isFirstHeader) leader[index] else "${index + 1}"

                        header to it
                    }
                mutableMapOf(*map.toTypedArray())
            }

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