package com.ryinex.kotlin.csvviewer.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    locale: String = "en",
    useDynamicColors: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorsScheme =
        if (!useDarkTheme) {
            LightColors
        } else {
            DarkColors
        }

    MaterialTheme(
        colorScheme = colorsScheme,
        content = content,
        typography = extendedTypography()
    )
}