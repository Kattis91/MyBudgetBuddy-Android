package com.kat.mybudgetbuddy.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SegmentedButtonRow(
    options: List<String>,
    selectedIndex: Int,
    onSelectionChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val cornerRadius = 12.dp

    // Create transition for smooth animations
    val transition = updateTransition(targetState = selectedIndex, label = "segmentedButton")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color(0xFFEEEEEE))
    ) {
        // Animated selection indicator
        val indicatorOffset by transition.animateDp(
            transitionSpec = {
                tween(durationMillis = 300, easing = FastOutSlowInEasing)
            },
            label = "indicatorOffset"
        ) { currentIndex ->
            (currentIndex * (100f / options.size)).dp
        }

        // Selection background
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(1f / options.size)
                .padding(4.dp)
                .offset(x = indicatorOffset)
                .clip(RoundedCornerShape(cornerRadius - 4.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF303030),
                            Color(0xFF1A1A1A)
                        )
                    )
                )
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(cornerRadius - 4.dp))
        )

        // Options Row
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            options.forEachIndexed { index, text ->
                val isSelected = index == selectedIndex

                // Animate text color change
                val textColor by transition.animateColor(
                    transitionSpec = {
                        tween(durationMillis = 300)
                    },
                    label = "textColor"
                ) { currentSelectedIndex ->
                    if (index == currentSelectedIndex) Color.White else Color(0xFF666666)
                }

                // Scale animation for special items (like your "Variable" option)
                val pulseTransition = rememberInfiniteTransition(label = "pulseTransition")
                val scale = if (index == 2 && !isSelected) {  // Assuming "Variable" is at index 2
                    pulseTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulseScale"
                    ).value
                } else {
                    1f
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onSelectionChanged(index)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        color = textColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Dividers between options (only for unselected adjacent items)
                if (index < options.size - 1 && index != selectedIndex && index + 1 != selectedIndex) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .align(Alignment.CenterVertically)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Gray.copy(alpha = 0.1f),
                                        Color.Gray.copy(alpha = 0.3f),
                                        Color.Gray.copy(alpha = 0.1f)
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
}