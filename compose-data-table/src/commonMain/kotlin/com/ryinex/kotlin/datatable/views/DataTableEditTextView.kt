package com.ryinex.kotlin.datatable.views

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.insert
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.selectAll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ryinex.kotlin.datatable.data.DataTableCell
import com.ryinex.kotlin.datatable.data.DataTableEditTextConfig
import com.ryinex.kotlin.datatable.data.DataTableMobileTextEdit
import com.ryinex.kotlin.datatable.data.DataTableSaveConfirm
import com.ryinex.kotlin.datatable.data.DataTableSaveTrigger

@Composable
internal fun <VALUE, DATA : Any> TextEditCell(
    modifier: Modifier = Modifier,
    cell: DataTableCell<VALUE, *>,
    editTextConfig: DataTableEditTextConfig<VALUE, DATA>,
    textStyle: TextStyle,
    enabledInteractions: Boolean,
    onSaveEdit: (old: VALUE, text: String) -> Unit,
    textMapper: (VALUE) -> String,
    onCancelEdit: () -> Unit
) {
    val originalText = remember(cell.state.value) { textMapper(cell.state.value) }

    val isMobile = remember { isMobile }
    var popupContent by remember { mutableStateOf<(DataTableSaveConfirm.Dialog<VALUE, Any>)?>(null) }
    var capturedFirst by remember { mutableStateOf(false) }
    var enableFocusChanges by remember { mutableStateOf(true) }
    var textFieldValue by remember { mutableStateOf(originalText) }
    val cancelEditProcess =
        remember {
            {
                textFieldValue = originalText
                onCancelEdit()
            }
        }
    val confirmSaveProcess =
        remember {
            {
                if (originalText != textFieldValue) {
                    when (editTextConfig.saveConfirm) {
                        is DataTableSaveConfirm.Auto -> onSaveEdit(cell.state.value, textFieldValue)

                        is DataTableSaveConfirm.Dialog<*, *> ->
                            popupContent =
                                editTextConfig.saveConfirm as DataTableSaveConfirm.Dialog<VALUE, Any>
                    }
                } else {
                    cancelEditProcess()
                }
            }
        }

    val itemsModifier =
        remember {
            Modifier
                .onFocusChanged {
                    if (!enableFocusChanges) return@onFocusChanged
                    if (
                        !it.isFocused &&
                        capturedFirst &&
                        editTextConfig.saveTrigger.contains(DataTableSaveTrigger.LEAVE_FOCUS)
                    ) {
                        confirmSaveProcess()
                    } else if (!it.isFocused && capturedFirst) {
                        cancelEditProcess()
                    } else {
                        capturedFirst = true
                    }
                }
                .focusRequester(cell.properties.childFocusRequester)
                .onEscPress(true) { cancelEditProcess() }
        }

    if (!isMobile) {
        val state = rememberTextFieldState(textFieldValue)

        LaunchedEffect(state.text) { textFieldValue = state.text.toString() }

        TextStateEditCell(
            modifier =
            modifier
                .onEnterPreviewPress(editTextConfig.saveTrigger.contains(DataTableSaveTrigger.ENTER_PRESS)) {
                    state.onEnterPress(it.isShiftPressed)
                    if (it.isShiftPressed) return@onEnterPreviewPress true

                    confirmSaveProcess()

                    true
                }
                .then(itemsModifier),
            cell = cell,
            editTextConfig = editTextConfig,
            state = state,
            textStyle = textStyle,
            enabledInteractions = enabledInteractions,
            onKeyboardAction = { confirmSaveProcess() }
        )
    } else {
        TextValueEditCell(
            modifier =
            modifier
                .onEnterPreviewPress(
                    editTextConfig.mobileEditConfig is DataTableMobileTextEdit.InPlace ||
                        editTextConfig.saveTrigger.contains(
                            DataTableSaveTrigger.ENTER_PRESS
                        )
                ) {
                    if (it.isShiftPressed) return@onEnterPreviewPress true

                    confirmSaveProcess()
                    true
                }
                .then(itemsModifier),
            cell = cell,
            editTextConfig = editTextConfig,
            originalText = originalText,
            textFieldValue = textFieldValue,
            onChangeTextFieldValue = { textFieldValue = it },
            textStyle = textStyle,
            enabledInteractions = enabledInteractions,
            onSaveEdit = { _, _ -> confirmSaveProcess() },
            onCancelEdit = cancelEditProcess,
            keyboardAction = { confirmSaveProcess() },
            enableFocusChanges = { enableFocusChanges = it }
        )
    }

    if (popupContent != null) {
        Dialog(onDismissRequest = { cancelEditProcess() }) {
            popupContent!!.content(
                cell.data,
                cell.state.value,
                textFieldValue,
                { cancelEditProcess() },
                { onSaveEdit(cell.state.value, textFieldValue) }
            )
        }
    }
}

