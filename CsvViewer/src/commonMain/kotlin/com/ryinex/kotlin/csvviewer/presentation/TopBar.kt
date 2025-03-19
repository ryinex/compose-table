package com.ryinex.kotlin.csvviewer.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.ryinex.kotlin.datatable.data.DataTableColumnLayout
import com.ryinex.kotlin.datatable.data.DataTableConfig
import com.ryinex.kotlin.datatable.data.DataTableEditTextConfig
import com.ryinex.kotlin.datatable.data.DataTableMobileTextEdit
import com.ryinex.kotlin.datatable.data.DataTableSaveConfirm
import com.ryinex.kotlin.datatable.data.DataTableSaveTrigger
import com.ryinex.kotlin.datatable.views.DataTableHorizontalScrollbar

@Composable
internal fun TopBar(
    initialConfig: DataTableConfig,
    initialEditConfig: DataTableEditTextConfig<Any, Any>,
    isDarkTheme: Boolean,
    toggleDarkTheme: () -> Unit,
    onApply: (DataTableConfig, DataTableEditTextConfig<Any, Any>) -> Unit
) {
    val state = rememberScrollState()
    val colorFieldWidth = remember { 50.dp }
    val backgroundColor: Color = MaterialTheme.colorScheme.background
    val contentColor: Color = MaterialTheme.colorScheme.onBackground
    var backgroundRed by remember { mutableStateOf((backgroundColor.red * 255).toInt().toString()) }
    var backgroundGreen by remember { mutableStateOf((backgroundColor.green * 255).toInt().toString()) }
    var backgroundBlue by remember { mutableStateOf((backgroundColor.blue * 255).toInt().toString()) }

    var contentRed by remember { mutableStateOf((contentColor.red * 255).toInt().toString()) }
    var contentGreen by remember { mutableStateOf((contentColor.green * 255).toInt().toString()) }
    var contentBlue by remember { mutableStateOf((contentColor.blue * 255).toInt().toString()) }
    val resultBackgroundColor =
        remember(backgroundRed, backgroundGreen, backgroundBlue) {
            Color(
                backgroundRed.toIntOrNull() ?: 0,
                backgroundGreen.toIntOrNull() ?: 0,
                backgroundBlue.toIntOrNull() ?: 0
            )
        }
    val backgroundBackgroundColor by remember(backgroundRed, backgroundGreen, backgroundBlue) {
        val isNewWhitish =
            (backgroundRed.toIntOrNull() ?: 0) >= 200 &&
                (backgroundGreen.toIntOrNull() ?: 0) >= 200 &&
                (backgroundBlue.toIntOrNull() ?: 0) >= 200
        val isNewBlackish =
            (backgroundRed.toIntOrNull() ?: 0) <= 55 &&
                (backgroundGreen.toIntOrNull() ?: 0) <= 55 &&
                (backgroundBlue.toIntOrNull() ?: 0) <= 55
        mutableStateOf(
            if (isNewWhitish && !isDarkTheme) {
                contentColor
            } else if (isNewWhitish) {
                backgroundColor
            } else if (isNewBlackish && isDarkTheme) {
                contentColor
            } else {
                backgroundColor
            }
        )
    }
    val resultContentColor =
        remember(contentRed, contentGreen, contentBlue) {
            Color(contentRed.toIntOrNull() ?: 0, contentGreen.toIntOrNull() ?: 0, contentBlue.toIntOrNull() ?: 0)
        }
    var showLayoutDropdown by remember { mutableStateOf(false) }
    var layout: DataTableColumnLayout by remember { mutableStateOf(initialConfig.column.layout) }

    var horizontalSpacing by remember { mutableStateOf(initialConfig.horizontalSpacing.toString()) }
    var verticalSpacing by remember { mutableStateOf(initialConfig.verticalSpacing.toString()) }
    var isEnterFocusChild by remember { mutableStateOf(initialConfig.column.cell.enterFocusChild) }

    var extended by remember { mutableStateOf(false) }
    var editConfig by remember { mutableStateOf(initialEditConfig) }

    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { extended = !extended }) {
                Icon(
                    imageVector = if (extended) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }

            TextButton(onClick = {
                val column = initialConfig.column.copy(layout = layout)
                val cell =
                    initialConfig.column.cell.copy(
                        backgroundColor = resultBackgroundColor,
                        color = resultContentColor,
                        enterFocusChild = isEnterFocusChild
                    )
                onApply(
                    initialConfig.copy(
                        column = column.copy(cell = cell),
                        horizontalSpacing = horizontalSpacing.toIntOrNull() ?: 0,
                        verticalSpacing = verticalSpacing.toIntOrNull() ?: 0
                    ),
                    editConfig
                )
            }) { Text("Apply") }

//            IconButton(onClick = toggleDarkTheme) {
//                Icon(
//                    modifier = Modifier.padding(8.dp),
//                    painter = painterResource(if (isDarkTheme) Res.drawable.ic_light_mode else Res.drawable.ic_dark_mode),
//                    contentDescription = null,
//                    tint = MaterialTheme.colorScheme.primary
//                )
//            }

            Row(
                modifier = Modifier.horizontalScroll(state = state),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Background",
                    modifier = Modifier.background(backgroundBackgroundColor),
                    color = resultBackgroundColor
                )

                AppMinimalTextField(
                    modifier = Modifier.width(colorFieldWidth),
                    value = backgroundRed,
                    onValueChange = {
                        if ((it.toIntOrNull() != null && it.toInt() in 0..255) || it.isBlank()) backgroundRed = it
                    },
                    hint = "Red"
                )

                AppMinimalTextField(
                    modifier = Modifier.width(colorFieldWidth),
                    value = backgroundGreen,
                    onValueChange = {
                        if ((it.toIntOrNull() != null && it.toInt() in 0..255) || it.isBlank()) backgroundGreen = it
                    },
                    hint = "Green"
                )

                AppMinimalTextField(
                    modifier = Modifier.width(colorFieldWidth),
                    value = backgroundBlue,
                    onValueChange = {
                        if ((it.toIntOrNull() != null && it.toInt() in 0..255) || it.isBlank()) backgroundBlue = it
                    },
                    hint = "Blue"
                )

                Text(
                    text = "Content",
                    modifier = Modifier.background(resultBackgroundColor),
                    color = resultContentColor
                )

                AppMinimalTextField(
                    modifier = Modifier.width(colorFieldWidth),
                    value = contentRed,
                    onValueChange = {
                        if ((it.toIntOrNull() != null && it.toInt() in 0..255) || it.isBlank()) contentRed = it
                    },
                    hint = "Red"
                )

                AppMinimalTextField(
                    modifier = Modifier.width(colorFieldWidth),
                    value = contentGreen,
                    onValueChange = {
                        if ((it.toIntOrNull() != null && it.toInt() in 0..255) || it.isBlank()) contentGreen = it
                    },
                    hint = "Green"
                )

                AppMinimalTextField(
                    modifier = Modifier.width(colorFieldWidth),
                    value = contentBlue,
                    onValueChange = {
                        if ((it.toIntOrNull() != null && it.toInt() in 0..255) || it.isBlank()) contentBlue = it
                    },
                    hint = "Blue"
                )

                Text(
                    text = "Layout: ${layout::class.simpleName ?: ""}",
                    modifier = Modifier.clickable { showLayoutDropdown = true }
                )

                DropdownMenu(expanded = showLayoutDropdown, onDismissRequest = { showLayoutDropdown = false }) {
                    DropdownMenuItem(
                        text = { Text("ScrollableKeepInitial") },
                        onClick = { layout = DataTableColumnLayout.ScrollableKeepInitial }
                    )
                    DropdownMenuItem(
                        text = { Text("ScrollableKeepLargest") },
                        onClick = { layout = DataTableColumnLayout.ScrollableKeepLargest }
                    )
                    DropdownMenuItem(
                        text = { Text("FixedEquals") },
                        onClick = { layout = DataTableColumnLayout.FixedEquals }
                    )
                    DropdownMenuItem(
                        text = { Text("FixedWighted") },
                        onClick = { layout = DataTableColumnLayout.FixedWighted }
                    )
                }

                Text("Spacing: ")

                AppMinimalTextField(
                    modifier = Modifier.width(120.dp),
                    value = horizontalSpacing.toString(),
                    onValueChange = {
                        if ((it.toIntOrNull() != null && it.toInt() in 0..50) || it.isBlank()) horizontalSpacing = it
                    },
                    hint = "Horizontal 0 to 50"
                )

                AppMinimalTextField(
                    modifier = Modifier.width(120.dp),
                    value = verticalSpacing.toString(),
                    onValueChange = {
                        if ((it.toIntOrNull() != null && it.toInt() in 0..50) || it.isBlank()) verticalSpacing = it
                    },
                    hint = "Vertical 0 to 50"
                )

                Text(text = "Enter press focus child: ")

                Checkbox(
                    checked = isEnterFocusChild,
                    onCheckedChange = { isEnterFocusChild = it }
                )
            }
        }

        if (extended) {
            Row(
                modifier = Modifier.horizontalScroll(state = state),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EditConfigRow(editConfig, onChange = { editConfig = it })
            }
        }

        DataTableHorizontalScrollbar(state = state)
    }
}

