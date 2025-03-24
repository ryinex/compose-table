package com.ryinex.kotlin.csv

data class CsvFile(
    var name: String,
    val content: List<MutableMap<String, String>>
) {
    fun raw(): String {
        return content
            .filter { it.values.any { it.isNotBlank() } } // Filter out empty rows
            .map { it.values.toList() } // Convert to Rows List
            .rotate("") // Convert to Columns List
            .filter { it.any { it.isNotBlank() } } // Filter out empty columns
            .rotate("") // Convert Back to Rows List
            .joinToString("\n") { it.map { it.stringRepresent() }.joinToString(",") }
    }
}

private fun <T> List<List<T>>.rotate(default: T): List<List<T>> {
    val largest = this.maxOf { it.size }
    val original = this.map { it.ensureSize(size = largest, default = default) }
    val newList = arrayListOf<List<T>>()

    for (i in 0 until largest) {
        newList.add(original.map { it[i] })
    }

    return newList
}