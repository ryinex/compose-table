package com.ryinex.kotlin.datatable.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.layout.onGloballyPositioned
import com.ryinex.kotlin.datatable.data.DataTable

@Composable
fun <T : Any> DataTableView(modifier: Modifier = Modifier, table: DataTable<T>) {
    val scrollableState = rememberScrollState()

    NoScrollAnimation {
        Column(modifier = modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f).focusGroup(),
                    state = table.lazyState,
                    verticalArrangement = Arrangement.spacedBy(table.config.value.verticalSpacing.densityDp)
                ) {
                    DataTableHeaderRow(table = table, scrollableState = scrollableState, isEmbedded = false)

                    ActualDataTable(scrollableState, table)
                }

                DataTableVerticalScrollbar(modifier = Modifier, state = table.lazyState)
            }

            DataTableHorizontalScrollbar(modifier = Modifier, state = scrollableState)
        }
    }
}

@Suppress("FunctionName")
fun <T : Any> LazyListScope.EmbeddedDataTableView(horizontalScrollState: ScrollState, table: DataTable<T>) {
    DataTableHeaderRow(table = table, scrollableState = horizontalScrollState, isEmbedded = true)

    ActualDataTable(horizontalScrollState, table)
}

@Suppress("FunctionName")
private fun <T : Any> LazyListScope.ActualDataTable(horizontalScrollState: ScrollState, table: DataTable<T>) {
    itemsIndexed(table.rows.state.value, key = table.rows.key) { index, row ->
        NoScrollAnimation {
            DataTableRowView(scrollState = horizontalScrollState, table = table, cells = table.row(index, row))
        }
    }
}

@Composable
private fun InitViewPort(table: DataTable<*>) {
    LaunchedEffect(table.lazyState) {
        snapshotFlow { table.lazyState.layoutInfo }.collect { table.rows.setCellsMap(table.lazyState) }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { table.setViewport(it.size) }
            .focusable(false)
            .focusProperties { canFocus = false }
    )
}

@Suppress("FunctionName")
@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.DataTableHeaderRow(table: DataTable<*>, scrollableState: ScrollState, isEmbedded: Boolean) {
    if (table.config.value.isHeadered && table.config.value.isHeaderSticky && !isEmbedded) {
        stickyHeader(key = table.headersTag) {
            InitViewPort(table)

            NoScrollAnimation {
                DataTableRowView(scrollState = scrollableState, table = table, cells = table.headers())
            }
        }
    } else if (table.config.value.isHeadered && (!table.config.value.isHeaderSticky || isEmbedded)) {
        stickyHeader { InitViewPort(table) }

        item(key = table.headersTag) {
            NoScrollAnimation {
                DataTableRowView(scrollState = scrollableState, table = table, cells = table.headers())
            }
        }
    } else {
        stickyHeader { InitViewPort(table) }
    }
}