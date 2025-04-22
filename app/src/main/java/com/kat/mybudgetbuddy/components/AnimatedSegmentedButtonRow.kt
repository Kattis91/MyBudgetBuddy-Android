package com.kat.mybudgetbuddy.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimatedSegmentedButtonRow(
    options: List<String>,
    selectedIndex: Int,
    onSelectionChanged: (Int) -> Unit
) {
    val density = LocalDensity.current.density
    val offsetY = remember { Animatable(300f) }
    val scale = remember { Animatable(0.6f) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Slide in
        launch {
            offsetY.animateTo(0f, tween(700, easing = FastOutSlowInEasing))
        }

        // Grow in
        launch {
            delay(200)
            scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }

        // Bounce forward (simulate z-bounce)
        launch {
            delay(800)
            scale.animateTo(1.25f, tween(200)) // Bigger zoom in
            scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy))
        }
    }


    Box(
        modifier = Modifier
            .graphicsLayer {
                translationY = offsetY.value
                scaleX = scale.value
                scaleY = scale.value
                rotationY = rotation.value
                cameraDistance = 12 * density
            }
    ) {
        SegmentedButtonRow(
            options = options,
            selectedIndex = selectedIndex,
            onSelectionChanged = onSelectionChanged
        )
    }
}


