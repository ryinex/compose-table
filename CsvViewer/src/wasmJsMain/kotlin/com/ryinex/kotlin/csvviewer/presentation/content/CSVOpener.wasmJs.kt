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
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.set
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.FileReader

@Composable
internal actual fun SelectButton(isFirstHeader: Boolean, text: String, onLoad: (CSVFile) -> Unit) {
    val scope = rememberCoroutineScope()
    Button(
        modifier = Modifier.width(200.dp),
        onClick = { scope.launch { onLoad(openFile(isFirstHeader)) } },
        content = { Text(text) }
    )
}

private suspend fun openFile(isFirstHeader: Boolean): CSVFile {
    var counter = 0
    var content: String? = null
    var fileName: String? = null

    val fileInput = document.createElement("input") as HTMLInputElement
    val fileReader = FileReader()

    fileReader.onload = { event ->
        content = fileReader.result.toString()
    }

    fileInput.type = "file"
    fileInput.accept = ".csv"
    fileInput.onchange = { event ->
        val target = event.target as? HTMLInputElement
        val files = target?.files
        val file = files?.item(0)
        fileName = file?.name
        fileReader.readAsText(file!!)
    }
    fileInput.oncancel = { content = "" }
    fileInput.onabort = { content = "" }

    fileInput.click()

    while (true) {
        delay(25)
        counter++
        if (content != null) break
    }

    return CSVFile(fileName ?: "", isFirstHeader, csvLines(isFirstHeader, content!!))
}

@Composable
internal actual fun SaveButton(text: String, fileName: String, file: CSVFile) {
    val scope = rememberCoroutineScope()
    Button(onClick = { scope.launch { saveFile(file.raw(), fileName) } }) { Text(text) }
}

private fun saveFile(content: ByteArray, name: String) {
    val a = window.document.createElement("a") as HTMLAnchorElement
    val arr = Uint8Array(content.size)

    content.forEachIndexed { index, byte -> arr[index] = byte }

    a.href = URL.createObjectURL(Blob(arr.unsafeCast()))
    a.download = name.ensureEndsWithCsv()
    a.click()
}

private fun saveFile(content: String, name: String) {
    val a = window.document.createElement("a") as HTMLAnchorElement
    a.href = URL.createObjectURL(Blob(arrayOf(content.toJsString()).toJsArray().unsafeCast()))
    a.download = name.ensureEndsWithCsv()
    a.click()
}

/** Returns a new [JsArray] containing all the elements of this [Array]. */
private fun <T : JsAny?> Array<T>.toJsArray(): JsArray<T> {
    val destination = JsArray<T>()
    for (i in this.indices) {
        destination[i] = this[i]
    }
    return destination
}