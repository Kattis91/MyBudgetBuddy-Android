package com.kat.mybudgetbuddy.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kat.mybudgetbuddy.R
import com.kat.mybudgetbuddy.budget.BudgetManager
import com.kat.mybudgetbuddy.components.CategoryList
import com.kat.mybudgetbuddy.components.HandleCategoryDialog
import com.kat.mybudgetbuddy.components.SegmentedButtonRow
import com.kat.mybudgetbuddy.models.Category
import com.kat.mybudgetbuddy.models.CategoryType
import kotlinx.coroutines.launch

@Composable
fun CategoryManagement(
    viewModel: BudgetManager = viewModel(),
    onDismiss: () -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val incomeCategories by viewModel.incomeCategories.collectAsState()
    val fixedExpenseCategories by viewModel.fixedExpenseCategories.collectAsState()
    val variableExpenseCategories by viewModel.variableExpenseCategories.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }

    var errorMessage by remember { mutableStateOf("") }

    val categories = incomeCategories + fixedExpenseCategories + variableExpenseCategories

    val isDarkMode = isSystemInDarkTheme()

    LaunchedEffect(Unit) {
        viewModel.loadIncomeCategories()
        viewModel.loadFixedExpenseCategories()
        viewModel.loadVariableExpenseCategories()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!showAddDialog && !showEditDialog) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        "Manage Categories",
                        fontSize = 21.sp,
                        color = if (isDarkMode) Color.White else colorResource(id = R.color.text_color),
                        modifier = Modifier.padding(start = 18.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colorResource(id = R.color.expense_color),
                            modifier = Modifier.padding(end = 18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                SegmentedButtonRow(
                    options = listOf("Incomes", "Fixed", "Variable"),
                    selectedIndex = selectedTabIndex,
                    onSelectionChanged = { index ->
                        selectedTabIndex = index
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                val currentCategories = when (selectedTabIndex) {
                    0 -> incomeCategories
                    1 -> fixedExpenseCategories
                    2 -> variableExpenseCategories
                    else -> emptyList()
                }

                // Get the current category type
                val currentCategoryType = CategoryType.entries[selectedTabIndex]

                CategoryList(
                    categories = currentCategories,
                    onAddCategoryClick = {
                        showAddDialog = true
                        errorMessage = ""
                    },
                    onEditCategoryClick = { categoryName ->
                        // Create a Category object for editing
                        categoryToEdit = Category(
                            name = categoryName,
                            type = currentCategoryType
                        )
                        errorMessage = ""
                        showEditDialog = true
                    },
                    onDeleteCategoryClick = { categoryName ->
                        viewModel.deleteCategory(categoryName, currentCategoryType)
                    }
                )
            }
        } else if (showAddDialog) {
            val coroutineScope = rememberCoroutineScope()
            val currentCategoryType = CategoryType.entries[selectedTabIndex]
            HandleCategoryDialog(
                onDismiss = {
                    showAddDialog = false
                    errorMessage = ""
                },
                onSaveCategory = { categoryName ->
                    coroutineScope.launch {
                        if (categoryName.isBlank()) {
                            errorMessage = "Category name cannot be empty"
                        } else if (categoryName in categories) {
                            errorMessage = "Category already exists"
                        } else {
                            viewModel.addCategory(categoryName, currentCategoryType)
                            showAddDialog = false
                        }
                    }
                },
                isEditing = false,
                category = Category("", "", currentCategoryType),
                errorMessage = errorMessage,
                onErrorMessageChange = { errorMessage = it }
            )
        } else if (categoryToEdit != null) {
            val currentCategoryType = CategoryType.entries[selectedTabIndex]
            HandleCategoryDialog(
                onDismiss = {
                    showEditDialog = false
                    errorMessage = ""
                },
                onSaveCategory = { categoryName ->
                    categoryToEdit?.let { category ->
                        if (categoryName.isBlank()) {
                            errorMessage = "Category name cannot be empty"
                        } else if (categoryName in categories) {
                            errorMessage = "Category already exists"
                        } else {
                            viewModel.editCategory(
                                oldName = category.name,
                                newName = categoryName,
                                currentCategoryType
                            )
                            errorMessage = ""
                            showEditDialog = false
                        }
                    }
                },
                isEditing = true,
                category = categoryToEdit,
                errorMessage = errorMessage,
                onErrorMessageChange = { errorMessage = it }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryManagementPreview() {
    CategoryManagement(onDismiss = {})
}