package com.ryinex.kotlin.datatable.data

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction

data class DataTableConfig(
    val verticalSpacing: Int,
    val horizontalSpacing: Int,
    val isScrollable: Boolean,
    val isIndexed: Boolean,
    val isHeadered: Boolean,
    val isHeaderSticky: Boolean,
    val column: DataTableColumnConfig
) {
    val defaultHeaderConfig = column.cell

    companion object {
        @Composable
        fun default(
            verticalSpacing: Int = 0,
            horizontalSpacing: Int = 0,
            isScrollable: Boolean = true,
            isIndexed: Boolean = true,
            isHeadered: Boolean = true,
            isHeaderSticky: Boolean = true,
            column: (DataTableColumnConfig) -> DataTableColumnConfig = { it }
        ): DataTableConfig {
            val color = MaterialTheme.colorScheme.onBackground
            val backgroundColor = MaterialTheme.colorScheme.background
            return remember {
                default(
                    color = color,
                    backgroundColor = backgroundColor,
                    verticalSpacing = verticalSpacing,
                    horizontalSpacing = horizontalSpacing,
                    isScrollable = isScrollable,
                    isIndexed = isIndexed,
                    isHeadered = isHeadered,
                    isHeaderSticky = isHeaderSticky,
                    column = column
                )
            }
        }

        fun default(
            color: Color,
            backgroundColor: Color,
            verticalSpacing: Int = 0,
            horizontalSpacing: Int = 0,
            isScrollable: Boolean = true,
            isIndexed: Boolean = true,
            isHeadered: Boolean = true,
            isHeaderSticky: Boolean = true,
            column: (DataTableColumnConfig) -> DataTableColumnConfig = { it }
        ): DataTableConfig {
            return DataTableConfig(
                verticalSpacing = verticalSpacing,
                horizontalSpacing = horizontalSpacing,
                isScrollable = isScrollable,
                isIndexed = isIndexed,
                isHeadered = isHeadered,
                isHeaderSticky = isHeaderSticky,
                column =
                column(
                    DataTableColumnConfig.default(
                        color = color,
                        backgroundColor = backgroundColor
                    )
                )
            )
        }
    }
}

internal enum class DataTableColumnSort {
    Ascending,
    Descending,
    None
}

sealed class DataTableColumnLayout {
    data object FixedEquals : DataTableColumnLayout()

    data object FixedWighted : DataTableColumnLayout()

    internal data object FixedFree : DataTableColumnLayout()

    data object ScrollableKeepInitial : DataTableColumnLayout()

    data object ScrollableKeepLargest : DataTableColumnLayout()

    fun isScrollable(): Boolean = when (this) {
        FixedEquals,
        FixedWighted,
        FixedFree
        -> false

        ScrollableKeepInitial,
        ScrollableKeepLargest
        -> true
    }

    fun isWeighted(): Boolean = when (this) {
        FixedWighted -> true

        FixedEquals,
        FixedFree,
        ScrollableKeepInitial,
        ScrollableKeepLargest
        -> false
    }
}

data class DataTableColumnConfig(
    val layout: DataTableColumnLayout,
    val weight: Float,
    val isResizable: Boolean,
    val resizeHandleColor: Color,
    val cell: DataTableCellConfig
) {
    companion object {
        internal fun default(
            layout: DataTableColumnLayout = DataTableColumnLayout.ScrollableKeepInitial,
            isResizable: Boolean = true,
            color: Color,
            backgroundColor: Color,
            resizeHandleColor: Color = Color.Transparent,
            weight: Float = if (layout.isWeighted()) 1f else 0f,
            cell: DataTableCellConfig =
                DataTableCellConfig.default(
                    color = color,
                    backgroundColor = backgroundColor
                )
        ): DataTableColumnConfig {
            return DataTableColumnConfig(
                layout = layout,
                isResizable = isResizable,
                resizeHandleColor = resizeHandleColor,
                weight = weight,
                cell = cell
            )
        }
    }
}

