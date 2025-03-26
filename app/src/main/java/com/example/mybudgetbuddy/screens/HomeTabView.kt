package com.example.mybudgetbuddy.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.mybudgetbuddy.budget.BudgetManager
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetbuddy.R
import com.example.mybudgetbuddy.components.CustomButton
import com.example.mybudgetbuddy.components.OutcomeBox
import com.example.mybudgetbuddy.components.StatBox
import com.example.mybudgetbuddy.components.StyledCard
import com.example.mybudgetbuddy.utils.formattedDateRange

@Composable
fun HomeTabView(viewModel: BudgetManager = viewModel()) {
    val currentPeriod by viewModel.currentPeriod.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)

    var showNewPeriodDialog by remember { mutableStateOf(false) }

    val totalIncome by viewModel.totalIncome.collectAsState()

    val totalExpenses by viewModel.totalExpenses.collectAsState()

    val isDarkMode = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))

            currentPeriod?.let { period ->
                StyledCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Current Budget Period",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (isDarkMode) Color.White else colorResource(id = R.color.text_color),
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            formattedDateRange(period.startDate, period.endDate),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = if (isDarkMode) Color.White else colorResource(id = R.color.text_color)
                        )
                    }
                }
            } ?: run {
                Text(
                    "No active budget period",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                Box(modifier = Modifier.weight(1f)) {
                    StatBox(
                        title = "Total Income",
                        amount = totalIncome,
                        isIncome = true,
                        showNegativeAmount = false
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(modifier = Modifier.weight(1f)) {
                    StatBox(
                        title = "Total Expense",
                        amount = totalExpenses,
                        isIncome = false,
                        showNegativeAmount = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutcomeBox(
                income = totalIncome,
                expense = totalExpenses
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomButton(
                onClick = { showNewPeriodDialog = true },
                buttonText = "Start New Period",
                isIncome = false,
                isExpense = false,
                isThirdButton = true,
                width = 215
            )

            Spacer(modifier = Modifier.weight(1f)) // Additional space at bottom
        }

        // Show New Budget Period Dialog when button is clicked
        if (showNewPeriodDialog) {
            NewBudgetPeriodView(
                isPresented = true,
                onDismiss = { showNewPeriodDialog = false },
                onSuccess = {
                },
                noCurrentPeriod = currentPeriod == null
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeTabViewPreview() {
    HomeTabView()
}