package com.ryinex.kotlin.csv

data class CsvFile(
    var name: String,
    val content: List<MutableMap<String, String>>
) {
    fun raw(): String {
        val rows = content
            .filter { it.values.any { it.isNotBlank() } }
            .map { it.values.toList() }

        return rows.joinToString("\n") { it.map { it.stringRepresent() }.joinToString(",") }
    }
}