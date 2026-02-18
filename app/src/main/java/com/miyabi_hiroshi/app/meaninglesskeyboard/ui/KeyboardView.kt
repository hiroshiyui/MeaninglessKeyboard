package com.miyabi_hiroshi.app.meaninglesskeyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.miyabi_hiroshi.app.meaninglesskeyboard.keyboard.KeyDef
import com.miyabi_hiroshi.app.meaninglesskeyboard.keyboard.KeyboardDisplayState
import com.miyabi_hiroshi.app.meaninglesskeyboard.keyboard.KeyboardState
import com.miyabi_hiroshi.app.meaninglesskeyboard.ui.theme.KeyboardDimensions
import com.miyabi_hiroshi.app.meaninglesskeyboard.ui.theme.LocalKeyboardColors
import com.miyabi_hiroshi.app.meaninglesskeyboard.ui.theme.LocalKeyboardDimensions
import com.miyabi_hiroshi.app.meaninglesskeyboard.ui.theme.keyboardColors

@Composable
fun KeyboardView(
    keyboardState: KeyboardState,
    onKeyAction: (KeyDef) -> Unit,
    onKeyReleased: (KeyDef) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val displayState by keyboardState.displayState.collectAsState()
    val colors = keyboardColors()
    val dimensions = KeyboardDimensions()

    val context = LocalContext.current
    val density = LocalDensity.current
    val displayMetrics = context.resources.displayMetrics
    val maxHeight = with(density) { (displayMetrics.heightPixels / 2).toDp() }

    var previewKey by remember { mutableStateOf<KeyDef?>(null) }
    var previewPosition by remember { mutableStateOf(IntOffset.Zero) }

    CompositionLocalProvider(
        LocalKeyboardColors provides colors,
        LocalKeyboardDimensions provides dimensions
    ) {
        Box {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .heightIn(max = maxHeight)
                    .background(colors.keyboardBackground)
                    .swipeDetector(
                        onSwipeLeft = { keyboardState.swipeLeft() },
                        onSwipeRight = { keyboardState.swipeRight() }
                    )
                    .padding(dimensions.keyboardPadding)
            ) {
                PackIndicator(
                    packName = displayState.packName,
                    packIndex = displayState.packIndex,
                    packCount = displayState.packCount
                )

                displayState.currentLayout?.rows?.forEach { row ->
                    KeyboardRow(
                        row = row,
                        onKeyPressed = onKeyAction,
                        onKeyReleased = onKeyReleased,
                        onPreviewShow = { keyDef, position ->
                            previewKey = keyDef
                            previewPosition = position
                        },
                        onPreviewHide = { previewKey = null }
                    )
                }
            }

            previewKey?.let { key ->
                KeyPreviewPopup(
                    label = key.label,
                    position = previewPosition
                )
            }
        }
    }
}

@Composable
private fun KeyboardRow(
    row: com.miyabi_hiroshi.app.meaninglesskeyboard.keyboard.KeyRow,
    onKeyPressed: (KeyDef) -> Unit,
    onKeyReleased: (KeyDef) -> Unit,
    onPreviewShow: (KeyDef, IntOffset) -> Unit,
    onPreviewHide: () -> Unit
) {
    val totalWeight = row.keys.sumOf { it.width }.toFloat()

    Row(modifier = Modifier.fillMaxWidth()) {
        row.keys.forEach { keyDef ->
            KeyButton(
                keyDef = keyDef,
                modifier = Modifier.weight(keyDef.width.toFloat() / totalWeight),
                onKeyPressed = onKeyPressed,
                onKeyReleased = onKeyReleased,
                onPreviewShow = onPreviewShow,
                onPreviewHide = onPreviewHide
            )
        }
    }
}

private fun Modifier.swipeDetector(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    threshold: Float = 100f
): Modifier = this.pointerInput(Unit) {
    var totalDragX = 0f
    detectHorizontalDragGestures(
        onDragStart = { totalDragX = 0f },
        onDragEnd = {
            if (totalDragX > threshold) {
                onSwipeRight()
            } else if (totalDragX < -threshold) {
                onSwipeLeft()
            }
        },
        onDragCancel = { totalDragX = 0f },
        onHorizontalDrag = { _, dragAmount ->
            totalDragX += dragAmount
        }
    )
}
