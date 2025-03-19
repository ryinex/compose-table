package com.ryinex.kotlin.datatable.views

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun DataTableVerticalScrollbar(modifier: Modifier = Modifier, state: LazyListState)

@Composable
expect fun DataTableHorizontalScrollbar(modifier: Modifier = Modifier, state: LazyListState)

@Composable
expect fun DataTableHorizontalScrollbar(modifier: Modifier = Modifier, state: ScrollState)

internal expect val isMobile: Boolean

@Composable
internal expect fun NoScrollAnimation(content: @Composable () -> Unit)