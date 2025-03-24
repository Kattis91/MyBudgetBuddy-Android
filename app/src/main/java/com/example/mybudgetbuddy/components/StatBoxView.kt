package com.example.mybudgetbuddy.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mybudgetbuddy.R
import com.example.mybudgetbuddy.utils.formatAmount

@Composable
fun StatBox(
    title: String,
    amount: Double,
    isIncome: Boolean,
    showNegativeAmount: Boolean
    ) {

    val isDarkMode = isSystemInDarkTheme()

    StyledCard {

        Row {
            Icon(
                imageVector = if (isIncome) Icons.Default.ArrowCircleUp else Icons.Default.ArrowCircleDown,
                contentDescription = "Transaction Type",
                tint = if (isIncome) colorResource(id = R.color.income_color) else colorResource(id = R.color.expense_color)
            )

            Text(title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (isDarkMode) Color.White else colorResource(id = R.color.text_color),
                modifier = Modifier.padding(start = 8.dp)
            )

        }

        Column(
            modifier = Modifier.padding(vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = if (showNegativeAmount && amount > 0) "- ${formatAmount(amount)}" else formatAmount(amount),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = if (isDarkMode) Color.White else colorResource(id = R.color.text_color)
            )
        }
    }
}