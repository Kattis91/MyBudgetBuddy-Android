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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetbuddy.BudgetManager
import com.example.mybudgetbuddy.components.CategoryMenu
import com.example.mybudgetbuddy.components.CustomButton
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ExpensesTabView(
    startDate: String,
    endDate: String,
    totalExpense: Double,
    viewModel: BudgetManager = viewModel()
) {
    var expenseAmount by remember { mutableStateOf("") }

    val categories = listOf("Rent", "Water", "Heat", "Electricity", "Insurance", "WiFi")
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
                value = expenseAmount,
                onValueChange = { expenseAmount = it },
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

        CustomButton(
            buttonText = "Add Expense",
            onClick = {

            },
            isIncome = false
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