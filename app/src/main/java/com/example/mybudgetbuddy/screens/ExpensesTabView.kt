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
import com.example.mybudgetbuddy.components.CategoryMenu

@Composable
fun ExpensesTabView(
    startDate: String,
    endDate: String,
    totalExpense: Double
) {
    var expenseAmount by remember { mutableStateOf("") }

    val categories = listOf("Rent", "Water", "Heat", "Electricity", "Insurance", "WiFi")
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
                text = "$${"%.2f".format(totalExpense)}"
            )
        }

        Spacer(modifier = Modifier.height(38.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TextField(
                value = expenseAmount,
                onValueChange = { expenseAmount = it },
                label = { Text("Amount") }
            )
        }

        CategoryMenu(
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = { category -> selectedCategory = category },
            showNewCategoryField = showNewCategoryField,
            onShowNewCategoryFieldChange = { showNewCategoryField = it }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExpensesTabViewPreview() {
    ExpensesTabView(
        startDate = "2023-08-01",
        endDate = "2023-08-31",
        totalExpense = 500.0
    )
}