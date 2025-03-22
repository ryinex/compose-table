package com.ryinex.kotlin.csv

internal data class CsvFile(
    var name: String,
    val isFirstHeader: Boolean,
    val content: List<MutableMap<String, String>>
) {
    fun raw(): String {
        val headers = if (isFirstHeader) content.first().keys.toList() else emptyList()
        val rows =
            content
                .filter { it.values.any { it.isNotBlank() } }
                .map { it.values.toList() }
        val list = listOf(headers) + rows

        return list.joinToString("\n") { it.map { "\"$it\"" }.joinToString(",") }
    }
}