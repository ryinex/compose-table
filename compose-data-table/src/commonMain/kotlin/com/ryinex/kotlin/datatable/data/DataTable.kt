package com.ryinex.kotlin.datatable.data

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DataTable<DATA : Any>(
    config: DataTableConfig,
    internal val scope: CoroutineScope,
    internal val isLoadingMoreEnabled: () -> Boolean = { false },
    internal val onLoadMoreItems: (DataTable<DATA>) -> Unit = { },
    internal val lazyState: LazyListState
) {
    internal val config = mutableStateOf(config)
    internal val headersTag = "${this::class.qualifiedName}${this.hashCode()}"
    internal val columns = mutableStateListOf<DataTableColumn<*, DATA>>()
    internal val rows = DataTableRows<DATA>(table = this)
    internal val viewport = mutableStateOf(IntSize.Zero)
    internal val lastLayoutRowIndex get() = rows.state.value.lastIndex + if (config.value.isHeadered) 1 else 0
    internal val views get() = columns.map { it.view }
    internal var headerLayoutIndex: Int? = null
    internal var firstItemLayoutIndex: Int? = null
    internal var enableInteractions = mutableStateOf(false)

    private var isIndexed = false

    init {
        setConfig(config)
    }

    fun enableInteractions(enable: Boolean) {
        if (enableInteractions.value != enable) {
            enableInteractions.value = enable
            if (enable && scope.isActive) scope.launch { rows.refocusCurrentCell() }
        }
    }

    internal fun setViewport(size: IntSize) {
        if (viewport.value.width == size.width) return
        viewport.value = size
        views.ensureFillViewPort()
    }

    internal fun headers(): List<@Composable RowScope.() -> Unit> {
        setHeaderLayoutIndex()
        return columns.map { column -> column.header(onWidthChanged = { force -> views.ensureFillViewPort(force) }) }
    }

    internal fun row(index: Int, data: DATA): List<@Composable RowScope.() -> Unit> {
        if (index == 0) setFirstItemLayoutIndex(data)
        if (index == rows.state.value.size - 1 && isLoadingMoreEnabled()) onLoadMoreItems(this)
        return columns.map { column -> column.cell(index, data, onWidthChanged = { views.ensureFillViewPort() }) }
    }

    internal fun resetSortHeaders(current: ComparableDataTableColumn<*, *>?) {
        columns.filterIsInstance<ComparableDataTableColumn<*, *>>()
            .filter { it != current }
            .forEach { column -> column.resetSort() }
    }

    internal fun cellLocation(rowIndex: Int, cellIndex: Int, isHeader: Boolean): DataTableCellLocation {
        return DataTableCellLocation(
            layoutRowIndex = if (!isHeader && config.value.isHeadered) rowIndex + 1 else rowIndex,
            columnIndex = cellIndex,
            lastLayoutRowIndex = { lastLayoutRowIndex },
            lastColumnIndex = columns.lastIndex,
            isHeadered = config.value.isHeadered
        )
    }

    internal fun List<MutableState<DataTableColumnViewPort>>.ensureFillViewPort(force: Boolean = false) {
        if (this.any { it.value.width == 0 }) return
        if (columns.any { it.view.value.layout == DataTableColumnLayout.FixedFree } && !force) return
        val range = (if (config.value.isIndexed) 1 else 0)..<size
        val source = columns.slice(range).filter { it.view.value.layout != DataTableColumnLayout.FixedFree }.map { it }
        val removes = (if (config.value.isIndexed) this[0].value.width else 0) +
            config.value.horizontalSpacing * (columns.size - 1) +
            columns
                .slice(range)
                .filter { it.view.value.layout == DataTableColumnLayout.FixedFree }
                .sumOf { it.largestWidth }
        val viewPortWidth = viewport.value.width
        val requiredSum = viewPortWidth - removes
        val sum = columns
            .slice(range)
            .filter { it.view.value.layout != DataTableColumnLayout.FixedFree }
            .sumOf { it.largestWidth }
        if (sum >= requiredSum) return

        println(
            "viewport: ${viewport.value.width}, sum: $sum, ${sum >= requiredSum}," +
                " requiredSum: $requiredSum, removes: $removes"
        )

//        val difference = requiredSum - sum
//        val increase = difference / source.size
//        val reminder = difference % source.size
//
//        val updatedList = source
//            .mapIndexed { index, state -> state.largestWidth + increase + (if (index < reminder) 1 else 0) }

        val difference = requiredSum.toFloat() / sum
        val updatedList = source.mapIndexed { index, state -> state.largestWidth * difference }

//        println(
//            "viewport: $viewPortWidth, removes: $removes, sum: $sum, requiredSum: $requiredSum, updatedList: $updatedList"
//        )

        source.forEachIndexed { index, column -> column.setViewPortWidth(updatedList[index].toInt()) }
    }

    private fun setHeaderLayoutIndex() {
        val index = lazyState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == headersTag }?.index
        if (headerLayoutIndex == index || index == null) return
        headerLayoutIndex = index
    }

    private fun setFirstItemLayoutIndex(data: DATA) {
        val index =
            lazyState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == rows.key?.let { it1 -> it1(0, data) } }?.index
        if (firstItemLayoutIndex == index || index == null) return
        firstItemLayoutIndex = index
    }

    fun setConfig(config: DataTableConfig) {
        if (config.isIndexed && !isIndexed) {
            isIndexed = true
            index()
            views.ensureFillViewPort()
        } else if (!config.isIndexed && isIndexed) {
            isIndexed = false
            columns.removeAt(0)
            views.ensureFillViewPort()
        }
        this.config.value = config
    }

    fun resetWidths() {
        columns.forEach { it.resetWidths() }
    }

    companion object {
        internal const val INDEX_COLUMN_TAG = "index-column"
    }
}