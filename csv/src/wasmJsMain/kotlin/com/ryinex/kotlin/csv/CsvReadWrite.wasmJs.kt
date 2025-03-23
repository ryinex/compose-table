package com.ryinex.kotlin.csv

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.delay
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.FileReader

actual object CsvReadWrite {
    actual suspend fun open(): CsvFile? {
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

        return open(fileName ?: "", content!!)
    }

    actual suspend fun open(name: String, content: String): CsvFile? {
        val lines = csvLines(content)
        return if (lines.isNotEmpty()) CsvFile(name, lines) else null
    }

    actual suspend fun save(csvFile: CsvFile) {
        save(csvFile.name, csvFile.raw())
    }

    actual suspend fun save(name: String, content: String) {
        val a = window.document.createElement("a") as HTMLAnchorElement
        a.href = URL.createObjectURL(Blob(arrayOf(content.toJsString()).toJsArray().unsafeCast()))
        a.download = name.ensureEndsWithCsv()
        a.click()
    }
}

/** Returns a new [JsArray] containing all the elements of this [Array]. */
private fun <T : JsAny?> Array<T>.toJsArray(): JsArray<T> {
    val destination = JsArray<T>()
    for (i in this.indices) {
        destination[i] = this[i]
    }
    return destination
}