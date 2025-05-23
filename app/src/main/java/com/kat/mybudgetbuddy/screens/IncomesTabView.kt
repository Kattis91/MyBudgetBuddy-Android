package com.kat.mybudgetbuddy.screens

import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.stringResource
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
import com.kat.mybudgetbuddy.components.StyledCard
import com.kat.mybudgetbuddy.models.CategoryType
import com.kat.mybudgetbuddy.models.defaultCategories
import com.kat.mybudgetbuddy.utils.formatAmount
import com.kat.mybudgetbuddy.utils.formattedDateRange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun IncomesTabView(
    viewModel: BudgetManager = viewModel()
) {
    var incomeAmount by remember { mutableStateOf("") }

    val incomeCategories by viewModel.incomeCategories.collectAsState()
    val categories = incomeCategories.ifEmpty { CategoryType.INCOME.defaultCategories }

    var selectedCategory by remember { mutableStateOf("") }
    var showNewCategoryField by remember { mutableStateOf(false) }
    var newCategory by remember { mutableStateOf("") }

    val currentPeriod by viewModel.currentPeriod.observeAsState()

    val incomeItems by viewModel.incomeItems.collectAsState()
    val isLoading by viewModel.isLoading.observeAsState(initial = false)

    val totalIncome by viewModel.totalIncome.collectAsState()

    // Change from storing string to storing resource ID
    var errorMessageResId by remember { mutableStateOf<Int?>(null) }

    val isDarkMode = isSystemInDarkTheme()
    val textColor = if (isDarkMode) Color.White else colorResource(id = R.color.text_color)

    LaunchedEffect (Unit) {
        viewModel.loadIncomeCategories()
        viewModel.loadCurrentBudgetPeriod()
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
                        text = stringResource(R.string.current_period_colon),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(formattedDateRange(period.startDate, period.endDate), color = textColor, fontSize = 18.sp)

                    Text(
                        text = stringResource(R.string.total_income),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp),
                        color = textColor,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        formatAmount(totalIncome),
                        color = colorResource(id = R.color.income_color),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Column(
            modifier = Modifier.padding(horizontal = 25.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CustomTextField(
                value = incomeAmount,
                onValueChange = { incomeAmount = it },
                label = stringResource(R.string.enter_income),
                icon = Icons.Default.AddCircleOutline,
                onChange = {
                    // Clear error message when typing
                    errorMessageResId = null
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            CategoryMenu(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { category -> selectedCategory = category },
                showNewCategoryField = showNewCategoryField,
                onShowNewCategoryFieldChange = { showNewCategoryField = it },
                newCategory = newCategory,
                onNewCategoryChange = {
                    newCategory = it
                },
                viewModel = viewModel,
                categoryType = CategoryType.INCOME
            )
        }

        Box(modifier = Modifier
            .heightIn(min = 25.dp)
            .align(Alignment.Start)
            .padding(start = 25.dp)) {
            // Display error message using stringResource
            errorMessageResId?.let { resId ->
                Text(
                    text = stringResource(resId),
                    color = colorResource(id = R.color.error_message_color)
                )
            }
        }

        CustomButton(
            buttonText = stringResource(R.string.add_income),
            onClick = {
                val normalizedAmount = incomeAmount.replace(",", ".")
                val income = normalizedAmount.toDoubleOrNull()

                // Check if income is valid and greater than zero
                if (income == null) {
                    errorMessageResId = R.string.amount_must_be_a_number
                } else if (income <= 0.00) {
                    errorMessageResId = R.string.amount_must_be_greater_than_zero
                } else {
                    // Proceed with adding category and income
                    if (showNewCategoryField) {
                        if (newCategory.isNotEmpty()) {
                            // Use coroutine scope to launch the suspend function
                            CoroutineScope(Dispatchers.Main).launch {
                                errorMessageResId = null  // Clear error message before attempting
                                Log.d("IncomesTabView", "Adding category: $newCategory")
                                val success = viewModel.addCategory(newCategory, CategoryType.INCOME)

                                if (success) {
                                    Log.d("IncomesTabView", "Category added successfully")
                                    incomeAmount = ""
                                    selectedCategory = newCategory
                                    viewModel.addIncome(income, newCategory)
                                    showNewCategoryField = false
                                    newCategory = ""
                                    selectedCategory = ""
                                } else if (newCategory in categories) {
                                    errorMessageResId = R.string.category_already_exists
                                } else {
                                    Log.e("IncomesTabView", "Failed to add category: $newCategory")
                                    errorMessageResId = R.string.failed_to_add_category
                                }
                            }
                        } else {
                            errorMessageResId = R.string.please_add_a_category
                        }
                    } else {
                        if (selectedCategory.isNotEmpty()) {
                            viewModel.addIncome(income, selectedCategory)
                            incomeAmount = ""
                            selectedCategory = ""
                        } else {
                            errorMessageResId = R.string.please_select_a_category
                        }
                    }
                }
            },
            isIncome = true,
            isExpense = false,
            isThirdButton = false,
            width = 0
        )

        if (incomeItems.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowLeft,
                    contentDescription = "Back"
                )
                Text(
                    text = stringResource(R.string.swipe_left_to_delete_incomes),
                    fontSize = 14.sp,
                    color = textColor
                )
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            CustomListView(
                items = incomeItems,
                deleteAction = { income ->
                    viewModel.deleteIncomeItem(income)
                },
                itemContent = { income ->
                    Triple(income.category, income.amount, null)
                },
                showNegativeAmount = false,
                alignAmountInMiddle = false,
                isInvoice = false
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BudgetOverviewPreview() {
    IncomesTabView()
}
