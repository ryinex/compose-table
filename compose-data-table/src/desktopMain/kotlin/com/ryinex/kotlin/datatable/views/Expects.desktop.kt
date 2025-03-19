package com.ryinex.kotlin.datatable.views

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
private fun scrollBarStyle(): ScrollbarStyle {
    return LocalScrollbarStyle.current.copy(
        unhoverColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
        hoverColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 1.0f),
        thickness = 8.dp
    )
}

@Composable
actual fun DataTableVerticalScrollbar(modifier: Modifier, state: LazyListState) {
    androidx.compose.foundation.VerticalScrollbar(
        modifier = modifier,
        adapter = rememberScrollbarAdapter(scrollState = state),
        style = scrollBarStyle()
    )
}

@Composable
actual fun DataTableHorizontalScrollbar(modifier: Modifier, state: LazyListState) {
    androidx.compose.foundation.HorizontalScrollbar(
        modifier = modifier,
        adapter = rememberScrollbarAdapter(scrollState = state),
        style = scrollBarStyle()
    )
}

@Composable
actual fun DataTableHorizontalScrollbar(modifier: Modifier, state: ScrollState) {
    androidx.compose.foundation.HorizontalScrollbar(
        modifier = modifier,
        adapter = rememberScrollbarAdapter(scrollState = state),
        style = scrollBarStyle()
    )
}

internal actual val isMobile: Boolean = false

@Composable
internal actual fun NoScrollAnimation(content: @Composable () -> Unit) {
    content()
}