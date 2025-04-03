package com.example.mybudgetbuddy.components


import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mybudgetbuddy.R

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector? = null,
    onChange: (() -> Unit)? = null,
) {
    val isDarkMode = isSystemInDarkTheme()

    val textColor = if (isDarkMode) Color.White else colorResource(id = R.color.text_color)
    val labelColor = if (isDarkMode) Color.LightGray else Color.DarkGray

    // Gradient colors similar to your Swift implementation
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
            .height(45.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                color = textColor,
                fontSize = 16.sp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Display icon if provided
                    icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            tint = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f),
                            modifier = Modifier
                                .size(30.dp)
                                .padding(horizontal = 5.dp)
                        )
                    }

                    // Content box with placeholder
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = label,
                                color = labelColor,
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                }
            }
        )
    }

    LaunchedEffect(value) {
        onChange?.invoke()
    }
}