package com.example.mybudgetbuddy.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetbuddy.budget.BudgetManager
import com.example.mybudgetbuddy.components.SegmentedButtonRow

@Composable
fun CategoryManagement(
    viewModel: BudgetManager = viewModel()
) {

    var selectedTabIndex by remember { mutableStateOf(0) }
    val incomeCategories by viewModel.incomeCategories.collectAsState()
    val fixedExpenseCategories by viewModel.fixedExpenseCategories.collectAsState()
    val variableExpenseCategories by viewModel.variableExpenseCategories.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadIncomeCategories()
        viewModel.loadFixedExpenseCategories()
        viewModel.loadVariableExpenseCategories()
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(top = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    )  {

        Text(
            "Manage Categories",
            fontSize = 20.sp,
        )

        Spacer(modifier = Modifier.height(20.dp))

        SegmentedButtonRow(
            options = listOf("Incomes", "Fixed", "Variable"),
            selectedIndex = selectedTabIndex,
            onSelectionChanged = { index ->
                selectedTabIndex = index
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        when (selectedTabIndex) {
            0 -> {
                LazyColumn {
                    items(incomeCategories) { category ->
                        Text(category)
                    }
                }
            }
            1 -> {
                LazyColumn {
                    items(fixedExpenseCategories) { category ->
                        Text(category)
                    }
                }
            }
            2 -> {
                LazyColumn {
                    items(variableExpenseCategories) { category ->
                        Text(category)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryManagementPreview() {
    CategoryManagement()
}