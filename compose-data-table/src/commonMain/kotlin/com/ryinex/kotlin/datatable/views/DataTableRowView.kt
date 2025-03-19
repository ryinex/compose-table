package com.ryinex.kotlin.datatable.views

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.ryinex.kotlin.datatable.data.DataTable

@Composable
internal fun DataTableRowView(
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
    table: DataTable<*>,
    cells: List<@Composable RowScope.() -> Unit>
) {
    Box(
        modifier =
        modifier.RowModifier(
            isFixed = !table.config.value.column.layout.isScrollable(),
            scrollState = scrollState,
            isEnabled = table.enableInteractions.value
        )
    ) {
        Row(
            modifier = Modifier.widthIn(table.viewport.value.width.densityDp).height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(table.config.value.horizontalSpacing.densityDp)
        ) {
            cells.forEachIndexed { index, cell -> cell() }
        }
    }
}

@Suppress("FunctionName")
private fun Modifier.RowModifier(isFixed: Boolean, scrollState: ScrollState, isEnabled: Boolean): Modifier {
    return when {
        isFixed -> this.fillMaxWidth()
        else -> this.horizontalScroll(scrollState, enabled = isEnabled)
    }
}

internal val Int.densityDp @Composable get() = (this / LocalDensity.current.density).dp