package com.ryinex.kotlin.csvviewer.presentation.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ryinex.kotlin.csvviewer.presentation.TopBar
import com.ryinex.kotlin.csvviewer.presentation.models.Person
import com.ryinex.kotlin.csvviewer.presentation.models.Samples
import com.ryinex.kotlin.datatable.data.DataTable
import com.ryinex.kotlin.datatable.data.DataTableConfig
import com.ryinex.kotlin.datatable.data.DataTableEditTextConfig
import com.ryinex.kotlin.datatable.data.appendList
import com.ryinex.kotlin.datatable.data.comparable
import com.ryinex.kotlin.datatable.data.composable
import com.ryinex.kotlin.datatable.data.setList
import com.ryinex.kotlin.datatable.data.text
import com.ryinex.kotlin.datatable.views.EmbeddedDataTableView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun DataTableEmbbededSample(isDarkTheme: Boolean, onToggleDarkTheme: () -> Unit) {
    var isLocked by remember { mutableStateOf(false) }
    Scaffold(
        floatingActionButton = {
            SmallFloatingActionButton(onClick = { isLocked = !isLocked }) {
                if (!isLocked) {
                    Icon(Icons.Filled.Lock, contentDescription = "Lock")
                } else {
                    Icon(Icons.Filled.Clear, contentDescription = "Unlock")
                }
            }
        }
    ) {
        Surface {
            Column(modifier = Modifier.fillMaxSize()) {
                val backgroundColor = MaterialTheme.colorScheme.surface
                val contentColor = MaterialTheme.colorScheme.onSurface
                var config by remember(isDarkTheme) {
                    val table = DataTableConfig.default(backgroundColor = backgroundColor, color = contentColor)
                    val column = table.column
                    val cell =
                        column.cell.copy(
                            modifier = Modifier.border(Dp.Hairline, contentColor),
                            padding = PaddingValues(horizontal = 2.dp)
                        )

                    mutableStateOf(table.copy(column = column.copy(cell = cell)))
                }
                var editConfig by remember {
                    mutableStateOf(DataTableEditTextConfig.default<Any, Any>())
                }
                key(isDarkTheme) {
                    TopBar(
                        initialConfig = config,
                        isDarkTheme = isDarkTheme,
                        initialEditConfig = editConfig,
                        toggleDarkTheme = onToggleDarkTheme,
                        onApply = { table, edit ->
                            config = table
                            editConfig = edit
                        }
                    )
                }

                key(config, editConfig, isDarkTheme) { New(isLocked, config, editConfig) }
            }
        }
    }
}

