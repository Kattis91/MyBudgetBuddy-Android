package com.example.mybudgetbuddy.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.mybudgetbuddy.BudgetManager
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetbuddy.NewBudgetPeriodView
import com.example.mybudgetbuddy.R
import com.example.mybudgetbuddy.components.StatBox
import com.example.mybudgetbuddy.components.StyledCard
import com.example.mybudgetbuddy.utils.formattedDateRange

@Composable
fun HomeTabView(viewModel: BudgetManager = viewModel()) {
    val currentPeriod by viewModel.currentPeriod.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)

    var showNewPeriodDialog by remember { mutableStateOf(false) }

    val totalIncome by viewModel.totalIncome.collectAsState()

    val isDarkMode = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            // Display current period info if available
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
                            modifier = Modifier
                                .padding(bottom = 10.dp)
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        StatBox(
            title = "Total Income",
            amount = totalIncome,
            isIncome = true,
            showNegativeAmount = false
        )

        Button(
            onClick = { showNewPeriodDialog = true },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Start New Period")
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