package com.example.mybudgetbuddy.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetbuddy.BudgetManager
import com.example.mybudgetbuddy.components.CategoryMenu
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun IncomesTabView(
    startDate: String,
    endDate: String,
    totalIncome: Double,
    viewModel: BudgetManager = viewModel()
) {
    var incomeAmount by remember { mutableStateOf("") }

    val categories = listOf("Salary", "Study grant", "Child benefit", "Housing insurance", "Sickness insurance", "Business")
    var selectedCategory by remember { mutableStateOf("") }
    var showNewCategoryField by remember { mutableStateOf(false) }

    val currentPeriod by viewModel.currentPeriod.observeAsState()

    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(modifier = Modifier
            .width(230.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            currentPeriod?.let { period ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Current Budget Period",
                            style = MaterialTheme.typography.titleMedium,
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
            }
        }

        Spacer(modifier = Modifier.height(38.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TextField(
                value = incomeAmount,
                onValueChange = { incomeAmount = it },
                label = { Text("Amount") }
            )

            CategoryMenu(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { category -> selectedCategory = category },
                showNewCategoryField = showNewCategoryField,
                onShowNewCategoryFieldChange = { showNewCategoryField = it }
            )
        }

        Button(onClick = {

        },
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(horizontal = 50.dp)
                .fillMaxWidth()
        ) {
            Text("Add Income")

        }
    }
}

// Function to format the date range as needed
fun formattedDateRange(startDate: String, endDate: String): String {
    return "$startDate - $endDate"
}

@Preview(showBackground = true)
@Composable
fun BudgetOverviewPreview() {
    IncomesTabView(
        startDate = "2025-03-01",
        endDate = "2025-03-31",
        totalIncome = 1234.56
    )
}
