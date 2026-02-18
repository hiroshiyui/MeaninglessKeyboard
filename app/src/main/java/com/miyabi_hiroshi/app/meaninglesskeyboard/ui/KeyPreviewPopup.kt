package com.miyabi_hiroshi.app.meaninglesskeyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.miyabi_hiroshi.app.meaninglesskeyboard.ui.theme.LocalKeyboardColors
import com.miyabi_hiroshi.app.meaninglesskeyboard.ui.theme.LocalKeyboardDimensions

@Composable
fun KeyPreviewPopup(
    label: String,
    position: IntOffset,
    modifier: Modifier = Modifier
) {
    val colors = LocalKeyboardColors.current
    val dimensions = LocalKeyboardDimensions.current
    val density = LocalDensity.current
    val popupWidth = 56.dp
    val popupHeight = 56.dp

    val offsetX = with(density) { position.x.toDp() - popupWidth / 2 }
    val offsetY = with(density) { position.y.toDp() - popupHeight - 8.dp }

    Popup(
        alignment = Alignment.TopStart,
        offset = IntOffset(
            with(density) { offsetX.roundToPx() },
            with(density) { offsetY.roundToPx() }
        ),
        properties = PopupProperties(clippingEnabled = false)
    ) {
        Box(
            modifier = Modifier
                .size(popupWidth, popupHeight)
                .shadow(dimensions.previewElevation, RoundedCornerShape(8.dp))
                .background(colors.previewBackground, RoundedCornerShape(8.dp))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = colors.previewText,
                fontSize = 28.sp
            )
        }
    }
}
