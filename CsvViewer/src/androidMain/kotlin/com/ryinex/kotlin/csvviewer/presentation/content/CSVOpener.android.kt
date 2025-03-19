package com.ryinex.kotlin.csvviewer.presentation.content

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ryinex.kotlin.csvviewer.data.csvLines
import com.ryinex.kotlin.csvviewer.data.ensureEndsWithCsv
import com.ryinex.kotlin.csvviewer.presentation.models.CSVFile
import java.io.FileOutputStream

@Composable
internal actual fun SelectButton(isFirstHeader: Boolean, text: String, onLoad: (CSVFile) -> Unit) {
    val context = LocalContext.current
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            val content =
                context.contentResolver.openInputStream(uri).use { String(it!!.readBytes()) }
            val fileName = queryName(context, uri)
            val csv = csvLines(isFirstHeader, content)
            onLoad(CSVFile(fileName ?: "", isFirstHeader, csv))
        }

    Button(
        modifier = Modifier.width(200.dp),
        onClick = { launcher.launch(arrayOf("text/csv", "text/comma-separated-values")) },
        content = { Text(text) }
    )
}

@Composable
internal actual fun SaveButton(text: String, fileName: String, file: CSVFile) {
    val context = LocalContext.current
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
            if (uri != null) alterDocument(context, uri, file.raw())
        }

    Button(onClick = { launcher.launch(fileName.ensureEndsWithCsv()) }) { Text(text) }
}

private fun alterDocument(context: Context, uri: Uri, data: String) {
    try {
        context.contentResolver.openFileDescriptor(uri, "w")?.use {
            FileOutputStream(it.fileDescriptor).use { it.write(data.encodeToByteArray()) }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun queryName(context: Context, uri: Uri): String {
    val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
    val returnCursor =
        checkNotNull(
            context.contentResolver.query(uri, projection, null, null, null)
        )
    returnCursor.moveToFirst()
    val name = returnCursor.getString(0)
    returnCursor.close()
    return name
}