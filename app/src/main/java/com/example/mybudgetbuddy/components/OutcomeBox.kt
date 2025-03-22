package com.example.mybudgetbuddy.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mybudgetbuddy.R
import com.example.mybudgetbuddy.utils.formatAmount
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

    StyledCard {
        Row {
            Icon(
                imageVector = if (isNegative) Icons.Default.Warning else Icons.Default.CheckCircle,
                contentDescription = "Outcome",
                tint = if (isNegative) Color.Red else Color.Green
            )

            Text(
                "Outcome",
                color = if (isDarkMode) Color.White else colorResource(id = R.color.text_color),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 8.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = String.format("%.1f%%", percentage),
                color = if (isDarkMode) Color.White else colorResource(id = R.color.text_color),
                fontWeight = FontWeight.Bold
            )
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
                        if (isNegative) Color.Red else Color.Green,
                        RoundedCornerShape(4.dp)
                    )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            Text(
                text = if (isNegative) "- ${formatAmount(outcome)}" else formatAmount(outcome),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = if (isDarkMode) Color.White else colorResource(id = R.color.text_color)
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
