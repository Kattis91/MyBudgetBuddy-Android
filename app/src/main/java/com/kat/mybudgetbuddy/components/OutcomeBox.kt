package com.kat.mybudgetbuddy.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kat.mybudgetbuddy.R
import com.kat.mybudgetbuddy.utils.formatAmount
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@SuppressLint("DefaultLocale")
@Composable
fun OutcomeBox(
    income: Double,
    expense: Double
) {
    val outcome = income - expense

    val percentage =
        if (income > 0) {
            (outcome / income) * 100
        } else if (expense > 0) {
            -100 // Show -100% when there's no income but has expenses
        } else {
            0 // Only show 0% when both income and expenses are 0
        }

    val isNegative: Boolean = outcome < 0

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
                    Row {
                        Icon(
                            imageVector = if (isNegative) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = "Outcome",
                            tint = if (isNegative) colorResource(id = R.color.expense_color) else colorResource(
                                id = R.color.income_color
                            )
                        )

                        Text(
                            text = stringResource(R.string.outcome),
                            color = if (isDarkMode) Color.White else colorResource(id = R.color.text_color),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = "${String.format("%.2f", percentage.toDouble())}%",
                            color = if (isDarkMode) Color.White else colorResource(id = R.color.text_color),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Bar
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Background track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    )

                    // Progress indicator
                    Box(
                        modifier = Modifier
                            .width(
                                with(LocalDensity.current) {
                                    (abs(minOf(maxOf(percentage.toFloat() / 100f, -1f), 1f)) *
                                            LocalConfiguration.current.screenWidthDp.dp.toPx() * 0.7f).toDp()
                                }
                            )
                            .height(8.dp)
                            .background(
                                if (isNegative) colorResource(id = R.color.expense_color) else colorResource(
                                    id = R.color.income_color
                                ),
                                RoundedCornerShape(4.dp)
                            )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    Text(
                        text = if (isNegative) "- ${formatAmount(outcome)}" else formatAmount(
                            outcome
                        ),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = if (isDarkMode) Color.White else colorResource(id = R.color.text_color)
                    )
                    Spacer(modifier = Modifier.weight(1f))
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
                     text = stringResource(R.string.this_percentage_shows),
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
