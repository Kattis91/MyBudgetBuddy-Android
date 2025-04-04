package com.kat.mybudgetbuddy.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kat.mybudgetbuddy.R
import com.kat.mybudgetbuddy.budget.BudgetManager
import com.kat.mybudgetbuddy.components.CategoryMenu
import com.kat.mybudgetbuddy.components.CustomButton
import com.kat.mybudgetbuddy.components.CustomListView
import com.kat.mybudgetbuddy.components.CustomTextField
import com.kat.mybudgetbuddy.components.SegmentedButtonRow
import com.kat.mybudgetbuddy.components.StyledCard
import com.kat.mybudgetbuddy.models.CategoryType
import com.kat.mybudgetbuddy.models.ExpenseViewType
import com.kat.mybudgetbuddy.models.defaultCategories
import com.kat.mybudgetbuddy.utils.formatAmount
import com.kat.mybudgetbuddy.utils.formattedDateRange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    var newCategory by remember { mutableStateOf("") }

    val currentPeriod by viewModel.currentPeriod.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(initial = false)

    val fixedExpenseItems by viewModel.fixedExpenseItems.collectAsState()
    val variableExpenseItems by viewModel.variableExpenseItems.collectAsState()

    val totalExpenses by viewModel.totalExpenses.collectAsState()

    var errorMessage by remember { mutableStateOf("") }

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

    LaunchedEffect(Unit) {
        viewModel.loadCurrentBudgetPeriod()
    }

    Column(
        modifier = Modifier
            .fillMaxSize().padding(top = 10.dp),
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
                        "Current Period:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(formattedDateRange(period.startDate, period.endDate), color = textColor, fontSize = 18.sp)

                    Text(
                        "Total Expenses:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp),
                        color = textColor,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        formatAmount(totalExpenses),
                        color = colorResource(id = R.color.expense_color),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
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
                label = "Enter Expense",
                icon = Icons.Default.RemoveCircleOutline,
                onChange = {
                    errorMessage = ""
                }
            )

            CategoryMenu(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { category -> selectedCategory = category },
                showNewCategoryField = showNewCategoryField,
                onShowNewCategoryFieldChange = { showNewCategoryField = it },
                newCategory = newCategory,
                onNewCategoryChange = {
                    newCategory = it
                }
            )
        }

        Box(modifier = Modifier
            .heightIn(min = 25.dp)
            .align(Alignment.Start)
            .padding(start = 25.dp)) {
            if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage,
                    color = colorResource(id = R.color.error_message_color))
            }
        }

        CustomButton(
            buttonText = "Add Expense",
            onClick = {
                val normalizedAmount = expenseAmount.replace(",", ".")
                val expense = normalizedAmount.toDoubleOrNull()

                if (expense == null) {
                    errorMessage = "Amount must be a number."
                } else if (expense <= 0.00) {
                    errorMessage = "Amount must be greater than zero."
                } else {
                    if (showNewCategoryField) {
                        if (newCategory.isNotEmpty()) {
                            CoroutineScope(Dispatchers.Main).launch {
                                errorMessage = ""
                                val success =
                                    if(selectedExpenseType == ExpenseViewType.FIXED)
                                        viewModel.addCategory(newCategory, CategoryType.FIXED_EXPENSE)
                                    else
                                        viewModel.addCategory(newCategory, CategoryType.VARIABLE_EXPENSE)
                                if (success) {
                                    expenseAmount = ""
                                    selectedCategory = newCategory
                                    val isFixed = selectedExpenseType == ExpenseViewType.FIXED
                                    viewModel.addExpense(expense, selectedCategory, isFixed)
                                    showNewCategoryField = false
                                    newCategory = ""
                                } else if (newCategory in categories) {
                                    errorMessage = "Category already exists"
                                } else {
                                    errorMessage = "Failed to add category"
                                }
                            }
                        } else {
                            errorMessage = "Please add a category"
                        }
                    } else {
                        if (selectedCategory.isNotEmpty()) {
                            val isFixed = selectedExpenseType == ExpenseViewType.FIXED
                            viewModel.addExpense(expense, selectedCategory, isFixed)
                            expenseAmount = ""
                            selectedCategory = ""
                        } else {
                            errorMessage = "Please select a category"
                        }
                    }
                }
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
                showNegativeAmount = true,
                alignAmountInMiddle = false,
                isInvoice = false
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
                showNegativeAmount = true,
                alignAmountInMiddle = false,
                isInvoice = false
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExpensesTabViewPreview() {
    ExpensesTabView()
}