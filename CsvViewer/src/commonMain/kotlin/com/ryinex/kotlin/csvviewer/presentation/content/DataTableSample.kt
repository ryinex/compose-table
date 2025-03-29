package com.ryinex.kotlin.csvviewer.presentation.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
import com.ryinex.kotlin.datatable.views.DataTableView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun DataTableSample(
    isLocked: Boolean,
    config: DataTableConfig,
    editConfig: DataTableEditTextConfig<Any, Any>
) {
    New(
        isLocked = isLocked,
        config = config,
        editConfig = editConfig
    )
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
                isLoadingMoreEnabled = { !isLoading },
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
                    presentation = { index, value, config -> config.copy(alignment = Alignment.Center) },
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
        DataTableView(table = table)

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