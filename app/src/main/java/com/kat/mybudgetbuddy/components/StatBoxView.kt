package com.kat.mybudgetbuddy.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material3.Icon
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
import com.kat.mybudgetbuddy.utils.formatAmount
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun StatBox(
    title: String,
    amount: Double,
    isIncome: Boolean,
    showNegativeAmount: Boolean
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
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isIncome) Icons.Default.ArrowCircleUp else Icons.Default.ArrowCircleDown,
                            contentDescription = "Transaction Type",
                            tint = if (isIncome) colorResource(id = R.color.income_color) else colorResource(
                                id = R.color.expense_color
                            )
                        )

                        Text(
                            title,
                            fontSize = 18.sp,
                            color = if (isDarkMode) Color.White else colorResource(id = R.color.text_color),
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .offset(x = titleOffsetX.value.dp)
                        )
                    }

                    Text(
                        text = if (showNegativeAmount && amount > 0) "- ${formatAmount(amount)}" else formatAmount(amount),
                        fontWeight = FontWeight.Bold,
                        fontSize = 23.sp,
                        color = if (isDarkMode) Color.White else colorResource(id = R.color.text_color),
                        modifier = Modifier.padding(top = 8.dp)
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
                    if (isIncome) "Tap the incomes tab to track your latest earnings" else
                        "Tap the expenses tab to track your latest purchase",
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .widthIn(max = 200.dp), // Limit width to keep card compact
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp, // Smaller font size to keep card compact
                    color = if (isDarkMode) Color.White else colorResource(id = R.color.text_color)
                )
            }
        }
    }
}