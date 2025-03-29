package com.example.mybudgetbuddy.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetbuddy.R
import com.example.mybudgetbuddy.budget.BudgetManager
import com.example.mybudgetbuddy.components.CategoryMenu
import com.example.mybudgetbuddy.components.CustomButton
import com.example.mybudgetbuddy.components.CustomListView
import com.example.mybudgetbuddy.components.CustomTextField
import com.example.mybudgetbuddy.components.SegmentedButtonRow
import com.example.mybudgetbuddy.components.StyledCard
import com.example.mybudgetbuddy.models.CategoryType
import com.example.mybudgetbuddy.models.ExpenseViewType
import com.example.mybudgetbuddy.models.defaultCategories
import com.example.mybudgetbuddy.utils.formatAmount
import com.example.mybudgetbuddy.utils.formattedDateRange

@Composable
fun ExpensesTabView(
    viewModel: BudgetManager = viewModel()
) {
    var expenseAmount by remember { mutableStateOf("") }

    val (selectedExpenseType, setSelectedExpenseType) = remember { mutableStateOf(ExpenseViewType.FIXED) }

    val fixedExpenseCategories by viewModel.fixedExpenseCategories.collectAsState()
    val variableExpenseCategories by viewModel.variableExpenseCategories.collectAsState()

    val categories = if (selectedExpenseType == ExpenseViewType.FIXED)
        fixedExpenseCategories.ifEmpty { CategoryType.FIXED_EXPENSE.defaultCategories }
    else
        variableExpenseCategories.ifEmpty { CategoryType.VARIABLE_EXPENSE.defaultCategories }

    var selectedCategory by remember { mutableStateOf("") }
    var showNewCategoryField by remember { mutableStateOf(false) }

    val currentPeriod by viewModel.currentPeriod.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(initial = false)

    val fixedExpenseItems by viewModel.fixedExpenseItems.collectAsState()
    val variableExpenseItems by viewModel.variableExpenseItems.collectAsState()

    val totalExpenses by viewModel.totalExpenses.collectAsState()

    val isDarkMode = isSystemInDarkTheme()
    val textColor = if (isDarkMode) Color.White else colorResource(id = R.color.text_color)

    LaunchedEffect(selectedExpenseType) {
        println("Selected Expense Type: $selectedExpenseType")
        if (selectedExpenseType == ExpenseViewType.FIXED) {
            viewModel.loadFixedExpenseCategories()
        } else if (selectedExpenseType == ExpenseViewType.VARIABLE) {
            viewModel.loadVariableExpenseCategories()
        }
    }

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
                        color = textColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(formattedDateRange(period.startDate, period.endDate), color = textColor)

                    Text(
                        "Total Expenses:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 14.dp),
                        color = textColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(formatAmount(totalExpenses), color = textColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        SegmentedButtonRow(
            options = listOf("Fixed Expenses", "Variable Expenses"),
            selectedIndex = if (selectedExpenseType == ExpenseViewType.FIXED) 0 else 1,
            onSelectionChanged = { index ->
                setSelectedExpenseType(if (index == 0) ExpenseViewType.FIXED else ExpenseViewType.VARIABLE)
                selectedCategory = ""
            }
        )

        Spacer(modifier = Modifier.height(28.dp))

        Column(
            modifier = Modifier.padding(horizontal = 25.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CustomTextField(
                value = expenseAmount,
                onValueChange = { expenseAmount = it },
                label = "Amount",
                icon = Icons.Default.RemoveCircleOutline,
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
                if (expenseAmount.isNotEmpty() && selectedCategory.isNotEmpty()) {
                    val amount = expenseAmount.toDoubleOrNull() ?: 0.0
                    // Pass the isFixed parameter based on the selected type
                    val isFixed = selectedExpenseType == ExpenseViewType.FIXED
                    viewModel.addExpense(amount, selectedCategory, isFixed)
                }
                expenseAmount = ""
                selectedCategory = ""
            },
            isIncome = false,
            isExpense = true,
            isThirdButton = false,
            width = 0
        )


        if (isLoading) {
            CircularProgressIndicator()
        } else if (selectedExpenseType == ExpenseViewType.FIXED) {
            CustomListView(
                items = fixedExpenseItems,
                deleteAction = { expense->
                    viewModel.deleteExpenseItem(
                        expense,
                        isfixed = true
                    )
                },
                itemContent = { expense ->
                    Triple(expense.category, expense.amount, null)
                },
                showNegativeAmount = true
            )
        } else {
            CustomListView(
                items = variableExpenseItems,
                deleteAction = { expense->
                    viewModel.deleteExpenseItem(
                        expense,
                        isfixed = false
                    )
                },
                itemContent = { expense ->
                    Triple(expense.category, expense.amount, null)
                },
                showNegativeAmount = true
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExpensesTabViewPreview() {
    ExpensesTabView()
}