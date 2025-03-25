package com.example.mybudgetbuddy.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetbuddy.BudgetManager
import com.example.mybudgetbuddy.components.SummaryBox
import com.example.mybudgetbuddy.models.BudgetPeriod
import com.example.mybudgetbuddy.utils.formatAmount
import com.example.mybudgetbuddy.utils.formattedDateRange
import java.util.Date

@Composable
fun OverviewTabView(
    period: BudgetPeriod,
    viewModel: BudgetManager = viewModel()
) {

    val historicalPeriods by viewModel.historicalPeriods.collectAsState()

    Column(
        modifier = Modifier.padding(horizontal = 26.dp)
    ) {
        SummaryBox(period = period)

        Spacer(modifier = Modifier.height(26.dp))

        Text("Historical Periods:",
            fontSize = 25.sp
        )

        LazyColumn {
            items(historicalPeriods) { period ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            formattedDateRange(period.startDate, period.endDate),
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Outcome: ${formatAmount(period.totalIncome - (period.totalFixedExpenses + period.totalVariableExpenses))}")
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