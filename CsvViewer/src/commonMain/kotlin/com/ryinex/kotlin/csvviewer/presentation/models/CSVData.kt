package com.ryinex.kotlin.csvviewer.presentation.models

internal data class CSVColumn(val name: String)

internal data class CSVData(
    val columns: List<CSVColumn> = ('A'..'Z').map { CSVColumn(it.toString()) },
    val rows: List<CSVRow>
)

internal data class CSVRow(val key: Any, val value: MutableMap<Any, String>)

private fun Char.nextChar(): Char? {
    val next = this.code + 1
    return if (next > 'Z'.code) null else next.toChar()
}