@Composable
private fun New(isLocked: Boolean, config: DataTableConfig, editConfig: DataTableEditTextConfig<Any, Any>) {
    val lazyState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    val table =
        remember {
            DataTable(
                config = config,
                scope = scope,
                isLoadingMoreEnabled = { false },
                onLoadMoreItems = {
                    scope.launch {
                        if (isLoading) return@launch
                        isLoading = true
                        delay(500)
                        it.appendList(Samples.persons())
                        isLoading = false
                    }
                },
                lazyState = lazyState
            )
                .text(
                    name = "Name",
                    editTextConfig =
                    editConfig.copy(onConfirmEdit = { _, old, text ->
                        onSaveEdit(
                            text,
                            { isLoading = it }
                        ) { it }
                    }) as DataTableEditTextConfig<String, Person>,
                    value = { index, data -> data.name }
                )
                .composable(
                    name = "Pet",
                    content = { index, data -> PetIcon(data) }
                )
                .text(
                    "Description",
                    presentation = { index, value, config -> config.copy(enterFocusChild = true) },
                    editTextConfig =
                    editConfig.copy(onConfirmEdit = { _, old, text ->
                        onSaveEdit(
                            text,
                            { isLoading = it }
                        ) { it }
                    }) as DataTableEditTextConfig<String, Person>,
                    value = { index, data -> data.description }
                )
                .text(
                    name = "Age",
                    editTextConfig =
                    editConfig.copy(onConfirmEdit = { _, old, text ->
                        onSaveEdit(
                            text,
                            { isLoading = it }
                        ) { it.toIntOrNull() ?: 0 }
                    }) as DataTableEditTextConfig<Int, Person>,
                    value = { index, data -> data.age }
                )
                .comparable(
                    name = "Insurance",
                    value = { index, data -> data.insurance },
                    content = { index, value ->
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Checkbox(checked = value, onCheckedChange = null)
                        }
                    }
                )
                .text(
                    name = "City",
                    editTextConfig =
                    editConfig.copy(onConfirmEdit = { _, old, text ->
                        onSaveEdit(
                            text,
                            { isLoading = it }
                        ) { it }
                    }) as DataTableEditTextConfig<String, Person>,
                    value = { index, data -> data.city }
                )
                .text(
                    name = "Country",
                    editTextConfig =
                    editConfig.copy(onConfirmEdit = { _, old, text ->
                        onSaveEdit(
                            text,
                            { isLoading = it }
                        ) { it }
                    }) as DataTableEditTextConfig<String, Person>,
                    value = { index, data -> data.country }
                )
                .text(
                    name = "Occupation",
                    editTextConfig =
                    editConfig.copy(onConfirmEdit = { _, old, text ->
                        onSaveEdit(
                            text,
                            { isLoading = it }
                        ) { it }
                    }) as DataTableEditTextConfig<String, Person>,
                    value = { index, data -> data.occupation }
                )
                .text(
                    name = "Company",
                    editTextConfig =
                    editConfig.copy(onConfirmEdit = { _, old, text ->
                        onSaveEdit(
                            text,
                            { isLoading = it }
                        ) { it }
                    }) as DataTableEditTextConfig<String, Person>,
                    value = { index, data -> data.company }
                )
                .text(
                    name = "Industry",
                    editTextConfig =
                    editConfig.copy(onConfirmEdit = { _, old, text ->
                        onSaveEdit(
                            text,
                            { isLoading = it }
                        ) { it }
                    }) as DataTableEditTextConfig<String, Person>,
                    value = { index, data -> data.industry }
                )
                .text(
                    name = "Salary",
                    editTextConfig =
                    editConfig.copy(onConfirmEdit = { _, old, text ->
                        onSaveEdit(
                            text,
                            { isLoading = it }
                        ) { it.toDoubleOrNull() ?: 0.0 }
                    }) as DataTableEditTextConfig<Double, Person>,
                    value = { index, data -> data.salary }
                )
                .setList(Samples.persons()) { index, data -> data.id }
        }

    LaunchedEffect(isLocked, isLoading) {
        table.enableInteractions(!isLocked && !isLoading)
    }

    LaunchedEffect(config) {
        table.setConfig(config)
    }

    Box {
        val horizantalScrollState = rememberScrollState()
        LazyColumn {
            items(1000) { index ->
                Text("Item $index")
            }

            EmbeddedDataTableView(horizantalScrollState, table)

            items(1000) { index ->
                Text("Item $index")
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PetIcon(data: Person) {
    val state = rememberTooltipState()

    TooltipBox(
        modifier = Modifier.fillMaxSize(),
        tooltip = { PlainTooltip { Text("You hovered ${data.name}'s pet") } },
        content = {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                IconButton(
                    onClick = { },
                    content = {
                        Image(
                            modifier = Modifier.size(40.dp),
                            painter = painterResource(data.pet),
                            contentDescription = null
                        )
                    }
                )
            }
        },
        state = state,
        positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider()
    )
}

private suspend fun <T> onSaveEdit(newText: String, isLoading: (Boolean) -> Unit, mapper: (String) -> T): T {
    isLoading(true)
    delay(1000)
    isLoading(false)
    return mapper(newText)
}