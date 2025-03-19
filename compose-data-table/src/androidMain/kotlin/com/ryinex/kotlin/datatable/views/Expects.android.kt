package com.ryinex.kotlin.datatable.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier

@Composable
actual fun DataTableVerticalScrollbar(modifier: Modifier, state: LazyListState) {
}

@Composable
actual fun DataTableHorizontalScrollbar(modifier: Modifier, state: LazyListState) {
}

@Composable
actual fun DataTableHorizontalScrollbar(modifier: Modifier, state: ScrollState) {
}

internal actual val isMobile: Boolean = true

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal actual fun NoScrollAnimation(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalOverscrollConfiguration.provides(null)) {
        content()
    }
}