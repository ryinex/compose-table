package com.ryinex.kotlin.datatable.data

import androidx.compose.runtime.Composable
import com.ryinex.kotlin.datatable.views.TextEditableCell
import com.ryinex.kotlin.datatable.views.TextViewCell

internal fun <DATA : Any> DataTable<DATA>.index(): DataTable<DATA> {
    return composable(
        tag = DataTable.INDEX_COLUMN_TAG,
        index = 0,
        name = this.rows.state.value.size.toString(),
        config = { config -> config.copy(isResizable = false, layout = DataTableColumnLayout.ScrollableKeepLargest) },
        presentation = { _, _, presentation ->
            presentation.copy(isForceLtr = true)
        },
        value = { _, _ -> },
        content = { index, _, _ -> TextViewCell(text = "${index.dataRelativeRowIndex + 1}", textAlign = null) }
    )
}

fun <VALUE : Comparable<VALUE>, DATA : Any> DataTable<DATA>.text(
    name: String,
    editTextConfig: DataTableEditTextConfig<VALUE, DATA> = DataTableEditTextConfig.default(),
    config: (DataTableColumnConfig) -> DataTableColumnConfig = { it },
    presentation: (
        location: DataTableCellLocation,
        value: VALUE,
        presentation: DataTableCellConfig
    ) -> DataTableCellConfig = { _, _, p -> p },
    textMapper: (value: VALUE) -> String = { it.toString() },
    value: (location: DataTableCellLocation, data: DATA) -> VALUE
): DataTable<DATA> {
    val view: @Composable (DataTableCell<VALUE, DATA>) -> Unit = { cell ->
        TextEditableCell(
            textMapper = textMapper,
            cell = cell,
            editTextConfig = editTextConfig,
            coroutineScope = scope,
            enabledInteractions = enableInteractions.value,
            onFocusCell = {
                rows.focusCell(cell.properties.location.layoutRowIndex, cell.properties.location.columnIndex)
            }
        )
    }
    return text(name, config, presentation, value, view)
}

private fun <VALUE : Comparable<VALUE>, DATA : Any> DataTable<DATA>.text(
    name: String,
    config: (DataTableColumnConfig) -> DataTableColumnConfig = { it },
    presentation: (
        location: DataTableCellLocation,
        value: VALUE,
        presentation: DataTableCellConfig
    ) -> DataTableCellConfig = { _, _, p -> p },
    value: (location: DataTableCellLocation, data: DATA) -> VALUE,
    view: @Composable (DataTableCell<VALUE, DATA>) -> Unit
): DataTable<DATA> {
    val cell: (
        location: DataTableCellLocation,
        DATA,
        DataTableCellConfig,
        DataTableCellProperties
    ) -> DataTableCell<VALUE, DATA> =
        { index, data, cellConfig, properties ->
            DataTableCell(
                data = data,
                value = value,
                properties = properties,
                config = { value -> presentation(index, value, cellConfig) },
                view = view
            )
        }

    return comparable(
        name = name,
        config = config,
        value = value,
        cell = cell
    )
}

fun <DATA : Any> DataTable<DATA>.composable(
    name: String,
    config: (DataTableColumnConfig) -> DataTableColumnConfig = { it },
    presentation: (location: DataTableCellLocation, DataTableCellConfig) -> DataTableCellConfig = { _, p -> p },
    content: @Composable (location: DataTableCellLocation, data: DATA) -> Unit
): DataTable<DATA> {
    return composable(
        index = -1,
        name = name,
        config = config,
        presentation = { index, _, p -> presentation(index, p) },
        value = { _, _ -> },
        content = { index, data, _ -> content(index, data) }
    )
}

fun <VALUE, DATA : Any> DataTable<DATA>.composable(
    name: String,
    config: (DataTableColumnConfig) -> DataTableColumnConfig = { it },
    presentation: (
        location: DataTableCellLocation,
        value: VALUE,
        presentation: DataTableCellConfig
    ) -> DataTableCellConfig = { _, _, p -> p },
    value: (location: DataTableCellLocation, data: DATA) -> VALUE,
    content: @Composable (location: DataTableCellLocation, value: VALUE) -> Unit
): DataTable<DATA> {
    return composable(
        index = -1,
        name = name,
        config = config,
        presentation = presentation,
        value = value,
        content = { index, _, value -> content(index, value) }
    )
}

