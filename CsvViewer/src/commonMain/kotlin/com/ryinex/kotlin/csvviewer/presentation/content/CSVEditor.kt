package com.ryinex.kotlin.csvviewer.presentation.content

import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import com.ryinex.kotlin.csvviewer.presentation.models.CSVRow
import com.ryinex.kotlin.datatable.data.DataTable
import com.ryinex.kotlin.datatable.data.DataTableConfig
import com.ryinex.kotlin.datatable.data.DataTableEditTextConfig
import com.ryinex.kotlin.datatable.data.setList
import com.ryinex.kotlin.datatable.data.text
import com.ryinex.kotlin.datatable.views.DataTableView

@Composable
internal fun CSVEditor(
    isLocked: Boolean,
    config: DataTableConfig,
    editConfig: DataTableEditTextConfig<String, CSVRow>,
    onReload: () -> Unit
) {
    Surface {
        Table(
            isLocked = isLocked,
            config = config,
            editConfig = editConfig
        )
    }
}

@Composable
private fun Table(isLocked: Boolean, config: DataTableConfig, editConfig: DataTableEditTextConfig<String, CSVRow>) {
    val coroutineScope = rememberCoroutineScope()
    val lazyState = rememberLazyListState()
    val table =
        remember {
            val table =
                DataTable<CSVRow>(
                    config = config,
                    scope = coroutineScope,
                    lazyState = lazyState
                )

            ('A'..'P').forEach { char ->
                table.text(
                    name = char.toString(),
                    editTextConfig =
                    editConfig.copy(onConfirmEdit = { data, _, text ->
                        data.value[char.toString()] = text
                        text
                    }),
                    presentation = {
                            _,
                            _,
                            presentation
                        ->
                        presentation.copy(modifier = presentation.modifier.widthIn(160.dp))
                    },
                    value = { location, data -> data.value[char.toString()] ?: "" }
                )
            }
            val list = (0..99).map { CSVRow(it, mutableMapOf()) }
            table.setList(list) { _, item -> item.key }
            table.enableInteractions(true)
            table
        }

    LaunchedEffect(isLocked) { table.enableInteractions(!isLocked) }

    DataTableView(table = table)
}