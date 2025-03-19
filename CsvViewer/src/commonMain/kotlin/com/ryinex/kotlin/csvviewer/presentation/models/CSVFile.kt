package com.ryinex.kotlin.csvviewer.presentation.models

internal data class CSVFile(
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
        println("headers: $headers\nlist: $list")
        return list.joinToString("\n") { it.map { "\"$it\"" }.joinToString(",") }
    }

    companion object {
        fun empty(name: String = "Untitled"): CSVFile {
            val list =
                (0..99).map {
                    val list = ('A'..'Z').map { it.toString() to " " }
                    mutableMapOf(*list.toTypedArray())
                }
            return CSVFile(name, true, list)
        }
    }
}