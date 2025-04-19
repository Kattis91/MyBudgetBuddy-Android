package com.kat.mybudgetbuddy.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kat.mybudgetbuddy.R
import com.kat.mybudgetbuddy.budget.BudgetManager
import com.kat.mybudgetbuddy.components.BudgetItemRow
import com.kat.mybudgetbuddy.components.CustomAlertDialog
import com.kat.mybudgetbuddy.components.CustomButton
import com.kat.mybudgetbuddy.components.DatePickerButton
import java.util.Calendar
import java.util.Date

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun NewBudgetPeriodView(
    isPresented: Boolean,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    budgetManager: BudgetManager = viewModel(),
    isLandingPage: Boolean,
    noCurrentPeriod: Boolean = false,
    viewModel: BudgetManager = viewModel()
) {
    if (!isPresented) return

    var startDate by remember { mutableStateOf(Date()) }
    var endDate by remember {
        mutableStateOf(Calendar.getInstance().apply {
            time = Date()
            add(Calendar.MONTH, 1)
        }.time)
    }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    // For validation
    var showValidationError by remember { mutableStateOf(false) }
    var validationMessage by remember { mutableStateOf("") }

    var includeIncomes by remember { mutableStateOf(true) }
    var includeFixedExpenses by remember { mutableStateOf(true) }

    // Validation function
    fun validatePeriod(): Boolean {
        val calendar = Calendar.getInstance()
        val now = Date()

        // Check if start date is in the past
        calendar.time = now
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val today = calendar.time

        if (startDate.before(today)) {
            validationMessage = "Start date cannot be in the past"
            showValidationError = true
            return false
        }

        // Check if end date is before start date
        if (endDate.before(startDate)) {
            validationMessage = "End date must be after start date"
            showValidationError = true
            return false
        }

        return true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false)
        ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp).padding(horizontal = 16.dp)) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                // Date pickers
                Text(
                    text = "Period Dates",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Start date picker
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Start Date", modifier = Modifier.width(100.dp))

                    Spacer(modifier = Modifier.weight(1f))
                    // Use a clickable text that shows a DatePicker dialog
                    DatePickerButton(
                        label = "Start Date",
                        date = startDate,
                        onDateSelected = { newDate ->
                            startDate = newDate
                        },
                        modifier = Modifier
                            .background(
                                color = Color.Gray.copy(alpha = 0.3f), // Light gray background
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // End date picker
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("End Date", modifier = Modifier.width(100.dp))

                    Spacer(modifier = Modifier.weight(1f))
                    DatePickerButton(
                        label = "End Date",
                        date = endDate,
                        onDateSelected = { newDate ->
                            endDate = newDate
                        },
                        modifier = Modifier
                            .background(
                                color = Color.Gray.copy(alpha = 0.3f), // Light gray background
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // Validation error
                if (showValidationError) {
                    Text(
                        text = validationMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!isLandingPage) {
                    if (viewModel.incomeItems.value.isNotEmpty() || viewModel.fixedExpenseItems.value.isNotEmpty()) {
                        Text(
                            text = "Transfer Settings",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (viewModel.incomeItems.value.isNotEmpty()) {
                        // Include incomes toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Include Incomes",
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = includeIncomes,
                                onCheckedChange = { includeIncomes = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = colorResource(id = R.color.income_color)
                                )
                            )
                        }
                        if (includeIncomes) {
                            Column {
                                LazyColumn(
                                    modifier = Modifier
                                        .height(
                                            min(
                                                (viewModel.incomeItems.value.size * 44).dp,
                                                125.dp
                                            )
                                        )
                                ) {
                                    items(viewModel.incomeItems.value) { income ->
                                        BudgetItemRow(
                                            category = income.category,
                                            amount = income.amount
                                        )
                                    }
                                }
                                if (viewModel.incomeItems.value.size > 3) {
                                    Row(
                                        modifier = Modifier.offset(y = (-6).dp)
                                    ) {
                                        Spacer(modifier = Modifier.weight(1f))
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Expand",
                                            tint = Color.Black.copy(alpha = 0.6f),
                                            modifier = Modifier.size(25.dp)
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    if (viewModel.fixedExpenseItems.value.isNotEmpty()) {
                        // Include fixed expenses toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Include Fixed Expenses",
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = includeFixedExpenses,
                                onCheckedChange = { includeFixedExpenses = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = colorResource(id = R.color.income_color)
                                )
                            )
                        }

                        if (includeFixedExpenses) {
                            Column {
                                LazyColumn(
                                    modifier = Modifier
                                        .height(
                                            min(
                                                (viewModel.fixedExpenseItems.value.size * 44).dp,
                                                125.dp
                                            )
                                        )
                                ) {
                                    items(viewModel.fixedExpenseItems.value) { expense ->
                                        BudgetItemRow(
                                            category = expense.category,
                                            amount = expense.amount
                                        )
                                    }
                                }

                                if (viewModel.fixedExpenseItems.value.size > 3) {
                                    Row(
                                        modifier = Modifier.offset(y = (-6).dp)
                                    ) {
                                        Spacer(modifier = Modifier.weight(1f))
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Expand",
                                            tint = Color.Black.copy(alpha = 0.6f),
                                            modifier = Modifier.size(25.dp)
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    CustomButton(
                        buttonText = "Start New Period",
                        onClick = {
                            if (validatePeriod()) {
                                if (isLandingPage || noCurrentPeriod) {
                                    // Create a new clean budget period
                                    viewModel.createCleanBudgetPeriodAndRefresh(
                                        startDate,
                                        endDate
                                    ) { success ->
                                        if (success) {
                                            // Maybe navigate to the overview tab or show a success message
                                        } else {
                                            // Show error message
                                        }
                                    }
                                    // After success
                                    onSuccess()
                                    onDismiss()
                                } else {
                                    showConfirmationDialog = true
                                }
                            }
                        },
                        isIncome = false,
                        isExpense = false,
                        isThirdButton = true
                    )
                }
            }
        }

        if (showConfirmationDialog) {
            CustomAlertDialog(
                show = true,
                onDismiss = {
                    showConfirmationDialog = false
                },
                onConfirm = {
                    budgetManager.startNewPeriod(
                        startDate,
                        endDate,
                        includeIncomes,
                        includeFixedExpenses
                    )
                    // After success
                    onSuccess()
                    onDismiss()
                },
                message = "Are you sure you want to start a new period? Please note that this period will replace the current one.",
                customColor = colorResource(id = R.color.error_message_color),
                confirmText = "Yes!",
                cancelButtonText = "Go back!",
                onCancel = {
                    showConfirmationDialog = false
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NewBudgetPeriodViewPreview() {
    NewBudgetPeriodView(
        isPresented = true,
        onDismiss = {},
        onSuccess = {},
        isLandingPage = true,
        noCurrentPeriod = true
    )
}

