package com.example.mybudgetbuddy.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mybudgetbuddy.components.SummaryBox
import com.example.mybudgetbuddy.models.BudgetPeriod
import java.util.Date

@Composable
fun OverviewTabView(
    period: BudgetPeriod
) {
    Column(
        modifier = Modifier.padding(horizontal = 26.dp)
    ) {
        SummaryBox(period = period)
    }
}

@Preview(showBackground = true)
@Composable
fun OverviewTabViewPreview() {
    OverviewTabView(period = BudgetPeriod(startDate = Date(), endDate = Date()))
}