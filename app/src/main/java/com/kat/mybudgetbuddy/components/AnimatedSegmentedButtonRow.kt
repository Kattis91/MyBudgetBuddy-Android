package com.kat.mybudgetbuddy.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimatedSegmentedButtonRow(
    options: List<String>,
    selectedIndex: Int,
    onSelectionChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val cornerRadius = 12.dp
    val density = LocalDensity.current.density

    // Entry animations
    val offsetY = remember { Animatable(300f) }
    val entryScale = remember { Animatable(0.6f) }
    val rotation = remember { Animatable(0f) }

    val isDarkMode = isSystemInDarkTheme()

    // Run entry animations
    LaunchedEffect(Unit) {
        // Slide in
        launch {
            offsetY.animateTo(0f, tween(700, easing = FastOutSlowInEasing))
        }

        // Grow in
        launch {
            delay(200)
            entryScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }

        // Bounce forward (simulate z-bounce)
        launch {
            delay(800)
            entryScale.animateTo(1.25f, tween(200)) // Bigger zoom in
            entryScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy))
        }
    }

    // For selection indicator animation
    val transition = updateTransition(targetState = selectedIndex, label = "segmentTransition")

    // Selection indicator scale animation (for bounce effect when changing selection)
    val selectionScale = remember { Animatable(1f) }

    // Trigger bounce animation when selection changes
    LaunchedEffect(selectedIndex) {
        if (selectedIndex >= 0) {
            selectionScale.snapTo(0.9f)
            selectionScale.animateTo(1.1f, tween(150))
            selectionScale.animateTo(1f, spring(stiffness = Spring.StiffnessMedium))
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .graphicsLayer {
                translationY = offsetY.value
                scaleX = entryScale.value
                scaleY = entryScale.value
                rotationY = rotation.value
                cameraDistance = 12 * density
            }
            .height(38.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(if(isDarkMode) Color.DarkGray else Color(0xFFEBEBEB))
    ) {
        // Calculate item width as a fraction of total width
        val itemWidthFraction = 1f / options.size

        // FIXED: Use BoxWithConstraints to get the exact width for calculations
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val boxWidth = maxWidth

            // Animated selection indicator position - calculate absolute position
            val indicatorOffset by transition.animateDp(
                transitionSpec = {
                    tween(durationMillis = 250, easing = FastOutSlowInEasing)
                },
                label = "indicatorOffset"
            ) { targetIndex ->
                (boxWidth * targetIndex * itemWidthFraction)
            }

            // Selection background indicator (the black brick)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(boxWidth * itemWidthFraction)
                    .padding(4.dp)
                    .offset(x = indicatorOffset)
                    .graphicsLayer {
                        scaleX = selectionScale.value
                        scaleY = selectionScale.value
                    }
                    .clip(RoundedCornerShape(cornerRadius - 4.dp))
                    .background(Color.Black.copy(alpha = 0.8f))
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(cornerRadius - 4.dp))
            )

            // Row of options
            Row(modifier = Modifier.fillMaxSize()) {
                options.forEachIndexed { index, text ->
                    val isSelected = index == selectedIndex

                    // ADDED: Pulse animation for specific items (for "Variable" at index 2)
                    val pulseTransition = rememberInfiniteTransition(label = "pulseTransition")
                    val pulseScale = if (index == 2 && index != selectedIndex) {
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

                    // Animate text color
                    val textColor by transition.animateColor(
                        transitionSpec = { tween(300) },
                        label = "textColor"
                    ) { targetIndex ->
                        if (index == targetIndex) Color.White else if(isDarkMode) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.7f)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .graphicsLayer {
                                scaleX = pulseScale
                                scaleY = pulseScale
                            }
                            .clickable { onSelectionChanged(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = text,
                            color = textColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    // Add dividers between unselected options
                    if (index < options.size - 1 && index != selectedIndex && index + 1 != selectedIndex) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(18.dp)
                                .align(Alignment.CenterVertically)
                                .alpha(0.3f)
                                .background(Color.Gray)
                        )
                    }
                }
            }
        }
    }
}


