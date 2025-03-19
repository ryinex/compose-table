package com.ryinex.kotlin.datatable.data

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.ryinex.kotlin.datatable.views.CellContainer
import com.ryinex.kotlin.datatable.views.TextViewCell
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal data class DataTableColumnWidths(
    var headerCellWidth: Int,
    var firstCellWidth: Int,
    var largestRegularCellWidth: Int,
    var latestViewPortWidth: Int
) {
    constructor() : this(0, 0, 0, 0)

    val smallest get() = minOf(headerCellWidth, firstCellWidth, largestRegularCellWidth)
}

internal data class DataTableColumnViewPort(
    val width: Int,
    val weight: Float,
    val layout: DataTableColumnLayout
)

sealed class DataTableColumn<VALUE, DATA : Any>(
    internal val tag: String,
    internal val table: DataTable<DATA>,
    internal val name: MutableState<String>,
    internal val value: (location: DataTableCellLocation, DATA) -> VALUE,
    internal val cell: (location: DataTableCellLocation, DATA, DataTableCellProperties) -> DataTableCell<VALUE, DATA>,
    internal val config: DataTableColumnConfig
) {
    private val widths = DataTableColumnWidths()
    private var isDragging = false
    val largestWidth
        get() = if (view.value.layout != DataTableColumnLayout.FixedFree) {
            widths.largestRegularCellWidth
        } else {
            view.value.width
        }

    internal val view =
        mutableStateOf(
            DataTableColumnViewPort(width = 0, weight = config.weight, layout = config.layout)
        )

    fun header(onWidthChanged: (Boolean) -> Unit): @Composable RowScope.() -> Unit = {
        val location =
            remember(table.columns) {
                table.cellLocation(0, table.columns.indexOf(this@DataTableColumn), true)
            }
        val properties = DataTableCellProperties.create(location = location)
        val currentTextStyle = LocalTextStyle.current
        val cellConfig =
            remember {
                if (tag != DataTable.INDEX_COLUMN_TAG) return@remember table.config.value.defaultHeaderConfig
                val original = table.config.value.defaultHeaderConfig

                table.config.value.defaultHeaderConfig
                    .copy(
                        color = Color.Transparent,
                        textStyle = (original.textStyle ?: currentTextStyle).copy(color = Color.Transparent)
                    )
            }
        val textStyle = remember { cellConfig.textStyle } ?: LocalTextStyle.current
        val cell =
            remember(location, name.value) {
                cell(
                    data = Any(),
                    value = name.value,
                    properties = properties,
                    config = cellConfig,
                    view = { value -> TextViewCell(text = value, textStyle = textStyle) }
                )
            }

        LaunchedEffect(cell) { table.rows.setCell(cell) }

        CellContainer(
            cell = cell,
            config = table.config.value.defaultHeaderConfig,
            columnConfig = config,
            columnViewPortConfig = view.value,
            onDrag = ::onDrag,
            onDragFinished = ::onDragFinished,
            onWidth = {
                onWidth(
                    width = it,
                    isHeader = true,
                    itemIndex = 0,
                    onWidthChanged = { onWidthChanged(false) }
                )
            },
            isFocusable = false,
            isClickable = this@DataTableColumn is ComparableDataTableColumn,
            isHeader = true,
            enableInteractions = table.enableInteractions.value,
            onNavigate = { from, to ->
                if (table.scope.isActive) {
                    table.scope.launch { table.rows.navigateTo(from, to) }
                }
            },
            onClick = { onHeaderClick() },
            onDoubleClickDrag = {
                setViewPortLayout(config.layout)
                onWidthChanged(true)
            }
        )
    }

    fun cell(index: Int, data: DATA, onWidthChanged: () -> Unit): @Composable RowScope.() -> Unit = {
        val location =
            remember(index, table.columns) {
                table.cellLocation(index, table.columns.indexOf(this@DataTableColumn), false)
            }
        val properties = DataTableCellProperties.create(location = location)
        val cell = remember(location, data) { cell(location, data, properties) }
        val config = remember(index, cell.state.value) { cell.config(cell.state.value) }

        LaunchedEffect(cell) { table.rows.setCell(cell) }

        CellContainer(
            cell = cell,
            config = config,
            columnConfig = this@DataTableColumn.config,
            columnViewPortConfig = view.value,
            isFocusable = true,
            isClickable = true,
            isHeader = false,
            enableInteractions = table.enableInteractions.value,
            onWidth = {
                onWidth(
                    width = it,
                    isHeader = false,
                    itemIndex = index,
                    onWidthChanged = onWidthChanged
                )
            },
            onDrag = ::onDrag,
            onDragFinished = ::onDragFinished,
            onClick = {
                table.rows.focusCell(it.location.layoutRowIndex, it.location.columnIndex)
            },
            onDoubleClickDrag = {},
            onNavigate = { from, to ->
                if (table.scope.isActive) {
                    table.scope.launch { table.rows.navigateTo(from, to) }
                }
            }
        )
    }

    private fun onWidth(width: Int, isHeader: Boolean, itemIndex: Int, onWidthChanged: () -> Unit) {
        val updated =
            updateLargestWidth(
                width = width,
                isHeader = isHeader,
                itemIndex = itemIndex,
                itemsSize = table.rows.state.value.size
            )
        if (updated) onWidthChanged()
    }

    private fun updateLargestWidth(width: Int, isHeader: Boolean, itemIndex: Int, itemsSize: Int): Boolean {
        if (
            width <= widths.largestRegularCellWidth ||
            width <= view.value.width ||
            width == widths.latestViewPortWidth
        ) {
            return false
        }
        // println("cell: ${cellIndex}, width: $width, largestRegularCellWidth: ${widths.largestRegularCellWidth}")

        if (isHeader && widths.headerCellWidth == 0) {
            widths.headerCellWidth = width
        } else if (!isHeader && itemIndex == 0 && widths.firstCellWidth == 0) {
            widths.firstCellWidth =
                width
        }
        if (widths.headerCellWidth == 0 || (widths.firstCellWidth == 0 && itemsSize > 1)) return false

        if (width > widths.largestRegularCellWidth && !isDragging) {
            widths.largestRegularCellWidth =
                maxOf(
                    width,
                    widths.largestRegularCellWidth,
                    widths.headerCellWidth,
                    widths.firstCellWidth
                )
        }

        setViewPortWidth(widths.largestRegularCellWidth)
        return true
    }

    fun setViewPortWidth(width: Int) {
        widths.latestViewPortWidth = this.view.value.width
        this.view.value = this.view.value.copy(width = width)
    }

    private fun setViewPortLayout(layout: DataTableColumnLayout) {
        widths.latestViewPortWidth = this.view.value.width
        this.view.value = this.view.value.copy(layout = layout)
    }

    private fun setViewPort(width: Int, weight: Float, layout: DataTableColumnLayout) {
        widths.latestViewPortWidth = this.view.value.width
        this.view.value = this.view.value.copy(width = width, weight = weight, layout = layout)
        with(table) { views.ensureFillViewPort(true) }
    }

    private fun onDrag(delta: Int): Boolean {
        val newWidth = view.value.width + delta
        if (newWidth <= widths.smallest) return false
        val newWeight = newWidth.toFloat() / view.value.width * view.value.weight
        isDragging = true
        setViewPort(newWidth, newWeight, DataTableColumnLayout.FixedFree)
        return true
    }

    private fun onDragFinished() {
        // setViewPort(view.value.width, view.value.weight, config.columnLayout)
        isDragging = false
    }

    private fun onHeaderClick() {
        when (this) {
            is ComparableDataTableColumn -> this.sortClick()
            is ComposeDataTableColumn -> {}
        }
    }
}