data class DataTableCellConfig(
    val modifier: Modifier,
    val color: Color,
    val backgroundColor: Color,
    val shape: Shape,
    val padding: PaddingValues,
    val textStyle: TextStyle?,
    val isForceLtr: Boolean,
    val enterFocusChild: Boolean,
    val alignment: Alignment
) {
    companion object {
        internal fun default(
            modifier: Modifier = Modifier,
            color: Color,
            backgroundColor: Color,
            shape: Shape = RectangleShape,
            padding: PaddingValues = PaddingValues(),
            textStyle: TextStyle? = null,
            isForceLtr: Boolean = false,
            enterFocusChild: Boolean = true,
            alignment: Alignment = Alignment.CenterStart
        ): DataTableCellConfig {
            return DataTableCellConfig(
                modifier = modifier,
                color = color,
                backgroundColor = backgroundColor,
                shape = shape,
                padding = padding,
                textStyle = textStyle,
                isForceLtr = isForceLtr,
                enterFocusChild = enterFocusChild,
                alignment = alignment
            )
        }
    }
}

data class DataTableEditTextConfig<VALUE, DATA : Any>(
    val isEditable: Boolean,
    val saveTrigger: List<DataTableSaveTrigger>,
    val saveConfirm: DataTableSaveConfirm,
    val mobileEditConfig: DataTableMobileTextEdit,
    val keyboardOptions: KeyboardOptions,
    val inputTransformation: InputTransformation?,
    val outputTransformation: OutputTransformation?,
    val lineLimits: TextFieldLineLimits,
    val maxLines: Int,
    val onConfirmEdit: suspend (data: DATA, old: VALUE, text: String) -> VALUE?
) {
    companion object {
        fun <VALUE, DATA : Any> default(
            isEditable: Boolean = false,
            saveTrigger: List<DataTableSaveTrigger> = DataTableSaveTrigger.entries,
            saveConfirm: DataTableSaveConfirm = DataTableSaveConfirm.Auto,
            mobileBrowserEditConfig: DataTableMobileTextEdit = DataTableMobileTextEdit.InPlace,
            inputTransformation: InputTransformation? = null,
            outputTransformation: OutputTransformation? = null,
            keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default,
            maxLines: Int = Int.MAX_VALUE,
            onSaveEdit: suspend (data: DATA, old: VALUE, text: String) -> VALUE? = { _, _, _ -> null }
        ): DataTableEditTextConfig<VALUE, DATA> {
            return DataTableEditTextConfig(
                isEditable = isEditable,
                saveTrigger = saveTrigger,
                saveConfirm = saveConfirm,
                mobileEditConfig = mobileBrowserEditConfig,
                onConfirmEdit = onSaveEdit,
                inputTransformation = inputTransformation,
                outputTransformation = outputTransformation,
                lineLimits = lineLimits,
                maxLines = maxLines,
                keyboardOptions = keyboardOptions
            )
        }
    }
}

internal data class DataTableCellProperties(
    val cellInteractionSource: MutableInteractionSource,
    val cellFocusRequester: FocusRequester,
    val childInteractionSource: MutableInteractionSource,
    val childFocusRequester: FocusRequester,
    val location: DataTableCellLocation,
    var height: Int = 0
) {
    internal companion object {
        @Composable
        fun create(location: DataTableCellLocation) = DataTableCellProperties(
            cellInteractionSource = remember { MutableInteractionSource() },
            cellFocusRequester = remember { FocusRequester() },
            childInteractionSource = remember { MutableInteractionSource() },
            childFocusRequester = remember { FocusRequester() },
            location = location
        )
    }
}

enum class DataTableSaveTrigger {
    LEAVE_FOCUS,
    ENTER_PRESS
}

sealed interface DataTableSaveConfirm {
    data object Auto : DataTableSaveConfirm

    data class Dialog<VALUE, DATA : Any>(
        val content: @Composable (
            data: DATA,
            oldValue: VALUE,
            text: String,
            onCancel: () -> Unit,
            onConfirm: () -> Unit
        ) -> Unit
    ) : DataTableSaveConfirm
}

sealed interface DataTableMobileTextEdit {
    data object InPlace : DataTableMobileTextEdit

    data class Dialog(
        val textFieldHint: @Composable () -> String,
        val content: @Composable (
            textField: @Composable () -> Unit,
            onCancel: () -> Unit,
            onConfirm: () -> Unit
        ) -> Unit
    ) : DataTableMobileTextEdit
}