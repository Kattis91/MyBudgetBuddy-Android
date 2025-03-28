package com.example.mybudgetbuddy.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mybudgetbuddy.components.CustomListView
import com.example.mybudgetbuddy.components.SegmentedButtonRow
import com.example.mybudgetbuddy.components.SummaryBox
import com.example.mybudgetbuddy.models.BudgetPeriod
import java.util.Date

@Composable
fun PeriodDetailView(
    period: BudgetPeriod,
) {
    var selectedTabIndex by remember { mutableStateOf(0) }

    Column(modifier = Modifier
        .fillMaxHeight()
        .padding(horizontal = 26.dp)) {
        SummaryBox(period = period, isCurrent = false)

        Spacer(modifier = Modifier.height(20.dp))

        SegmentedButtonRow(
            options = listOf("Incomes", "Fixed", "Variable"),
            selectedIndex = selectedTabIndex,
            onSelectionChanged = { index ->
                selectedTabIndex = index
            }
        )

        when (selectedTabIndex) {
            0 -> {
                CustomListView(
                    items = period.incomes,
                    deleteAction = {},
                    itemContent = { income ->
                        Triple(income.category, income.amount, null)
                    },
                    showNegativeAmount = false
                )
            }
            1 -> {
                CustomListView(
                    items = period.fixedExpenses,
                    deleteAction = {},
                    itemContent = { expense ->
                        Triple(expense.category, expense.amount, null)
                    },
                    showNegativeAmount = true
                )
            }
            2 -> {
                CustomListView(
                    items = period.variableExpenses,
                    deleteAction = {},
                    itemContent = { expense ->
                        Triple(expense.category, expense.amount, null)
                    },
                    showNegativeAmount = true
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PeriodDetailViewPreview() {
    PeriodDetailView(period = BudgetPeriod(startDate = Date(), endDate = Date()))
}