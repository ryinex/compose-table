package com.ryinex.kotlin.csvviewer.presentation.content

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ryinex.kotlin.datatable.views.DataTableHorizontalScrollbar
import com.ryinex.kotlin.datatable.views.DataTableVerticalScrollbar

@Composable
internal fun Dummy() {
    val horizontal = rememberScrollState()
    val lazyListState = rememberLazyListState()
    var height by remember { mutableStateOf(0) }
    println("height: $height")
    Row(modifier = Modifier.onGloballyPositioned { height = it.size.height }) {
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(modifier = Modifier.heightIn((468 + 500).densityDp), state = lazyListState) {
                items(1000) { index ->
                    println("index: $index")
                    DummyRow(index = index, scroll = horizontal)
                }
            }
            DataTableHorizontalScrollbar(state = horizontal)
        }

        DataTableVerticalScrollbar(state = lazyListState)
    }
}

@Composable
private fun DummyRow(index: Int, scroll: ScrollState) {
    Row(modifier = Modifier.horizontalScroll(scroll)) {
        TextCell(width = 48, text = index.toString())
        ('A'..'Z').forEach { char ->
            TextCell(width = 160, text = char.toString())
        }
    }
}

@Composable
private fun TextCell(width: Int, text: String) {
    Text(modifier = Modifier.width(width.dp).border(Dp.Hairline, Color.Black), text = text)
}

internal val Int.densityDp @Composable get() = (this / LocalDensity.current.density).dp