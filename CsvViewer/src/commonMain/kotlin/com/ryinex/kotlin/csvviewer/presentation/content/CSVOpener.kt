package com.ryinex.kotlin.csvviewer.presentation.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ryinex.kotlin.csv.CsvFile
import com.ryinex.kotlin.csv.CsvReadWrite
import com.ryinex.kotlin.csvviewer.presentation.models.CSVRow
import com.ryinex.kotlin.datatable.data.DataTable
import com.ryinex.kotlin.datatable.data.DataTableConfig
import com.ryinex.kotlin.datatable.data.DataTableEditTextConfig
import com.ryinex.kotlin.datatable.data.setList
import com.ryinex.kotlin.datatable.data.text
import com.ryinex.kotlin.datatable.views.DataTableView
import kotlinx.coroutines.launch

@Composable
internal fun CSVOpener(
    content: CsvFile,
    isLocked: Boolean,
    config: DataTableConfig,
    editConfig: DataTableEditTextConfig<String, CSVRow>,
    onReload: () -> Unit
) {
    Content(content, isLocked, config, editConfig, onReload = onReload)
}

@Composable
private fun Content(
    content: CsvFile,
    isLocked: Boolean,
    config: DataTableConfig,
    editConfig: DataTableEditTextConfig<String, CSVRow>,
    onReload: () -> Unit
) {
    Table(content.content, isLocked, config, editConfig)
}

@Composable
internal fun Title(fileName: String, file: CsvFile?, onReload: () -> Unit) {
    var fileName by remember { mutableStateOf(fileName) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = fileName,
            onValueChange = { fileName = it },
            textStyle = MaterialTheme.typography.headlineLarge.copy(color = LocalContentColor.current),
            modifier = Modifier.weight(1f)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onReload) { Icon(Icons.Filled.Refresh, contentDescription = null) }

            if (file != null) {
                val scope = rememberCoroutineScope()
                Button(
                    onClick = {
                        scope.launch { CsvReadWrite.save(name = fileName, content = file.raw()) }
                    }
                ) { Text("Save") }
            }
        }
    }
}

@Composable
private fun Table(
    content: List<MutableMap<String, String>>,
    isLocked: Boolean,
    config: DataTableConfig,
    editConfig: DataTableEditTextConfig<String, CSVRow>
) {
    val scope = rememberCoroutineScope()
    val lazyState = rememberLazyListState()

    val table =
        remember {
            val table = DataTable<CSVRow>(scope = scope, lazyState = lazyState, config = config)

            content.first().forEach { (key, _) ->
                table.text(
                    name = key,
                    value = { _, data -> data.value[key] ?: "" },
                    editTextConfig =
                    editConfig.copy(onConfirmEdit = { data, _, text ->
                        data.value[key] = text
                        text
                    })
                )
            }

            table.setList(content.map { CSVRow(it.keys.first(), it as MutableMap<Any, String>) })
            table.enableInteractions(true)
            table
        }

    LaunchedEffect(isLocked) { table.enableInteractions(!isLocked) }

    DataTableView(table = table)
}