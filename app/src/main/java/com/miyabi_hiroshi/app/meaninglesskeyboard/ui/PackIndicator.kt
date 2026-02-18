package com.miyabi_hiroshi.app.meaninglesskeyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miyabi_hiroshi.app.meaninglesskeyboard.ui.theme.LocalKeyboardColors

@Composable
fun PackIndicator(
    packName: String,
    packIndex: Int,
    packCount: Int,
    modifier: Modifier = Modifier
) {
    val colors = LocalKeyboardColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (packCount > 1) {
            repeat(packCount) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(6.dp)
                        .background(
                            if (index == packIndex) colors.indicatorDotActive
                            else colors.indicatorDot,
                            CircleShape
                        )
                )
            }
            Text(
                text = packName,
                color = colors.indicatorText,
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}