private fun <VALUE, DATA : Any> DataTable<DATA>.composable(
    index: Int,
    name: String,
    tag: String = name,
    config: (DataTableColumnConfig) -> DataTableColumnConfig = { it },
    presentation: (
        location: DataTableCellLocation,
        value: VALUE,
        presentation: DataTableCellConfig
    ) -> DataTableCellConfig = { _, _, p -> p },
    value: (location: DataTableCellLocation, data: DATA) -> VALUE,
    content: @Composable (location: DataTableCellLocation, data: DATA, value: VALUE) -> Unit
): DataTable<DATA> {
    val config = config(this.config.value.column)
    val column =
        ComposeDataTableColumn(
            tag = tag,
            table = this,
            name = name.ifEmpty { " " },
            config = config,
            value = value,
            cell = { index, data, properties ->
                DataTableCell(
                    data = data,
                    value = value,
                    properties = properties,
                    config = { value -> presentation(index, value, config.cell) },
                    view = { cell -> content(index, data, cell.state.value) }
                )
            }
        )
    if (index == -1) columns.add(column) else columns.add(index, column)
    views.ensureFillViewPort()
    return this
}

fun <VALUE : Comparable<VALUE>, DATA : Any> DataTable<DATA>.comparable(
    name: String,
    config: (DataTableColumnConfig) -> DataTableColumnConfig = { it },
    presentation: (
        location: DataTableCellLocation,
        value: VALUE,
        presentation: DataTableCellConfig
    ) -> DataTableCellConfig = { _, _, p -> p },
    value: (location: DataTableCellLocation, data: DATA) -> VALUE,
    content: @Composable (location: DataTableCellLocation, value: VALUE) -> Unit
): DataTable<DATA> {
    return comparable(
        name = name,
        config = config,
        value = value,
        cell = { location, data, cellConfig, properties ->
            DataTableCell(
                data = data,
                value = value,
                properties = properties,
                config = { value -> presentation(location, value, cellConfig) },
                view = { cell -> content(cell.properties.location, cell.state.value) }
            )
        }
    )
}

private fun <VALUE : Comparable<VALUE>, DATA : Any> DataTable<DATA>.comparable(
    name: String,
    config: (DataTableColumnConfig) -> DataTableColumnConfig = { it },
    value: (location: DataTableCellLocation, data: DATA) -> VALUE,
    cell: (DataTableCellLocation, DATA, DataTableCellConfig, DataTableCellProperties) -> DataTableCell<VALUE, DATA>
): DataTable<DATA> {
    val config = config(this.config.value.column)
    val column =
        ComparableDataTableColumn(
            table = this,
            name = name.ifBlank { " " },
            value = value,
            config = config,
            cell = { index, data, properties -> cell(properties.location, data, config.cell, properties) }
        )
    columns.add(column)
    views.ensureFillViewPort()
    return this
}

fun <T : Any> DataTable<T>.setList(list: List<T>, key: ((Int, T) -> Any)? = null): DataTable<T> {
    val current = rows.state.value
    if (current.isEmpty()) resetWidths()

    rows.setList(list, key)
    return this
}

fun <T : Any> DataTable<T>.appendList(list: List<T>): DataTable<T> {
    val current = rows.state.value
    if (current.isEmpty()) resetWidths()
    rows.append(list)
    return this
}

internal fun <VALUE, DATA : Any> cell(
    data: DATA,
    value: VALUE,
    properties: DataTableCellProperties,
    config: DataTableCellConfig,
    view: @Composable (VALUE) -> Unit
): DataTableCell<VALUE, DATA> {
    return DataTableCell(
        data = data,
        properties = properties,
        value = { _, _ -> value },
        config = { _ -> config },
        view = { cell -> view(cell.state.value) }
    )
}