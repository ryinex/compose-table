package com.ryinex.kotlin.csvviewer.presentation.models

import com.ryinex.kotlin.csv.CsvFile

internal sealed interface CSVLoadType {
    data object Sample : CSVLoadType

    data class File(val file: CsvFile) : CSVLoadType
}