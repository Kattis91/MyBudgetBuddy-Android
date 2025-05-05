package com.kat.mybudgetbuddy.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
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
    LaunchedEffect(Unit) {
        viewModel.loadHistoricalPeriods()
    }

    val historicalPeriods by viewModel.historicalPeriods.collectAsState()
    val isLoading by viewModel.isLoading.observeAsState(initial = false)

    val nonEmptyPeriods = historicalPeriods.filter {
        it.incomes.isNotEmpty() || it.fixedExpenses.isNotEmpty() || it.variableExpenses.isNotEmpty()
    }.reversed()
    val currentPeriod by viewModel.currentPeriod.observeAsState()

    val isDarkMode = isSystemInDarkTheme()
    val textColor = if (isDarkMode) Color.White else colorResource(id = R.color.text_color)

    Column(
        modifier = Modifier.padding(horizontal = 18.dp)
    ) {
        Spacer(modifier = Modifier.height(26.dp))

        currentPeriod?.let { period ->
            SummaryBox(period = period, isCurrent = true)
        }

        Spacer(modifier = Modifier.height(26.dp))

        if (nonEmptyPeriods.isNotEmpty()) {
            Text(
                text = stringResource(R.string.historical_periods),
                fontSize = 23.sp,
                color = textColor
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                Icon(
                    Icons.AutoMirrored.Filled.ArrowLeft,
                    contentDescription = "Back"
                )
                Text(
                    text = stringResource(R.string.swipe_left_to_delete_periods),
                    fontSize = 14.sp,
                    color = textColor
                )
            }
        } else {
            Text(
                text = stringResource(R.string.you_have_no_historical_periods),
                style = TextStyle(textAlign = TextAlign.Center),
                fontSize = 23.sp,
                color = textColor
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn {
                items(nonEmptyPeriods) { period ->
                    // Use key parameter to help compose track items better
                    key(period.id) {
                        PeriodRowView(period = period, navController = navController, textColor = textColor)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OverviewTabViewPreview() {
    OverviewTabView(period = BudgetPeriod(startDate = Date(), endDate = Date()))
}