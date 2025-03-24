package com.ryinex.kotlin.csv

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.io.FileOutputStream
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

actual object CsvReadWrite {
    private val observer: LifecycleEventObserver = LifecycleEventObserver { _, event -> onLifeCycleEvent(event) }
    private val flow = MutableSharedFlow<Uri?>()
    private var owner: LifecycleOwner? = null
    private var readLauncher: ActivityResultLauncher<Array<String>>? = null
    private var saveLauncher: ActivityResultLauncher<String>? = null
    private var context: CoroutineContext? = null

    fun bind(owner: LifecycleOwner) {
        if (
            owner.lifecycle.currentState != Lifecycle.State.CREATED &&
            owner.lifecycle.currentState != Lifecycle.State.INITIALIZED
        ) {
            throw Exception("bind must be called in onCreate of an activity or in onCreateView of a fragment")
        }
        this.owner = owner
        this.owner!!.lifecycle.addObserver(observer)
    }

    actual suspend fun open(): CsvFile? {
        context = currentCoroutineContext()
        readLauncher?.launch(arrayOf("text/csv", "text/comma-separated-values")) ?: bindError()
        val result = flow.first() ?: bindError()

        val context = owner?.getContext() ?: bindError()
        val content = context.contentResolver.openInputStream(result).use { String(it!!.readBytes()) }
        val fileName = queryName(context, result)

        return open(fileName, content)
    }

    actual suspend fun open(name: String, content: String): CsvFile? {
        val csv = csvLines(content)
        return if (csv.isNotEmpty()) CsvFile(name, csv) else null
    }

    actual suspend fun save(csvFile: CsvFile) {
        save(csvFile.name, csvFile.raw())
    }

    actual suspend fun save(name: String, content: String) {
        context = currentCoroutineContext()
        saveLauncher?.launch(name.ensureEndsWithCsv()) ?: bindError()
        val result = flow.first() ?: bindError()
        save(result, content)
    }

    private suspend fun save(uri: Uri, content: String) = withContext(Dispatchers.IO) {
        val context = owner?.getContext() ?: bindError()
        try {
            context.contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use { it.write(content.encodeToByteArray()) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getReadLauncher(callback: (Uri?) -> Unit): ActivityResultLauncher<Array<String>> {
        val caller = owner as ActivityResultCaller
        val resultCallback = ActivityResultCallback(callback)

        return caller.registerForActivityResult(ActivityResultContracts.OpenDocument(), resultCallback)
    }

    private fun getSaveLauncher(callback: (Uri?) -> Unit): ActivityResultLauncher<String> {
        val caller = owner as ActivityResultCaller
        val resultCallback = ActivityResultCallback(callback)
        val mimeType = "text/comma-separated-values"

        return caller.registerForActivityResult(ActivityResultContracts.CreateDocument(mimeType), resultCallback)
    }

    private fun onLifeCycleEvent(event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> onCreateLifeCycleEvent()
            Lifecycle.Event.ON_DESTROY -> onDestroyLifeCycleEvent()
            else -> Unit
        }
    }

    private fun onCreateLifeCycleEvent() {
        readLauncher = getReadLauncher { result ->
            val scope = context?.let { CoroutineScope(it) } ?: return@getReadLauncher
            scope.launch { flow.emit(result) }
        }
        saveLauncher = getSaveLauncher { result ->
            val scope = context?.let { CoroutineScope(it) } ?: return@getSaveLauncher
            scope.launch { flow.emit(result) }
        }
    }

    private fun onDestroyLifeCycleEvent() {
        owner?.lifecycle?.removeObserver(observer)
        owner = null
        readLauncher = null
    }

    private fun bindError(): Nothing {
        error("bind must be called in onCreate of an activity or in onCreateView of a fragment")
    }
}

internal fun LifecycleOwner.getContext(): Context {
    return if (this is Fragment) requireContext() else this as Context
}

private fun queryName(context: Context, uri: Uri): String {
    val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
    val returnCursor = checkNotNull(context.contentResolver.query(uri, projection, null, null, null))
    returnCursor.moveToFirst()
    val name = returnCursor.getString(0)
    returnCursor.close()
    return name
}