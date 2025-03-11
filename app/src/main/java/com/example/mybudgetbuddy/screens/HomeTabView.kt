package com.example.mybudgetbuddy.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetbuddy.NewBudgetPeriodView
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeTabView(viewModel: BudgetManager = viewModel()) {
    val currentPeriod by viewModel.currentPeriod.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)

    var showNewPeriodDialog by remember { mutableStateOf(false) }

    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Current Budget Period",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row {
                            Text(
                                "Start: ",
                                fontWeight = FontWeight.Bold
                            )
                            Text(dateFormatter.format(period.startDate))
                        }

                        Row {
                            Text(
                                "End: ",
                                fontWeight = FontWeight.Bold
                            )
                            Text(dateFormatter.format(period.endDate))
                        }
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