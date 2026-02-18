package com.miyabi_hiroshi.app.meaninglesskeyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import com.miyabi_hiroshi.app.meaninglesskeyboard.keyboard.KeyDef
import com.miyabi_hiroshi.app.meaninglesskeyboard.ui.theme.KeyLabelStyle
import com.miyabi_hiroshi.app.meaninglesskeyboard.ui.theme.LocalKeyboardColors
import com.miyabi_hiroshi.app.meaninglesskeyboard.ui.theme.LocalKeyboardDimensions
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun KeyButton(
    keyDef: KeyDef,
    modifier: Modifier = Modifier,
    onKeyPressed: (KeyDef) -> Unit = {},
    onKeyReleased: (KeyDef) -> Unit = {},
    onPreviewShow: (KeyDef, IntOffset) -> Unit = { _, _ -> },
    onPreviewHide: () -> Unit = {}
) {
    val colors = LocalKeyboardColors.current
    val dimensions = LocalKeyboardDimensions.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    var isPressed by remember(keyDef) { mutableStateOf(false) }
    var repeatJob by remember(keyDef) { mutableStateOf<Job?>(null) }
    var keyPosition by remember { mutableStateOf(IntOffset.Zero) }
    var keyWidth by remember { mutableStateOf(0) }

    // Clean up preview and repeat job if this composable is disposed or keyDef changes
    DisposableEffect(keyDef) {
        onDispose {
            if (isPressed) {
                onPreviewHide()
            }
            repeatJob?.cancel()
        }
    }

    val isSpecialKey = keyDef.keycode != null || keyDef.switchLayout != null
    val bgColor = when {
        isPressed -> colors.keyBackgroundPressed
        isSpecialKey -> colors.specialKeyBackground
        else -> colors.keyBackground
    }

    Box(
        modifier = modifier
            .height(dimensions.keyHeight)
            .padding(
                horizontal = dimensions.keyHorizontalPadding,
                vertical = dimensions.keyVerticalPadding
            )
            .background(bgColor, RoundedCornerShape(dimensions.keyCornerRadius))
            .onGloballyPositioned { coordinates ->
                val pos = coordinates.positionInRoot()
                keyPosition = IntOffset(pos.x.toInt(), pos.y.toInt())
                keyWidth = coordinates.size.width
            }
            .pointerInput(keyDef) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        // Show preview before dispatching action, so it's visible
                        // even if the action triggers a layout change (e.g. resetShift)
                        if (keyDef.output != null) {
                            val previewX = keyPosition.x + keyWidth / 2
                            val previewY = keyPosition.y
                            onPreviewShow(
                                keyDef,
                                IntOffset(previewX, previewY)
                            )
                        }
                        onKeyPressed(keyDef)
                        if (keyDef.repeatable) {
                            repeatJob = scope.launch {
                                delay(400)
                                while (true) {
                                    onKeyPressed(keyDef)
                                    delay(50)
                                }
                            }
                        }
                        tryAwaitRelease()
                        isPressed = false
                        repeatJob?.cancel()
                        repeatJob = null
                        onPreviewHide()
                        onKeyReleased(keyDef)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = keyDef.label,
            style = KeyLabelStyle,
            color = colors.keyText
        )
    }
}
