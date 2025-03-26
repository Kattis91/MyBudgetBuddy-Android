package com.example.mybudgetbuddy.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetbuddy.budget.BudgetManager
import com.example.mybudgetbuddy.components.CustomButton
import com.example.mybudgetbuddy.components.DatePickerButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun NewBudgetPeriodView(
    isPresented: Boolean,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    budgetManager: BudgetManager = viewModel(),
    isLandingPage: Boolean = false,
    noCurrentPeriod: Boolean = false
) {
    if (!isPresented) return

    var startDate by remember { mutableStateOf(Date()) }
    var endDate by remember {
        mutableStateOf(Calendar.getInstance().apply {
            time = Date()
            add(Calendar.MONTH, 1)
        }.time)
    }

    // For validation
    var showValidationError by remember { mutableStateOf(false) }
    var validationMessage by remember { mutableStateOf("") }

    var includeIncomes by remember { mutableStateOf(true) }
    var includeFixedExpenses by remember { mutableStateOf(true) }

    // Date formatter
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

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

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
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

                // Title
                Text(
                    text = "New Budget Period",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

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

                    // Use a clickable text that shows a DatePicker dialog
                    DatePickerButton(
                        label = "Start Date",
                        date = startDate,
                        onDateSelected = { newDate ->
                            startDate = newDate
                        }
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

                    DatePickerButton(
                        label = "End Date",
                        date = endDate,
                        onDateSelected = { newDate ->
                            endDate = newDate
                        }
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

                Text(
                    text = "Transfer Settings",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Include incomes toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Include Incomes",
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = includeIncomes,
                        onCheckedChange = { includeIncomes = it }
                    )
                }

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
                        onCheckedChange = { includeFixedExpenses = it }
                    )
                }

                CustomButton(
                    buttonText = "Start New Period",
                    onClick = {
                        if (validatePeriod()) {
                            if (isLandingPage && noCurrentPeriod) {
                                // Create a new clean budget period
                                budgetManager.startNewPeriod(startDate, endDate)
                                // After success
                                onSuccess()
                                onDismiss()
                            } else {
                                // Normal flow
                                budgetManager.startNewPeriod(startDate, endDate)
                                // After success
                                onSuccess()
                                onDismiss()
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
}

@Preview(showBackground = true)
@Composable
fun NewBudgetPeriodViewPreview() {
    NewBudgetPeriodView(isPresented = true, onDismiss = {}, onSuccess = {})
}

