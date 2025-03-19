package com.example.mybudgetbuddy.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.mybudgetbuddy.components.CustomTextField
import com.example.mybudgetbuddy.components.IncomeItem
import com.example.mybudgetbuddy.components.StyledCard
import com.example.mybudgetbuddy.utils.formatAmount
import com.example.mybudgetbuddy.utils.formattedDateRange

@Composable
fun IncomesTabView(
    startDate: String,
    endDate: String,
    viewModel: BudgetManager = viewModel()
) {
    var incomeAmount by remember { mutableStateOf("") }

    val categories = listOf("Salary", "Study grant", "Child benefit", "Housing insurance", "Sickness insurance", "Business")
    var selectedCategory by remember { mutableStateOf("") }
    var showNewCategoryField by remember { mutableStateOf(false) }

    val currentPeriod by viewModel.currentPeriod.observeAsState()

    val incomeItems by viewModel.incomeItems.collectAsState()
    val isLoading by viewModel.isLoading.observeAsState(initial = false)

    val totalIncome by viewModel.totalIncome.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .width(230.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            currentPeriod?.let { period ->
                StyledCard {
                    Text(
                        "Current Budget Period",
                        style = MaterialTheme.typography.titleMedium,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(formattedDateRange(period.startDate, period.endDate))

                    Text(
                        "Total Income:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 14.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(formatAmount(totalIncome))
                }
            }
        }


        Spacer(modifier = Modifier.height(38.dp))

        Column(
            modifier = Modifier.padding(horizontal = 25.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CustomTextField(
                value = incomeAmount,
                onValueChange = { incomeAmount = it },
                label = "Amount",
            )

            CategoryMenu(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { category -> selectedCategory = category },
                showNewCategoryField = showNewCategoryField,
                onShowNewCategoryFieldChange = { showNewCategoryField = it }
            )
        }

        Button(
            onClick = {
                if (incomeAmount.isNotEmpty() && selectedCategory.isNotEmpty()) {
                    val amount = incomeAmount.toDoubleOrNull() ?: 0.0
                    viewModel.addIncome(amount, selectedCategory)
                }
            },
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(horizontal = 50.dp)
                .fillMaxWidth()
        ) {
            Text("Add Income")
        }

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                items(incomeItems) { income ->
                    IncomeItem(income)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BudgetOverviewPreview() {
    IncomesTabView(
        startDate = "2025-03-01",
        endDate = "2025-03-31",
    )
}
