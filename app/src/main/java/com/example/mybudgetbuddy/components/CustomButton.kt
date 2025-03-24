package com.example.mybudgetbuddy.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mybudgetbuddy.R

@Composable
fun CustomButton(
    buttonText: String,
    onClick: () -> Unit,
    isIncome: Boolean,
    isExpense: Boolean,
    isThirdButton: Boolean,
    width: Int = 200
) {
    val addIncomeStart = Color(78 / 255f, 177 / 255f, 181 / 255f) // 76ADA1
    val addIncomeMiddle = Color(120 / 255f, 182 / 255f, 168 / 255f) // 78B6A8
    val addIncomeEnd = Color(57 / 255f, 111 / 255f, 134 / 255f) // BE607A

    // MARK: - Expense Button Colors
    val addExpenseStart = Color(174 / 255f, 41 / 255f, 114 / 255f) // AE2972
    val addExpenseMiddle = Color(233 / 255f, 93 / 255f, 115 / 255f) // E95D73
    val addExpenseEnd = Color(201 / 255f, 94 / 255f, 123 / 255f) // C95E7B

    val thirdButtonColor = colorResource(id = R.color.background_tint_dark)

    val background: Brush = when {
        isIncome -> Brush.linearGradient(
            colors = listOf(addIncomeStart, addIncomeMiddle, addIncomeEnd),
            start = Offset(0f, 0f),
            end = Offset(400f, 400f)
        )
        isExpense -> Brush.linearGradient(
            colors = listOf(addExpenseStart, addExpenseMiddle, addExpenseEnd),
            start = Offset(0f, 0f),
            end = Offset(400f, 400f)
        )
        isThirdButton -> SolidColor(thirdButtonColor)
        else -> SolidColor(Color.Gray)
    }

    Box(
        modifier = Modifier
            .then(if (width > 0) Modifier.width(width.dp) else Modifier.fillMaxWidth())
            .padding(horizontal = 25.dp, vertical = 16.dp)
            .shadow(8.dp, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = buttonText,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}


@Preview(showBackground = true)
@Composable
fun CustomButtonPreview() {
    CustomButton(buttonText = "Custom Button", onClick = {}, isIncome = true, isExpense = false, isThirdButton = false)
}