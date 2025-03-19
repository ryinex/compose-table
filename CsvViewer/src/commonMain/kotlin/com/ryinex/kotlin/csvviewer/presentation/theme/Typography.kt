package com.ryinex.kotlin.csvviewer.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import composetable.csvviewer.generated.resources.Cairo_Bold
import composetable.csvviewer.generated.resources.Cairo_Medium
import composetable.csvviewer.generated.resources.Cairo_Regular
import composetable.csvviewer.generated.resources.Cairo_SemiBold
import composetable.csvviewer.generated.resources.Res
import org.jetbrains.compose.resources.Font

internal val CairoFontFamily: FontFamily
    @Composable get() =
        FontFamily(
            Font(Res.font.Cairo_Regular, FontWeight.Normal),
            Font(Res.font.Cairo_Medium, FontWeight.Medium),
            Font(Res.font.Cairo_SemiBold, FontWeight.SemiBold),
            Font(Res.font.Cairo_Bold, FontWeight.Bold)
        )

val mainFontFamily @Composable get() = CairoFontFamily

@Composable
internal fun extendedTypography(): Typography {
    val default = Typography()
    val addition = 2
    return Typography(
        displayLarge =
        default.displayLarge.copy(
            fontFamily = mainFontFamily,
            fontSize = (default.displayLarge.fontSize.value + addition).sp
        ),
        displayMedium =
        default.displayMedium.copy(
            fontFamily = mainFontFamily,
            fontSize = (default.displayMedium.fontSize.value + addition).sp
        ),
        displaySmall =
        default.displaySmall.copy(
            fontFamily = mainFontFamily,
            fontSize = (default.displaySmall.fontSize.value + addition).sp
        ),
        headlineLarge =
        default.headlineLarge.copy(
            fontFamily = mainFontFamily,
            fontSize = (default.headlineLarge.fontSize.value + addition).sp
        ),
        headlineMedium =
        default.headlineMedium.copy(
            fontFamily = mainFontFamily,
            fontSize = (default.headlineMedium.fontSize.value + addition).sp
        ),
        headlineSmall =
        default.headlineSmall.copy(
            fontFamily = mainFontFamily,
            fontSize = (default.headlineSmall.fontSize.value + addition).sp
        ),
        titleLarge =
        default.titleLarge.copy(
            fontFamily = mainFontFamily,
            fontSize = (default.titleLarge.fontSize.value + addition).sp
        ),
        titleMedium =
        default.titleMedium.copy(
            fontFamily = mainFontFamily,
            fontSize = (default.titleMedium.fontSize.value + addition).sp
        ),
        titleSmall =
        default.titleSmall.copy(
            fontFamily = mainFontFamily,
            fontSize = (default.titleSmall.fontSize.value + addition).sp
        ),
        bodyLarge =
        default.bodyLarge.copy(
            fontFamily = mainFontFamily,
            fontSize = (default.bodyLarge.fontSize.value + addition).sp
        ),
        bodyMedium =
        default.bodyMedium.copy(
            fontFamily = mainFontFamily,
            fontSize = (default.bodyMedium.fontSize.value + addition).sp
        ),
        bodySmall =
        default.bodySmall.copy(
            fontFamily = mainFontFamily,
            fontSize = (default.bodySmall.fontSize.value + addition).sp
        ),
        labelLarge =
        default.labelLarge.copy(
            fontFamily = mainFontFamily,
            fontSize = (default.labelLarge.fontSize.value + addition).sp
        ),
        labelMedium =
        default.labelMedium.copy(
            fontFamily = mainFontFamily,
            fontSynthesis = FontSynthesis.All,
            fontSize = (default.labelMedium.fontSize.value + addition).sp
        ),
        labelSmall =
        default.labelSmall.copy(
            fontFamily = mainFontFamily,
            fontSize = (default.labelSmall.fontSize.value + addition).sp
        )
    )
}