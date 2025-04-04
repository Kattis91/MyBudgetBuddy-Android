package com.kat.mybudgetbuddy.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kat.mybudgetbuddy.R
import com.kat.mybudgetbuddy.budget.BudgetManager
import com.kat.mybudgetbuddy.components.PeriodRowView
import com.kat.mybudgetbuddy.components.SummaryBox
import com.kat.mybudgetbuddy.models.BudgetPeriod
import java.util.Date

@Composable
fun OverviewTabView(
    period: BudgetPeriod,
    viewModel: BudgetManager = viewModel(),
    navController: NavController = rememberNavController()
) {

    val historicalPeriods by viewModel.historicalPeriods.collectAsState()

    val nonEmptyPeriods = historicalPeriods.filter {
        it.incomes.isNotEmpty() || it.fixedExpenses.isNotEmpty() || it.variableExpenses.isNotEmpty()
    }

    val isDarkMode = isSystemInDarkTheme()
    val textColor = if (isDarkMode) Color.White else colorResource(id = R.color.text_color)

    Column(
        modifier = Modifier.padding(horizontal = 26.dp)
    ) {
        SummaryBox(period = period, isCurrent = true)

        Spacer(modifier = Modifier.height(26.dp))

        if (nonEmptyPeriods.isNotEmpty()) {
            Text(
                "Historical Periods:",
                fontSize = 25.sp,
                color = textColor
            )
        } else {
            Text(
                "You have no historical periods right now.",
                style = TextStyle(textAlign = TextAlign.Center),
                fontSize = 23.sp,
                color = textColor
            )
        }

        LazyColumn {
            items(nonEmptyPeriods) { period ->
                PeriodRowView(period = period, navController = navController, textColor = textColor)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OverviewTabViewPreview() {
    OverviewTabView(period = BudgetPeriod(startDate = Date(), endDate = Date()))
}