package com.example.mybudgetbuddy.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mybudgetbuddy.budget.BudgetManager
import com.example.mybudgetbuddy.models.BudgetPeriod
import com.example.mybudgetbuddy.utils.formatAmount
import com.example.mybudgetbuddy.utils.formattedDateRange
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodRowView(
    period: BudgetPeriod,
    navController: NavController,
    textColor: Color,
    viewModel: BudgetManager = viewModel()
) {
    // Track if this item is being deleted
    var isBeingDeleted by remember { mutableStateOf(false) }

    // Don't show items being deleted
    if (isBeingDeleted) {
        return
    }
    val swipeState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                // Mark as being deleted
                isBeingDeleted = true
                // Delete from Firebase
                viewModel.deleteHistoricalPeriod(period.id)
                true
            } else {
                false
            }
        },
        positionalThreshold = { it * 0.7f }
    )

    SwipeToDismissBox(
        state = swipeState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {},
        content = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        navController.navigate("periodDetail/${period.id}")
                    },
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            formattedDateRange(period.startDate, period.endDate),
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "Outcome: ${formatAmount(period.totalIncome - (period.totalFixedExpenses + period.totalVariableExpenses))}",
                            color = textColor
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Column(modifier = Modifier.padding(end = 5.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Right arrow"
                        )
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PeriodRowViewPreview() {
    val previewNavController = rememberNavController()
    PeriodRowView(
        navController = previewNavController,
        period = BudgetPeriod(startDate = Date(), endDate = Date()),
        textColor = Color.Blue)
}


