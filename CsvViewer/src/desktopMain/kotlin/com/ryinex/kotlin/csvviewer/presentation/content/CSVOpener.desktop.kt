package com.ryinex.kotlin.csvviewer.presentation.content

import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ryinex.kotlin.csvviewer.data.csvLines
import com.ryinex.kotlin.csvviewer.data.ensureEndsWithCsv
import com.ryinex.kotlin.csvviewer.presentation.models.CSVFile
import java.awt.FileDialog
import java.awt.Frame
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.FilenameFilter
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal actual fun SelectButton(isFirstHeader: Boolean, text: String, onLoad: (CSVFile) -> Unit) {
    val scope = rememberCoroutineScope()
    Button(
        modifier = Modifier.width(200.dp),
        onClick = { scope.launch { onLoad(openFile(isFirstHeader)) } },
        content = { Text(text) }
    )
}

private suspend fun openFile(isFirstHeader: Boolean): CSVFile = withContext(Dispatchers.Main) {
    val frame: Frame? = null
    val dialog = FileDialog(frame, "Select File to Open")
    dialog.mode = FileDialog.LOAD
    dialog.filenameFilter = FilenameFilter { file, s -> s.matches(".+.csv".toRegex()) }
    dialog.isVisible = true
    val directory = dialog.directory
    val fileName = dialog.file
    dialog.dispose()
    val content = if (fileName != null) File(directory, fileName).readText() else ""
    return@withContext CSVFile(fileName ?: "", isFirstHeader, csvLines(isFirstHeader, content))
}

@Composable
internal actual fun SaveButton(text: String, fileName: String, file: CSVFile) {
    val scope = rememberCoroutineScope()
    Button(onClick = { scope.launch { saveFile(file.raw(), fileName) } }) { Text(text) }
}

private suspend fun saveFile(content: String, name: String) = withContext(Dispatchers.Main) {
    var fileOutput: FileOutputStream? = null
    var dataOutput: DataOutputStream? = null
    val frame: Frame? = null
    val dialog = FileDialog(frame, "Save", FileDialog.SAVE)
    dialog.filenameFilter = FilenameFilter { file, s -> s.matches(".+.csv".toRegex()) }
    dialog.file = name.ensureEndsWithCsv()
    dialog.isVisible = true
    val dir = dialog.directory
    val oneFile = File(dir + dialog.file)
    try {
        oneFile.createNewFile()
    } catch (e1: IOException) {
        e1.printStackTrace()
    }
    try {
        fileOutput = FileOutputStream(oneFile)
        dataOutput = DataOutputStream(fileOutput)
        dataOutput.writeBytes(content)
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