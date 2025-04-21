package com.kat.mybudgetbuddy.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kat.mybudgetbuddy.R
import com.kat.mybudgetbuddy.models.BudgetPeriod
import com.kat.mybudgetbuddy.utils.formattedDateRange
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PeriodBox(
    period: BudgetPeriod,
) {
    val isDarkMode = isSystemInDarkTheme()
    val scale = remember { Animatable(0.5f) }
    val rotation = remember { Animatable(0f) }
    val alphaDescription = remember { Animatable(0f) }
    val titleOffsetX = remember { Animatable(400f) }

    var isFlipped by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        launch {
            titleOffsetX.animateTo(0f, tween(1000, easing = FastOutSlowInEasing))
        }
        delay(300)
        launch {
            scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }

    LaunchedEffect(isFlipped) {
        if (isFlipped) {
            rotation.animateTo(180f, tween(700, easing = FastOutSlowInEasing))
            alphaDescription.animateTo(1f, tween(400))
        } else {
            alphaDescription.snapTo(0f)
            rotation.animateTo(0f, tween(700, easing = FastOutSlowInEasing))
        }
    }

    Box(
        modifier = Modifier
            .graphicsLayer(
                scaleX = scale.value,
                scaleY = scale.value
            )
    ) {
        // Front of the card
        Box(
            modifier = Modifier
                .wrapContentSize()
                .clickable { isFlipped = !isFlipped }
                .graphicsLayer {
                    rotationY = rotation.value
                    cameraDistance = 12f * density
                    alpha = if (rotation.value <= 90f) 1f else 0f
                }
        ) {
            StyledCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Current Budget Period",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (isDarkMode) Color.White else colorResource(id = R.color.text_color),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        formattedDateRange(period.startDate, period.endDate),
                        fontWeight = FontWeight.Bold,
                        fontSize = 23.sp,
                        color = if (isDarkMode) Color.White else colorResource(id = R.color.text_color)
                    )
                }
            }
        }
        // Back of the card
        Box(
            modifier = Modifier
                .wrapContentSize()
                .clickable { isFlipped = !isFlipped }
                .graphicsLayer {
                    rotationY = rotation.value - 180f
                    cameraDistance = 12f * density
                    alpha = if (rotation.value > 90f) 1f else 0f
                }
        ) {
            StyledCard {
                Text(
                    "Tap 'Start New Period' when you're ready for a fresh budget slate. Pro tip: All your budget history is saved in the Overview tab for those 'how did I do last month?' moments",
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .widthIn(max = 300.dp), // Limit width to keep card compact
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp, // Smaller font size to keep card compact
                    color = if (isDarkMode) Color.White else colorResource(id = R.color.text_color)
                )
            }
        }
    }
}