internal class ComposeDataTableColumn<VALUE, DATA : Any> constructor(
    table: DataTable<DATA>,
    config: DataTableColumnConfig,
    name: String,
    tag: String = name,
    value: (location: DataTableCellLocation, DATA) -> VALUE,
    cell: (location: DataTableCellLocation, DATA, DataTableCellProperties) -> DataTableCell<VALUE, DATA>
) : DataTableColumn<VALUE, DATA>(
    tag = tag,
    table = table,
    config = config,
    cell = cell,
    value = value,
    name = mutableStateOf(name)
)

internal class ComparableDataTableColumn<VALUE : Comparable<VALUE>, DATA : Any>(
    private var sortType: DataTableColumnSort = DataTableColumnSort.None,
    value: (location: DataTableCellLocation, DATA) -> VALUE,
    table: DataTable<DATA>,
    config: DataTableColumnConfig,
    cell: (location: DataTableCellLocation, DATA, DataTableCellProperties) -> DataTableCell<VALUE, DATA>,
    name: String,
    tag: String = name
) : DataTableColumn<VALUE, DATA>(
    tag = tag,
    table = table,
    config = config,
    cell = cell,
    value = value,
    name = mutableStateOf("$name  •")
) {
    private val originalName = name

    internal fun sortClick() {
        table.resetSortHeaders(this)
        if (!table.scope.isActive) return
        when (sortType) {
            DataTableColumnSort.None ->
                table.scope.launch {
                    table.rows.sort { first, second -> compare(first, second) }
                    sortType = DataTableColumnSort.Ascending
                    name.value = originalName + nameSuffix(sortType)
                }

            DataTableColumnSort.Ascending ->
                table.scope.launch {
                    table.rows.sort { first, second -> compare(second, first) }
                    sortType = DataTableColumnSort.Descending
                    name.value = originalName + nameSuffix(sortType)
                }

            DataTableColumnSort.Descending ->
                table.scope.launch {
                    table.rows.resetSort()
                    sortType = DataTableColumnSort.None
                    name.value = originalName + nameSuffix(sortType)
                }
        }
    }

    internal fun resetSort() {
        sortType = DataTableColumnSort.None
        name.value = originalName + nameSuffix(sortType)
    }

    private fun compare(first: DATA, second: DATA): Int {
        val location = DataTableCellLocation(-1, -1, { -1 }, -1, false)
        return value(location, first).compareTo(value(location, second))
    }

    private fun nameSuffix(sortType: DataTableColumnSort): String {
        return when (sortType) {
            DataTableColumnSort.None -> "  •"
            DataTableColumnSort.Ascending -> " ^"
            DataTableColumnSort.Descending -> " v"
        }
    }
}