@Composable
private fun <VALUE, DATA : Any> TextStateEditCell(
    modifier: Modifier = Modifier,
    cell: DataTableCell<VALUE, *>,
    editTextConfig: DataTableEditTextConfig<VALUE, DATA>,
    state: TextFieldState,
    textStyle: TextStyle,
    enabledInteractions: Boolean,
    onKeyboardAction: (() -> Unit)?
) {
    LaunchedEffect(Unit) {
        runCatching { cell.properties.childFocusRequester.requestFocus() }
        if (editTextConfig.isEditable) state.edit { selectAll() }
    }

    BasicTextField(
        modifier = modifier,
        state = state,
        textStyle = textStyle,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
        interactionSource = cell.properties.childInteractionSource,
        inputTransformation = editTextConfig.inputTransformation,
        outputTransformation = editTextConfig.outputTransformation,
        lineLimits = editTextConfig.lineLimits,
        keyboardOptions = editTextConfig.keyboardOptions,
        onKeyboardAction = onKeyboardAction?.let { action -> KeyboardActionHandler { action() } },
        readOnly = !editTextConfig.isEditable,
        enabled = enabledInteractions
    )
}

@Composable
internal fun <VALUE, DATA : Any> TextValueEditCell(
    modifier: Modifier = Modifier,
    cell: DataTableCell<VALUE, *>,
    editTextConfig: DataTableEditTextConfig<VALUE, DATA>,
    originalText: String,
    textFieldValue: String,
    onChangeTextFieldValue: (String) -> Unit,
    textStyle: TextStyle,
    enabledInteractions: Boolean,
    keyboardAction: (() -> Unit)?,
    onCancelEdit: () -> Unit,
    onSaveEdit: (old: VALUE, text: String) -> Unit,
    enableFocusChanges: (Boolean) -> Unit
) {
    EditDialog(
        state = editTextConfig.mobileEditConfig,
        onCancelEdit = {
            enableFocusChanges(true)
            onChangeTextFieldValue(originalText)
            onCancelEdit()
        },
        onSaveEdit = {
            enableFocusChanges(true)
            onSaveEdit(cell.state.value, textFieldValue)
        },
        enableFocusChanges = enableFocusChanges
    ) {
        BasicTextField(
            modifier = modifier,
            value = textFieldValue,
            onValueChange = onChangeTextFieldValue,
            textStyle = textStyle,
            maxLines = editTextConfig.maxLines,
            interactionSource = cell.properties.childInteractionSource,
            readOnly = !editTextConfig.isEditable,
            enabled = enabledInteractions,
            keyboardOptions = editTextConfig.keyboardOptions,
            keyboardActions = KeyboardActions(onDone = { keyboardAction?.invoke() }),
            cursorBrush = SolidColor(textStyle.color)
        ) {
            if (textFieldValue.isBlank() && editTextConfig.mobileEditConfig is DataTableMobileTextEdit.Dialog) {
                Text(
                    text = editTextConfig.mobileEditConfig.textFieldHint(),
                    style = textStyle.copy(color = textStyle.color.copy(alpha = 0.7f))
                )
            }
            it()
        }
    }
}

@Composable
private fun EditDialog(
    state: DataTableMobileTextEdit,
    onCancelEdit: () -> Unit,
    onSaveEdit: () -> Unit,
    enableFocusChanges: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    when (state) {
        is DataTableMobileTextEdit.InPlace -> content()
        is DataTableMobileTextEdit.Dialog -> {
            LaunchedEffect(Unit) { enableFocusChanges(false) }
            Dialog(
                onDismissRequest = onCancelEdit,
                properties = DialogProperties(dismissOnClickOutside = false)
            ) { state.content(content, { onCancelEdit() }, { onSaveEdit() }) }
        }
    }
}

internal suspend fun <VALUE, DATA : Any> saveEdit(
    data: DATA,
    originalText: String,
    oldValue: VALUE,
    stateText: String,
    onEditStateValue: (VALUE) -> Unit,
    onEditViewText: (String) -> Unit,
    onSaveEdit: suspend (data: DATA, old: VALUE, text: String) -> VALUE?
) {
    var result = false
    onEditViewText(stateText)
    runCatching {
        val value = onSaveEdit(data, oldValue, stateText)
        result = value != null
        if (value != null && value != oldValue) {
            onEditStateValue(value)
        } else if (value == oldValue) {
            onEditViewText(originalText)
        }
    }
    if (!result) onEditViewText(originalText)
}

private fun Modifier.onEnterPreviewPress(enabled: Boolean, onPress: (KeyEvent) -> Boolean): Modifier {
    return this.onPreviewKeyEvent {
        val isEnter = it.key == Key.Enter || it.key == Key.NumPadEnter
        if (enabled && it.type == KeyEventType.KeyDown && isEnter) {
            return@onPreviewKeyEvent onPress(it)
        }
        false
    }
}

private fun Modifier.onEscPress(enabled: Boolean, onPress: () -> Unit): Modifier {
    return this.onKeyEvent {
        val isEsc = it.key == Key.Escape
        if (enabled && it.type == KeyEventType.KeyUp && isEsc) {
            onPress()
            return@onKeyEvent true
        }
        false
    }
}

private fun TextFieldState.onEnterPress(isShiftPressed: Boolean) {
    if (isShiftPressed) {
        edit { insert(selection.min, "\n") }
    } else {
        edit { revertAllChanges() }
    }
}