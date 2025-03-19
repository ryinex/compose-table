package com.ryinex.kotlin.datatable.data

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateOf
import kotlin.math.ceil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class DataTableRows<DATA : Any>(
    private val table: DataTable<*>
) {
    internal var key: ((Int, DATA) -> Any)? = null
        private set
    private var original: List<DATA> = emptyList()
    private val extraScrollHeightMultiplier = 1.5f
    private var cellsMap = mutableMapOf<Int, DataTableRow>()
    private var queuedCellNavigation: DataTableCellLocation? = null
    private var currentFocusedCell: DataTableCell<*, *>? = null
    private var lastComparator: ((DATA, DATA) -> Int)? = null
    internal val state = mutableStateOf(original)

    internal fun setList(list: List<DATA>, key: ((Int, DATA) -> Any)?) {
        original = list
        this.key = key
        table.columns.find { it.tag == DataTable.INDEX_COLUMN_TAG }?.name?.value = original.size.toString()
        state.value = if (lastComparator != null) original.sortedWith(lastComparator!!) else original
    }

    internal fun append(list: List<DATA>) {
        original = original + list
        table.columns.find { it.tag == DataTable.INDEX_COLUMN_TAG }?.name?.value = original.size.toString()

        state.value = if (lastComparator != null) original.sortedWith(lastComparator!!) else original
    }

    internal suspend fun sort(comparator: (DATA, DATA) -> Int) = withContext(Dispatchers.Default) {
        lastComparator = comparator
        val sorted = state.value.sortedWith(comparator)
        state.value = sorted

        if (currentFocusedCell == null) return@withContext
        navigateTo(currentFocusedCell!!)
    }

    internal suspend fun resetSort() {
        lastComparator = null
        state.value = original
        if (currentFocusedCell == null) return
        navigateTo(currentFocusedCell!!)
    }

    internal fun setCell(cell: DataTableCell<*, *>) {
        if (cellsMap[cell.properties.location.layoutRowIndex] == null) {
            cellsMap[cell.properties.location.layoutRowIndex] = DataTableRow()
        }

        cellsMap[cell.properties.location.layoutRowIndex]!!.cells[cell.properties.location.columnIndex] = cell
        if (
            cell.properties.location.columnIndex == queuedCellNavigation?.columnIndex &&
            cell.properties.location.layoutRowIndex == queuedCellNavigation?.layoutRowIndex
        ) {
            focusCell(cell.properties.location.layoutRowIndex, cell.properties.location.columnIndex)
        }
    }

    private suspend fun navigateTo(to: DataTableCell<*, *>) {
        val index = state.value.indexOf(to.data)
        if (index == -1) return

        navigateTo(index, to.properties.location.columnIndex)
    }

    internal suspend fun <VALUE> find(value: VALUE): List<DataTableCellLocation> = withContext(Dispatchers.Default) {
        val columns = table.columns.filterIsInstance<DataTableColumn<VALUE, DATA>>()
        return@withContext state.value
            .asSequence()
            .mapIndexed { rowIndex, data ->
                val dataRowCellValues =
                    columns.map {
                        it.value(DataTableCellLocation.empty.copy(layoutRowIndex = rowIndex), data)
                    }
                val filtered =
                    dataRowCellValues.mapIndexed { index, dataValue ->
                        if (dataValue.containsOrEqual(value)) {
                            table.cellLocation(rowIndex, index, false)
                        } else {
                            null
                        }
                    }
                filtered.filterNotNull()
            }
            .flatten()
            .toList()
    }

    internal fun refocusCurrentCell() {
        if (!table.scope.isActive) return
        table.scope.launch {
            currentFocusedCell?.let {
                delay(50)
                focusCell(it.properties.location.layoutRowIndex, it.properties.location.columnIndex)
            }
        }
    }

    internal suspend fun navigateTo(dataIndex: Int, cellIndex: Int) {
        val location = table.cellLocation(dataIndex, cellIndex, false)
        navigateTo(location)
    }

    internal suspend fun navigateTo(location: DataTableCellLocation) {
        val from = location.copy(layoutRowIndex = -999)

        scrollTo(location.layoutRowIndex, 16 * extraScrollHeightMultiplier)
        navigateTo((table.viewport.value.height * 0.25f).toInt(), from, location)
    }

    internal suspend fun navigateTo(from: DataTableCellProperties, to: DataTableCellLocation) {
        navigateTo(from.height, from.location, to)
    }

    private suspend fun navigateTo(fromHeight: Int, from: DataTableCellLocation, to: DataTableCellLocation) {
        if (queuedCellNavigation != null) return
        queuedCellNavigation = to

        if (cellsMap[to.layoutRowIndex] == null) {
            queuedCellNavigation = to
            scroll(fromHeight.toFloat(), from, to)
        } else {
            focusCell(to.layoutRowIndex, to.columnIndex)
        }
        removeStuck(fromHeight, from, to)
    }

    private suspend fun scroll(fromHeight: Float, from: DataTableCellLocation, to: DataTableCellLocation) {
        val fromHeight = fromHeight * extraScrollHeightMultiplier
        if (cellsMap[from.layoutRowIndex] == null) {
            scrollTo(to.layoutRowIndex, fromHeight)
        } else if (to.isUp(from)) {
            table.lazyState.scrollBy(fromHeight * -1)
        } else {
            table.lazyState.scrollBy(fromHeight)
        }
    }

    private suspend fun scrollTo(to: Int, fromHeight: Float) {
        table.lazyState.scrollToItem(to, -ceil(fromHeight).toInt())
    }

    internal fun focusCell(rowLayoutIndex: Int, columnIndex: Int) {
        val cell = cellsMap[rowLayoutIndex]?.cells?.get(columnIndex) ?: return
        val properties = cell.properties.copy(height = (table.viewport.value.height * 0.25f).toInt())
        kotlin.runCatching {
            cell.properties.cellFocusRequester.requestFocus()
            queuedCellNavigation = null
            currentFocusedCell = cell.copy(properties = properties)
        }
    }

    private suspend fun removeStuck(fromHeight: Int, from: DataTableCellLocation, to: DataTableCellLocation) {
        delay(10)
        if (queuedCellNavigation != to) return

        if (to.isUp(from)) {
            table.lazyState.scrollBy(fromHeight * -extraScrollHeightMultiplier)
        } else if (to.isDown(from)) {
            table.lazyState.scrollBy(fromHeight * extraScrollHeightMultiplier)
        }

        delay(10)

        focusCell(to.layoutRowIndex, to.columnIndex)
        queuedCellNavigation = null
    }

    internal fun setCellsMap(lazyState: LazyListState) {
        val visible = lazyState.layoutInfo.visibleItemsInfo.map { it.index }
        val firstIndex = visible.firstOrNull() ?: return
        val lastIndex = visible.last()
        if (firstIndex <= (table.headerLayoutIndex ?: -1) && lastIndex <= (table.firstItemLayoutIndex ?: -1)) return
        if (lastIndex >= table.lastLayoutRowIndex) return
        val buffer = mutableMapOf<Int, DataTableRow>()

        visible.forEach { buffer[it] = cellsMap[it] ?: DataTableRow() }

        cellsMap = buffer
    }

    private fun <T> T.containsOrEqual(value: T): Boolean {
        return if (this is String && value is String) this.contains(value) else this == value
    }
}

internal class DataTableRow {
    val cells = mutableMapOf<Int, DataTableCell<*, *>?>()
}