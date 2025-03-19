package com.ryinex.kotlin.datatable.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf

internal data class DataTableCell<VALUE, DATA : Any>(
    val data: DATA,
    val properties: DataTableCellProperties,
    val value: (location: DataTableCellLocation, DATA) -> VALUE,
    val config: (VALUE) -> DataTableCellConfig,
    val view: @Composable (DataTableCell<VALUE, DATA>) -> Unit
) {
    val state = mutableStateOf(value(properties.location, data))
}

data class DataTableCellLocation(
    val layoutRowIndex: Int,
    val columnIndex: Int,
    private val lastLayoutRowIndex: () -> Int,
    private val lastColumnIndex: Int,
    private val isHeadered: Boolean
) {
    val dataRelativeRowIndex = layoutRowIndex - (if (isHeadered) 1 else 0)

    fun moveNext(): DataTableCellLocation? {
        return if (columnIndex == lastColumnIndex) null else copy(columnIndex = columnIndex + 1)
    }

    fun movePrevious(): DataTableCellLocation? {
        return if (columnIndex == 0) null else copy(columnIndex = columnIndex - 1)
    }

    fun moveUp(): DataTableCellLocation? {
        return if (layoutRowIndex <= 0) null else copy(layoutRowIndex = layoutRowIndex - 1)
    }

    fun moveDown(): DataTableCellLocation? {
        return if (layoutRowIndex == lastLayoutRowIndex()) null else copy(layoutRowIndex = layoutRowIndex + 1)
    }

    fun isUp(from: DataTableCellLocation): Boolean = layoutRowIndex < from.layoutRowIndex

    fun isDown(from: DataTableCellLocation): Boolean = layoutRowIndex > from.layoutRowIndex

    companion object {
        internal val empty = DataTableCellLocation(-1, -1, { -1 }, -1, false)
    }
}