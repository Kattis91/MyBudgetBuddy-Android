package com.kat.mybudgetbuddy.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kat.mybudgetbuddy.R
import com.kat.mybudgetbuddy.budget.BudgetManager
import com.kat.mybudgetbuddy.components.AnimatedSegmentedButtonRow
import com.kat.mybudgetbuddy.components.CustomListView
import com.kat.mybudgetbuddy.components.SummaryBox

@Composable
fun PeriodDetailView(
    periodId: String,
    navController: NavController,
    viewModel: BudgetManager = viewModel()
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val historicalPeriods by viewModel.historicalPeriods.collectAsState()
    val isDarkMode = isSystemInDarkTheme()

    // Force load historical periods when this screen is shown
    LaunchedEffect(periodId) {
        viewModel.loadHistoricalPeriods()
    }
    // Get the period after loading
    val period = historicalPeriods.find { it.id == periodId }

    Column(modifier = Modifier
        .fillMaxHeight()
    ) {

        TextButton(
            onClick = {
                navController.popBackStack()
            }
        ) {
            Text(
                "< Back",
                fontSize = 18.sp,
            )
        }

        Box(modifier = Modifier
            .padding(vertical = 10.dp)
            .padding(horizontal = 18.dp)
        ) {
            if (period != null) {
                SummaryBox(period = period, isCurrent = false)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        AnimatedSegmentedButtonRow(
            options = listOf(stringResource(R.string.incomes), stringResource(R.string.fixed), stringResource(R.string.variable)),
            selectedIndex = selectedTabIndex,
            onSelectionChanged = { index ->
                selectedTabIndex = index
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        when (selectedTabIndex) {
            0 -> {
                if (period != null) {
                    if (period.incomes.isNotEmpty()) {
                        CustomListView(
                            items = period.incomes,
                            deleteAction = {},
                            itemContent = { income ->
                                Triple(income.category, income.amount, null)
                            },
                            showNegativeAmount = false,
                            alignAmountInMiddle = false,
                            isInvoice = false
                        )
                    } else {
                        Text(
                            "You didn't add any incomes for this period.",
                            fontSize = 23.sp,
                            color = if (isDarkMode) Color.White else colorResource(R.color.text_color),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            1 -> {
                if (period != null) {
                    if (period.fixedExpenses.isNotEmpty()) {
                        CustomListView(
                            items = period.fixedExpenses,
                            deleteAction = {},
                            itemContent = { expense ->
                                Triple(expense.category, expense.amount, null)
                            },
                            showNegativeAmount = true,
                            alignAmountInMiddle = false,
                            isInvoice = false
                        )
                    } else {
                        Text(
                            "You didn't add any fixed expenses for this period.",
                            fontSize = 23.sp,
                            color = if (isDarkMode) Color.White else colorResource(R.color.text_color),
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            2 -> {
                if (period != null) {
                    if (period.variableExpenses.isNotEmpty()) {
                        CustomListView(
                            items = period.variableExpenses,
                            deleteAction = {},
                            itemContent = { expense ->
                                Triple(expense.category, expense.amount, null)
                            },
                            showNegativeAmount = true,
                            alignAmountInMiddle = false,
                            isInvoice = false
                        )
                    } else {
                        Text(
                            "You didn't add any variable expenses for this period.",
                            fontSize = 23.sp,
                            color = if (isDarkMode) Color.White else colorResource(R.color.text_color),
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PeriodDetailViewPreview() {
    PeriodDetailView(
        navController = rememberNavController(), periodId = "")
}