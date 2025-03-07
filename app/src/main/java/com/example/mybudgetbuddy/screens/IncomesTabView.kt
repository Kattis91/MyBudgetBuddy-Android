package com.example.mybudgetbuddy.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mybudgetbuddy.CategoryMenu

@Composable
fun IncomesTabView(
    startDate: String,
    endDate: String,
    totalIncome: Double
) {
    var incomeAmount by remember { mutableStateOf("") }

    val categories = listOf("Salary", "Study grant", "Child benefit", "Housing insurance", "Sickness insurance", "Business")
    var selectedCategory by remember { mutableStateOf("") }
    var showNewCategoryField by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Current Period:",
                modifier = Modifier.padding(bottom = 3.dp),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = formattedDateRange(startDate, endDate),
                modifier = Modifier.padding(bottom = 5.dp)
            )
            Text(
                text = "Total Income:",
                modifier = Modifier.padding(bottom = 3.dp),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$${"%.2f".format(totalIncome)}"
            )
        }

        Spacer(modifier = Modifier.height(38.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TextField(
                value = incomeAmount,
                onValueChange = { incomeAmount = it },
                label = { Text("Amount") }
            )
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
