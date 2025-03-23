package com.ryinex.kotlin.csv

actual object CsvReadWrite {
    actual suspend fun open(): CsvFile? {
        TODO("Not yet implemented")
    }

    actual suspend fun open(name: String, content: String): CsvFile? {
        TODO("Not yet implemented")
    }

    actual suspend fun save(csvFile: CsvFile) {
    }

    actual suspend fun save(name: String, content: String) {
    }
}