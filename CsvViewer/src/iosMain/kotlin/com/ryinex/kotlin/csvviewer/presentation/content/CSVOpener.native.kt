package com.ryinex.kotlin.csvviewer.presentation.content

import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ryinex.kotlin.csvviewer.presentation.models.CSVFile
import kotlinx.coroutines.launch

@Composable
internal actual fun SelectButton(isFirstHeader: Boolean, text: String, onLoad: (CSVFile) -> Unit) {
    val scope = rememberCoroutineScope()
    Button(
        modifier = Modifier.width(200.dp),
        onClick = { scope.launch { } },
        content = { Text(text) }
    )
}

@Composable
internal actual fun SaveButton(text: String, fileName: String, file: CSVFile) {
    val scope = rememberCoroutineScope()
    Button(onClick = { scope.launch { } }) { Text(text) }
}