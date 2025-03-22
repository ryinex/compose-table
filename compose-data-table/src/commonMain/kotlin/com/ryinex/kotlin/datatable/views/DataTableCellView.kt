package com.ryinex.kotlin.datatable.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.ryinex.kotlin.datatable.data.DataTableCell
import com.ryinex.kotlin.datatable.data.DataTableCellConfig
import com.ryinex.kotlin.datatable.data.DataTableCellLocation
import com.ryinex.kotlin.datatable.data.DataTableCellProperties
import com.ryinex.kotlin.datatable.data.DataTableColumnConfig
import com.ryinex.kotlin.datatable.data.DataTableColumnLayout
import com.ryinex.kotlin.datatable.data.DataTableColumnViewPort
import com.ryinex.kotlin.datatable.data.DataTableEditTextConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun <VALUE, DATA : Any> RowScope.CellContainer(
    cell: DataTableCell<VALUE, DATA>,
    columnViewPortConfig: DataTableColumnViewPort,
    config: DataTableCellConfig,
    columnConfig: DataTableColumnConfig,
    enableInteractions: Boolean,
    isHeader: Boolean,
    isFocusable: Boolean,
    isClickable: Boolean,
    onNavigate: (from: DataTableCellProperties, to: DataTableCellLocation) -> Unit,
    onDoubleClickDrag: () -> Unit,
    onDrag: (Int) -> Unit,
    onDragFinished: () -> Unit,
    onClick: (DataTableCellProperties) -> Unit,
    onWidth: (Int) -> Unit
) {
    val direction = LocalLayoutDirection.current
    val dragState =
        rememberDraggableState { onDrag((if (direction == LayoutDirection.Ltr) it else -it).toInt()) }
    val cellProperties = remember(cell) { cell.properties }
    val interactionSource = remember(cell) { cellProperties.cellInteractionSource }
    val focusRequester = remember(cell) { cellProperties.cellFocusRequester }
    val isFocused = interactionSource.collectIsFocusedAsState().value
    val isHovered = interactionSource.collectIsHoveredAsState().value
    val isChildFocused = cell.properties.childInteractionSource.collectIsFocusedAsState().value
    Surface(
        modifier = config.modifier
            .CellModifier(columnViewPortConfig)
            .onGloballyPositioned {
                cellProperties.height = it.size.height
                onWidth(it.size.width)
            }
            .fillMaxHeight()
            .focusProperties { canFocus = isFocusable && enableInteractions && !isChildFocused }
            .focusRequester(focusRequester)
            .moveFocusOnTab()
            .keyNavigation(location = cell.properties.location) { onNavigate(cellProperties, it) }
            .onEnterPress(config.enterFocusChild) {
                runCatching { cellProperties.childFocusRequester.requestFocus() }
            }
            .hoverable(interactionSource = interactionSource, enabled = enableInteractions)
            .focusable(
                interactionSource = interactionSource,
                enabled = isFocusable && enableInteractions
            )
            .clip(config.shape)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = isClickable && enableInteractions,
                onLongClick = { onClick(cellProperties) },
                onDoubleClick = {
                    onClick(cellProperties)
                    kotlin.runCatching { cellProperties.childFocusRequester.requestFocus() }
                },
                onClick = { }
            ),
        color = config.backgroundColor ?: MaterialTheme.colorScheme.surface,
        contentColor = config.color ?: MaterialTheme.colorScheme.onSurface,
        shape = config.shape,
        tonalElevation = if (isFocused || isChildFocused) 16.dp else if (isHovered) 1.dp else 0.dp
    ) {
        Box {
            Provided(config) { cell.view(cell) }

            if (isHeader && columnConfig.isResizable && enableInteractions) {
                ResizeDragHandle(
                    presentation = columnConfig,
                    dragState = dragState,
                    onDragFinished = onDragFinished,
                    onDoubleClickDrag = onDoubleClickDrag
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun <VALUE, DATA : Any> TextEditableCell(
    modifier: Modifier = Modifier,
    cell: DataTableCell<VALUE, DATA>,
    editTextConfig: DataTableEditTextConfig<VALUE, DATA>,
    coroutineScope: CoroutineScope,
    enabledInteractions: Boolean,
    textMapper: (VALUE) -> String,
    onFocusCell: () -> Unit
) {
    val config = remember(cell.state.value) { cell.config(cell.state.value) }
    val text = remember(cell.state.value) { textMapper(cell.state.value) }
    val cellTextStyle = remember(config) { config.textStyle } ?: LocalTextStyle.current
    val cellProperties = remember(cell) { cell.properties }
    var viewText by remember(text) { mutableStateOf(text) }
    val textStyle = remember(viewText, text) {
        cellTextStyle.copy(
            color = if (viewText == text) cellTextStyle.color else cellTextStyle.color.copy(alpha = 0.7f)
        )
    }
    var isEditMode by remember { mutableStateOf(false) }
    if (isEditMode) {
        TextEditCell(
            textMapper = textMapper,
            cell = cell,
            textStyle = textStyle,
            editTextConfig = editTextConfig,
            enabledInteractions = enabledInteractions,
            onSaveEdit = { old, newText ->
                onFocusCell()
                isEditMode = false
                if (textMapper(old) == newText) return@TextEditCell
                if (coroutineScope.isActive) {
                    coroutineScope.launch {
                        saveEdit(
                            data = cell.data,
                            originalText = text,
                            oldValue = old,
                            stateText = newText,
                            onEditStateValue = { cell.state.value = it },
                            onEditViewText = { viewText = it },
                            onSaveEdit = editTextConfig.onConfirmEdit
                        )
                    }
                }
            },
            onCancelEdit = {
                onFocusCell()
                isEditMode = false
            }
        )
    } else {
        TextViewCell(
            modifier = modifier
                .combinedClickable(
                    interactionSource = null,
                    indication = null,
                    enabled = enabledInteractions,
                    onDoubleClick = { isEditMode = true },
                    onClick = { }
                )
                .focusRequester(cellProperties.childFocusRequester)
                .onFocusChanged { if (it.isFocused) isEditMode = true }
                .focusable(interactionSource = cellProperties.childInteractionSource),
            text = viewText,
            textStyle = textStyle,
            textAlign = config.textAlign
        )
    }
}

@Composable
internal fun TextViewCell(
    modifier: Modifier = Modifier,
    text: String,
    textAlign: TextAlign?,
    textStyle: TextStyle = LocalTextStyle.current
) {
    Text(modifier = modifier, text = text, style = textStyle, textAlign = textAlign)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BoxScope.ResizeDragHandle(
    presentation: DataTableColumnConfig,
    dragState: DraggableState,
    onDragFinished: () -> Unit,
    onDoubleClickDrag: () -> Unit
) {
    VerticalDivider(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .draggable(
                state = dragState,
                orientation = Orientation.Horizontal,
                onDragStopped = { onDragFinished() }
            )
            .pointerHoverIcon(PointerIcon.Hand)
            .focusProperties { canFocus = false }
            .combinedClickable(onDoubleClick = onDoubleClickDrag) {},
        thickness = 4.dp,
        color = presentation.resizeHandleColor
    )
}

@Composable
fun BoxScope.Provided(config: DataTableCellConfig, content: @Composable () -> Unit) = with(config) {
    val provides = arrayListOf<ProvidedValue<*>>()

    if (textStyle != null) {
        provides.add(LocalTextStyle provides textStyle)
    } else {
        provides.add(LocalTextStyle provides LocalTextStyle.current.copy(color = config.color))
    }
    if (isForceLtr) provides.add(LocalLayoutDirection provides LayoutDirection.Ltr)

    Box(modifier = Modifier.padding(padding).align(config.alignment)) {
        if (provides.isEmpty()) {
            content()
        } else {
            CompositionLocalProvider(values = provides.toTypedArray(), content = content)
        }
    }
}

@Composable
private fun Modifier.keyNavigation(
    focusManager: FocusManager = LocalFocusManager.current,
    location: DataTableCellLocation
): Modifier {
    return onKeyEvent {
        if (it.type == KeyEventType.KeyDown && it.key == Key.DirectionUp) {
            return@onKeyEvent location.moveUp() != null && focusManager.moveFocus(FocusDirection.Up)
        } else if (it.type == KeyEventType.KeyDown && it.key == Key.DirectionDown) {
            return@onKeyEvent location.moveDown() != null && focusManager.moveFocus(FocusDirection.Down)
        } else if (it.type == KeyEventType.KeyDown && it.key == Key.DirectionLeft) {
            return@onKeyEvent location.movePrevious() != null && focusManager.moveFocus(FocusDirection.Left)
        } else if (it.type == KeyEventType.KeyDown && it.key == Key.DirectionRight) {
            return@onKeyEvent location.moveNext() != null && focusManager.moveFocus(FocusDirection.Right)
        }

        false
    }
}

@Composable
private fun Modifier.keyNavigation(location: DataTableCellLocation, navTo: (DataTableCellLocation) -> Unit): Modifier {
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    return onKeyEvent {
        if (it.type == KeyEventType.KeyDown && it.key == Key.DirectionUp) {
            val navigation = location.moveUp() ?: return@onKeyEvent false
            navTo(navigation)
        } else if (it.type == KeyEventType.KeyDown && it.key == Key.DirectionDown) {
            val navigation = location.moveDown() ?: return@onKeyEvent false
            navTo(navigation)
        } else if (it.type == KeyEventType.KeyDown && it.key == Key.DirectionLeft) {
            val navigation = (if (isLtr) location.movePrevious() else location.moveNext()) ?: return@onKeyEvent false
            navTo(navigation)
        } else if (it.type == KeyEventType.KeyDown && it.key == Key.DirectionRight) {
            val navigation = (if (isLtr) location.moveNext() else location.movePrevious()) ?: return@onKeyEvent false
            navTo(navigation)
        }

        false
    }
}

private fun Modifier.onEnterPress(enabled: Boolean, onPress: () -> Unit): Modifier {
    return this.onKeyEvent {
        val isEnter = it.key == Key.Enter || it.key == Key.NumPadEnter
        if (enabled && it.type == KeyEventType.KeyDown && isEnter) {
            onPress()
            return@onKeyEvent true
        }
        false
    }
}

@Composable
private fun Modifier.moveFocusOnTab(focusManager: FocusManager = LocalFocusManager.current) = onPreviewKeyEvent {
    if (it.type == KeyEventType.KeyDown && it.key == Key.Tab) {
        focusManager.moveFocus(
            if (it.isShiftPressed) {
                FocusDirection.Previous
            } else {
                FocusDirection.Next
            }
        )
        return@onPreviewKeyEvent true
    }
    false
}

context(RowScope)
@Composable
@Suppress("FunctionName")
internal fun Modifier.CellModifier(column: DataTableColumnViewPort): Modifier {
    return when (column.layout) {
        DataTableColumnLayout.FixedEquals -> this.weight(1f)
        DataTableColumnLayout.FixedWighted ->
            this
                .nullableWeight(column.weight)
                .minWidthModifier(column.width)

        DataTableColumnLayout.FixedFree ->
            this
                .nullableWeight(column.weight)
                .widthModifier(column.width)

        DataTableColumnLayout.ScrollableKeepLargest -> this.widthIn(column.width.densityDp)
        DataTableColumnLayout.ScrollableKeepInitial -> this.widthModifier(column.width)
    }
}

@Composable
private fun Modifier.widthModifier(width: Int): Modifier {
    return if (width != 0) this.width(width.densityDp) else this
}

@Composable
private fun Modifier.minWidthModifier(width: Int): Modifier {
    return if (width != 0) this.widthIn(width.densityDp) else this
}

context(RowScope)
private fun Modifier.nullableWeight(weight: Float?): Modifier {
    return this.then(if (weight != null && weight > 0) Modifier.weight(weight) else Modifier)
}