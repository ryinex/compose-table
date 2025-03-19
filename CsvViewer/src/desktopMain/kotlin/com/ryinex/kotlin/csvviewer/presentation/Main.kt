package com.ryinex.kotlin.csvviewer.presentation

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Csv Viewer"
    ) {
        App()
    }
}