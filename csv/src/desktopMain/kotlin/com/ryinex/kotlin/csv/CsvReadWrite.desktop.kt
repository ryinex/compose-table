package com.ryinex.kotlin.csv

import java.awt.FileDialog
import java.awt.Frame
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.FilenameFilter
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual object CsvReadWrite {

    actual suspend fun open(): CsvFile? = withContext(Dispatchers.Main) {
        val frame: Frame? = null
        val dialog = FileDialog(frame, "Select File to Open")
        dialog.mode = FileDialog.LOAD
        dialog.filenameFilter = FilenameFilter { file, s -> s.matches(".+.csv".toRegex()) }
        dialog.isVisible = true
        val directory = dialog.directory
        val fileName = dialog.file
        if (directory.isNullOrBlank() || fileName.isNullOrBlank()) return@withContext null
        dialog.dispose()
        val content = File(directory, fileName).readText()
        return@withContext open(fileName ?: "", content)
    }

    actual suspend fun open(name: String, content: String): CsvFile? = withContext(Dispatchers.Default) {
        val lines = csvLines(content)
        return@withContext if (lines.isNotEmpty()) CsvFile(name, lines) else null
    }

    actual suspend fun save(csvFile: CsvFile) {
        save(csvFile.name, csvFile.raw())
    }

    actual suspend fun save(name: String, content: String) = withContext(Dispatchers.Main) {
        var fileOutput: FileOutputStream? = null
        var dataOutput: DataOutputStream? = null
        val frame: Frame? = null
        val dialog = FileDialog(frame, "Save", FileDialog.SAVE)
        dialog.filenameFilter = FilenameFilter { file, s -> s.matches(".+.csv".toRegex()) }
        dialog.file = name.ensureEndsWithCsv()
        dialog.isVisible = true
        val dir = dialog.directory
        if (dialog.directory.isNullOrBlank() || dialog.file.isNullOrBlank()) {
            return@withContext
        }

        withContext(Dispatchers.Default) {
            val oneFile = File(dir + dialog.file)
            try {
                oneFile.createNewFile()
            } catch (e1: IOException) {
                e1.printStackTrace()
            }
            try {
                fileOutput = FileOutputStream(oneFile)
                dataOutput = DataOutputStream(fileOutput)
                dataOutput!!.writeBytes(content)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    dataOutput!!.close()
                    fileOutput!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}