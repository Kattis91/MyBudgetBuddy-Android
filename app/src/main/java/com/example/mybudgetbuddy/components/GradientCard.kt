package com.example.mybudgetbuddy.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp

@Composable
fun StyledCard(content: @Composable ColumnScope.() -> Unit) {
    val isDarkMode = isSystemInDarkTheme()

    val gradientColors = if (isDarkMode) {
        listOf(Color(40, 40, 45), Color(28, 28, 32))
    } else {
        listOf(Color(252, 242, 230), Color(245, 235, 220))
    }
    val rowReference = remember { mutableStateOf<LayoutCoordinates?>(null) }

    Box(
        modifier = Modifier
            .padding(vertical = 6.dp)
            .shadow(
                elevation = if (isDarkMode) 2.dp else 1.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = if (isDarkMode) 0.35f else 0.25f),
                ambientColor = Color.Black.copy(alpha = if (isDarkMode) 0.35f else 0.25f),
                clip = false
            )
            .graphicsLayer(
                shadowElevation = 4f,
                shape = RoundedCornerShape(16.dp)
            )
            .offset(x = (3).dp, y = (-3).dp)
            .background(
                Brush.horizontalGradient(
                    colors = gradientColors,
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .border(
                width = if (isDarkMode) 0.6.dp else 0.8.dp,
                color = Color.White.copy(alpha = if (isDarkMode) 0.3f else 0.4f),
                shape = RoundedCornerShape(16.dp)
            )
            .onGloballyPositioned { coordinates ->
                rowReference.value = coordinates
            }
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}


