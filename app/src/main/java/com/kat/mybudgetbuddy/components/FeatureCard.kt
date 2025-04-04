package com.kat.mybudgetbuddy.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.kat.mybudgetbuddy.R
import com.kat.mybudgetbuddy.data.FeatureItem

@Composable
fun FeatureCard(feature: FeatureItem) {
    val isDarkMode = isSystemInDarkTheme()
    val textColor = if (isDarkMode) Color.White else colorResource(id = R.color.text_color)

    val rowReference = remember { mutableStateOf<LayoutCoordinates?>(null) }
    // Swift-like gradient colors
    val gradientColors = if (isDarkMode) {
        listOf(Color.DarkGray, Color.Black)
    } else {
        listOf(
            Color(red = 245f/255f, green = 247f/255f, blue = 245f/255f),
            Color(red = 240f/255f, green = 242f/255f, blue = 240f/255f)
        )
    }

    Card(
        modifier = Modifier
            .zIndex(0f)
            .fillMaxWidth()
            .padding(horizontal = 23.dp)
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
    ){
        Row (
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = "",
                tint = colorResource(id = R.color.expense_color)
            )
            if (feature.text != null) {
                Text(
                    text = feature.text,
                    modifier = Modifier.padding(16.dp),
                    color = textColor
                )
            } else if (feature.content != null) {
                feature.content.invoke()
            }
        }
    }
}