@Composable
private fun EditConfigRow(
    editConfig: DataTableEditTextConfig<Any, Any>,
    onChange: (DataTableEditTextConfig<Any, Any>) -> Unit
) {
    var showSaveEditDropdown by remember { mutableStateOf(false) }
    var showSaveConfirmDropdown by remember { mutableStateOf(false) }
    var showMobileEditDropdown by remember { mutableStateOf(false) }

    Text(
        text = "Editable: ",
        modifier = Modifier.clickable { showSaveEditDropdown = true }
    )

    Checkbox(
        checked = editConfig.isEditable,
        onCheckedChange = { onChange(editConfig.copy(isEditable = it)) }
    )

    Text(
        text = "Save edit trigger: ${editConfig.saveTrigger.joinToString { it.name }}",
        modifier = Modifier.clickable { showSaveEditDropdown = true }
    )

    DropdownMenu(expanded = showSaveEditDropdown, onDismissRequest = { showSaveEditDropdown = false }) {
        DataTableSaveTrigger.entries.forEach { trigger ->
            DropdownMenuItem(
                text = { Text(trigger.name) },
                onClick = { onChange(editConfig.copy(saveTrigger = listOf(trigger))) }
            )
        }
    }

    Text(
        text = "Save edit confirm: ${editConfig.saveConfirm::class.simpleName}",
        modifier = Modifier.clickable { showSaveConfirmDropdown = true }
    )

    DropdownMenu(expanded = showSaveConfirmDropdown, onDismissRequest = { showSaveConfirmDropdown = false }) {
        DropdownMenuItem(
            text = { Text("Auto") },
            onClick = { onChange(editConfig.copy(saveConfirm = DataTableSaveConfirm.Auto)) }
        )

        DropdownMenuItem(
            text = { Text("Dialog") },
            onClick = {
                onChange(
                    editConfig.copy(
                        saveConfirm =
                        DataTableSaveConfirm.Dialog<Any, Any> { _, _, _, onCancel, onConfirm ->
                            SaveChangesDialog(onCancel, onConfirm)
                        }
                    )
                )
            }
        )
    }

    Text(
        text = "Mobile edit: ${editConfig.mobileEditConfig::class.simpleName}",
        modifier = Modifier.clickable { showMobileEditDropdown = true }
    )

    DropdownMenu(expanded = showMobileEditDropdown, onDismissRequest = { showMobileEditDropdown = false }) {
        DropdownMenuItem(
            text = { Text("In place") },
            onClick = { onChange(editConfig.copy(mobileEditConfig = DataTableMobileTextEdit.InPlace)) }
        )

        DropdownMenuItem(
            text = { Text("Dialog") },
            onClick = {
                onChange(
                    editConfig.copy(
                        mobileEditConfig =
                        DataTableMobileTextEdit.Dialog(
                            textFieldHint = { "Your input" },
                            content = { text, onCancel, onConfirm ->
                                MobileChangesDialog(text, onCancel, onConfirm)
                            }
                        )
                    )
                )
            }
        )
    }
}

@Composable
private fun SaveChangesDialog(onCancel: () -> Unit, onConfirm: () -> Unit) {
    val focusRequester = remember { FocusRequester() }

    Card {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Save Changes ?")
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                LaunchedEffect(Unit) { focusRequester.requestFocus() }
                TextButton(onClick = onCancel) { Text("Cancel") }
                TextButton(
                    modifier = Modifier.focusRequester(focusRequester),
                    onClick = onConfirm
                ) { Text("Confirm") }
            }
        }
    }
}

@Composable
internal fun MobileChangesDialog(textField: @Composable () -> Unit, onCancel: () -> Unit, onSave: () -> Unit) {
    Card {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            textField()

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onCancel) { Text("Cancel") }
                TextButton(onClick = onSave) { Text("Save") }
            }
        }
    }
}

@Composable
private fun AppMinimalTextField(
    modifier: Modifier = Modifier,
    value: String,
    hint: String,
    onValueChange: (String) -> Unit
) {
    BasicTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onBackground),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
        singleLine = true,
        maxLines = 1,
        decorationBox = { innerTextField ->
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = hint,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.labelSmall
                )
                innerTextField()
            }
        }
    )
}