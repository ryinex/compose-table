package com.ryinex.kotlin.csvviewer.presentation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ryinex.kotlin.csvviewer.presentation.content.CSVOpener
import com.ryinex.kotlin.csvviewer.presentation.content.DataTableSample
import com.ryinex.kotlin.csvviewer.presentation.content.SelectButton
import com.ryinex.kotlin.csvviewer.presentation.content.Title
import com.ryinex.kotlin.csvviewer.presentation.models.CSVFile
import com.ryinex.kotlin.csvviewer.presentation.models.CSVLoadType
import com.ryinex.kotlin.csvviewer.presentation.models.CSVRow
import com.ryinex.kotlin.csvviewer.presentation.theme.AppTheme
import com.ryinex.kotlin.datatable.data.DataTableColumnLayout
import com.ryinex.kotlin.datatable.data.DataTableConfig
import com.ryinex.kotlin.datatable.data.DataTableEditTextConfig
import com.ryinex.kotlin.datatable.data.DataTableMobileTextEdit
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    var isDarkTheme by remember { mutableStateOf(false) }
    AppTheme(useDarkTheme = isDarkTheme) {
        val isLocked = remember { mutableStateOf(false) }
        val backgroundColor = MaterialTheme.colorScheme.background
        val contentColor = MaterialTheme.colorScheme.onBackground
        var content by remember { mutableStateOf<CSVLoadType?>(null) }
        var editConfig by remember {
            val config = DataTableEditTextConfig.default<Any, Any>(
                isEditable = true,
                mobileBrowserEditConfig = DataTableMobileTextEdit.Dialog(
                    textFieldHint = { "Your input" },
                    content = { field, cancel, confirm ->
                        MobileChangesDialog(textField = field, onCancel = cancel, onSave = confirm)
                    }
                )
            )
            mutableStateOf(config)
        }
        var config by remember {
            val table = DataTableConfig.default(
                backgroundColor = backgroundColor,
                color = contentColor
            )
            val column = table.column.copy(layout = DataTableColumnLayout.ScrollableKeepInitial)
            val cell = column.cell.copy(
                enterFocusChild = true,
                textAlign = TextAlign.Center,
                modifier = Modifier.heightIn(40.dp).border(Dp.Hairline, contentColor)
            )

            mutableStateOf(table.copy(column = column.copy(cell = cell)))
        }
        Scaffold(
            modifier = Modifier.padding(4.dp),
            floatingActionButton = { if (content != null) FAB(isLocked) }
        ) {
            Column {
                key(isDarkTheme) {
                    TopBar(
                        initialConfig = config,
                        initialEditConfig = editConfig,
                        isDarkTheme = isDarkTheme,
                        toggleDarkTheme = { isDarkTheme = !isDarkTheme },
                        onApply = { table, edit ->
                            config = table
                            editConfig = edit
                        }
                    )
                }
                key(config, editConfig, isDarkTheme) {
                    when (content) {
                        is CSVLoadType.Sample -> {
                            Title("Sample", null, onReload = { content = null })

                            DataTableSample(
                                isLocked = isLocked.value,
                                config = config,
                                editConfig = editConfig
                            )
                        }

                        is CSVLoadType.File -> {
                            Title(
                                fileName = (content as CSVLoadType.File).file.name,
                                file = (content as CSVLoadType.File).file,
                                onReload = { content = null }
                            )

                            CSVOpener(
                                content = (content as CSVLoadType.File).file,
                                isLocked = isLocked.value,
                                config = config,
                                editConfig = editConfig as DataTableEditTextConfig<String, CSVRow>,
                                onReload = { content = null }
                            )
                        }

                        null -> {
                            Loader(
                                onLoad = {
                                    when (it) {
                                        is CSVLoadType.Sample -> content = it
                                        is CSVLoadType.File -> if (it.file.content.isNotEmpty()) content = it
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FAB(isLocked: MutableState<Boolean>) {
    SmallFloatingActionButton(onClick = { isLocked.value = !isLocked.value }) {
        if (!isLocked.value) {
            Icon(Icons.Filled.Lock, contentDescription = "Lock")
        } else {
            Icon(Icons.Filled.Clear, contentDescription = "Unlock")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Loader(onLoad: (CSVLoadType) -> Unit) {
    val scrollState = rememberScrollState()
    Box(modifier = Modifier.fillMaxSize().verticalScroll(scrollState), contentAlignment = Alignment.Center) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.Center
        ) {
            UnWrapText(modifier = Modifier.weight(1f), text = "CSV Opener")

            CSVOpenButton(onLoad)
        }
    }
}

@Composable
private fun RowScope.CSVOpenButton(onLoad: (CSVLoadType) -> Unit) {
    var isFirstHeader by remember { mutableStateOf(true) }
    Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Select Comma Separated CSV File",
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = isFirstHeader, onCheckedChange = { isFirstHeader = it })
            Text("First Row is Header", style = MaterialTheme.typography.bodyMedium)
        }

        Button(modifier = Modifier.width(200.dp), onClick = { onLoad(CSVLoadType.Sample) }) { Text("Sample") }

        Button(
            modifier = Modifier.width(200.dp),
            onClick = { onLoad(CSVLoadType.File(CSVFile.empty())) },
            content = { Text("Empty") }
        )

        SelectButton(isFirstHeader = isFirstHeader, text = "Select File", onLoad = { onLoad(CSVLoadType.File(it)) })
    }
}

@Composable
private fun UnWrapText(modifier: Modifier = Modifier, text: String) {
    var fitWidth by remember { mutableStateOf(-1) }

    if (fitWidth == -1) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier.onGloballyPositioned { fitWidth = it.size.width + 2 },
                text = text,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize * 2f
                )
            )
        }
    } else {
        var viewportWidth by remember { mutableStateOf(0) }
        var breakingViewport by remember { mutableStateOf(0) }
        var isLessThanMin by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxWidth().onGloballyPositioned { viewportWidth = it.size.width })

        Box(modifier = if (isLessThanMin) Modifier.fillMaxWidth() else modifier, contentAlignment = Alignment.Center) {
            Box(
                modifier =
                    Modifier.fillMaxWidth().onGloballyPositioned {
                        val result = it.size.width < fitWidth
                        val isBroken = it.size.width <= breakingViewport
                        val equalViewport = it.size.width == viewportWidth

                        val enable = !equalViewport || !isBroken

                        if (result != isLessThanMin && enable) {
                            if (result && breakingViewport == 0) breakingViewport = viewportWidth
                            isLessThanMin = result
                        }
                    }
            )

            Text(
                modifier = Modifier.onGloballyPositioned {
                    if (it.size.width > fitWidth) {
                        breakingViewport = 0
                        fitWidth = it.size.width + 2
                    }
                },
                text = text,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize * 2f
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}