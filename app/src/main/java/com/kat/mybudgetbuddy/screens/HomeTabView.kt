package com.kat.mybudgetbuddy.screens

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
import com.kat.mybudgetbuddy.budget.BudgetManager
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kat.mybudgetbuddy.R
import com.kat.mybudgetbuddy.components.CustomButton
import com.kat.mybudgetbuddy.components.OutcomeBox
import com.kat.mybudgetbuddy.components.PeriodBox
import com.kat.mybudgetbuddy.components.StatBox
import com.kat.mybudgetbuddy.components.StyledCard
import com.kat.mybudgetbuddy.components.SummaryBox
import com.kat.mybudgetbuddy.utils.formattedDateRange

@Composable
fun HomeTabView(viewModel: BudgetManager = viewModel()) {
    val currentPeriod by viewModel.currentPeriod.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)

    var showNewPeriodDialog by remember { mutableStateOf(false) }

    val totalIncome by viewModel.totalIncome.collectAsState()

    val totalExpenses by viewModel.totalExpenses.collectAsState()

    val isDarkMode = isSystemInDarkTheme()

    LaunchedEffect(Unit) {
        viewModel.loadCurrentBudgetPeriod()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!showNewPeriodDialog) {
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
                        PeriodBox(period = period)
                    } ?: run {
                        Text(
                            "No active budget period",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                    }

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
                        width = 250,
                        height = 78
                    )

                    Spacer(modifier = Modifier.weight(1f)) // Additional space at bottom
                }
            }
        } else {
            // Show New Budget Period Dialog when button is clicked
            NewBudgetPeriodView(
                isPresented = true,
                onDismiss = { showNewPeriodDialog = false },
                onSuccess = {
                },
                noCurrentPeriod = currentPeriod == null,
                isLandingPage = false
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeTabViewPreview() {
    HomeTabView()
}