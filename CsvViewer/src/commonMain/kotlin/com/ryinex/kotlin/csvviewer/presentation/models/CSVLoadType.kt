package com.ryinex.kotlin.csvviewer.presentation.models

internal sealed interface CSVLoadType {
    data object Sample : CSVLoadType

    data class File(val file: CSVFile) : CSVLoadType
}