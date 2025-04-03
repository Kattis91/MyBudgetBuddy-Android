package com.example.mybudgetbuddy.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetbuddy.R
import com.example.mybudgetbuddy.budget.BudgetManager
import com.example.mybudgetbuddy.models.BudgetPeriod
import com.example.mybudgetbuddy.utils.formatAmount
import com.example.mybudgetbuddy.utils.formattedDateRange

@Composable
fun SummaryBox(
    period: BudgetPeriod,
    viewModel: BudgetManager = viewModel(),
    isCurrent: Boolean
) {
    val totalIncome = if (isCurrent) {
        viewModel.totalIncome.collectAsState().value
    } else {
        viewModel.getPeriodById(period.id)?.totalIncome ?: 0.0
    }

    val totalExpenses = if (isCurrent) {
        viewModel.totalExpenses.collectAsState().value
    } else {
        viewModel.getPeriodById(period.id)?.let { it.totalFixedExpenses + it.totalVariableExpenses } ?: 0.0
    }

    val outcome = totalIncome - totalExpenses
    val isNegative = outcome < 0

    val isDarkMode = isSystemInDarkTheme()
    val textColor = if (isDarkMode) Color.White else colorResource(id = R.color.text_color)

    StyledCard {
        Text(
            if (isCurrent) "Current Period" else "",
            fontSize = 21.sp,
            color = textColor,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            formattedDateRange(period.startDate, period.endDate),
            color = textColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    formatAmount(totalIncome),
                    color = colorResource(id = R.color.income_color),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text("Income", color = textColor)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    formatAmount(totalExpenses),
                    color = colorResource(id = R.color.expense_color),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text("Expenses", color = textColor)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    if (isNegative) "- ${formatAmount(outcome)}" else formatAmount(outcome),
                    color = Color.Blue,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text("Outcome", color = textColor)
            }
        }
    }
}