package com.miyabi_hiroshi.app.meaninglesskeyboard.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

@Immutable
data class KeyboardColors(
    val keyBackground: Color,
    val keyBackgroundPressed: Color,
    val specialKeyBackground: Color,
    val keyText: Color,
    val keyboardBackground: Color,
    val previewBackground: Color,
    val previewText: Color,
    val indicatorText: Color,
    val indicatorDot: Color,
    val indicatorDotActive: Color
)

@Immutable
data class KeyboardDimensions(
    val keyHeight: Dp = 48.dp,
    val keyCornerRadius: Dp = 6.dp,
    val keyHorizontalPadding: Dp = 3.dp,
    val keyVerticalPadding: Dp = 4.dp,
    val rowSpacing: Dp = 0.dp,
    val keyboardPadding: Dp = 4.dp,
    val previewElevation: Dp = 8.dp
)

val LightKeyboardColors = KeyboardColors(
    keyBackground = Color(0xFFFFFFFF),
    keyBackgroundPressed = Color(0xFFD0D0D0),
    specialKeyBackground = Color(0xFFB0BEC5),
    keyText = Color(0xFF212121),
    keyboardBackground = Color(0xFFE0E0E0),
    previewBackground = Color(0xFF424242),
    previewText = Color(0xFFFFFFFF),
    indicatorText = Color(0xFF757575),
    indicatorDot = Color(0xFFBDBDBD),
    indicatorDotActive = Color(0xFF616161)
)

val DarkKeyboardColors = KeyboardColors(
    keyBackground = Color(0xFF424242),
    keyBackgroundPressed = Color(0xFF616161),
    specialKeyBackground = Color(0xFF37474F),
    keyText = Color(0xFFE0E0E0),
    keyboardBackground = Color(0xFF212121),
    previewBackground = Color(0xFF757575),
    previewText = Color(0xFFFFFFFF),
    indicatorText = Color(0xFF9E9E9E),
    indicatorDot = Color(0xFF616161),
    indicatorDotActive = Color(0xFFBDBDBD)
)

val KeyLabelStyle = TextStyle(
    fontSize = 20.sp,
    fontWeight = FontWeight.Normal
)

val LocalKeyboardColors = staticCompositionLocalOf { LightKeyboardColors }
val LocalKeyboardDimensions = staticCompositionLocalOf { KeyboardDimensions() }

@Composable
fun keyboardColors(darkTheme: Boolean = isSystemInDarkTheme()): KeyboardColors =
    if (darkTheme) DarkKeyboardColors else LightKeyboardColors
