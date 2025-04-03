package com.example.mybudgetbuddy.screens

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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Doorbell
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetbuddy.R
import com.example.mybudgetbuddy.budget.BudgetManager
import com.example.mybudgetbuddy.components.CustomAlertDialog
import com.example.mybudgetbuddy.components.CustomButton
import com.example.mybudgetbuddy.components.CustomListView
import com.example.mybudgetbuddy.components.CustomTextField
import com.example.mybudgetbuddy.components.DatePickerButton
import com.example.mybudgetbuddy.components.SegmentedButtonRow
import com.example.mybudgetbuddy.models.Invoice
import java.util.Date

@Composable
fun InvoiceReminder(
    viewModel: BudgetManager = viewModel(),
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf(Date()) }
    var errorMessage by remember { mutableStateOf("") }

    val invoices by viewModel.invoices.collectAsState()
    val unprocessedInvoiceState by viewModel.unprocessedInvoices.collectAsState()
    val isLoading by viewModel.isLoading.observeAsState(initial = false)
    val unprocessedInvoices = remember { mutableStateListOf<Invoice>() }
    val processedInvoices = remember { mutableStateListOf<Invoice>() }

    var selectedTabIndex by remember { mutableStateOf(0) }
    val showAlertDialog = remember { mutableStateOf(false) }
    var invoiceToMarkAsProcessed by remember { mutableStateOf<Invoice?>(null) }

    val isDarkMode = isSystemInDarkTheme()

    LaunchedEffect(selectedTabIndex) {
        viewModel.loadInvoicesByStatus(processed = selectedTabIndex == 1)
    }

    // Load invoices when the screen appears
    LaunchedEffect(invoices, selectedTabIndex) {
        if (selectedTabIndex == 0) {
            unprocessedInvoices.clear()
            unprocessedInvoices.addAll(invoices)
        } else {
            processedInvoices.clear()
            processedInvoices.addAll(invoices)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = colorResource(id = R.color.expense_color)
                )
            }
        }
        // Title
        Text(
            "Manage Invoices",
            fontSize = 20.sp,
            color = if (isDarkMode) Color.White else colorResource(id = R.color.text_color),
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .align(Alignment.CenterHorizontally)
        )

        // Form inputs section
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CustomTextField(
                value = title,
                onValueChange = { title = it },
                label = "Title",
                icon = Icons.Default.Doorbell,
                onChange = {
                    errorMessage = ""
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            CustomTextField(
                value = amount,
                onValueChange = { amount = it },
                label = "Amount",
                icon = Icons.Default.AttachMoney,
                onChange = {
                    errorMessage = ""
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Date picker row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AccessTime,
                        contentDescription = "Clock",
                        tint = Color.Gray,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Due to:",
                        color = Color.Gray
                    )
                }

                DatePickerButton(
                    label = "Start Date",
                    date = expiryDate,
                    onDateSelected = { newDate ->
                        expiryDate = newDate
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
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
            buttonText = "Save",
            onClick = {
                val normalizedAmount = amount.replace(",", ".")

                when {
                    title.isEmpty() -> {
                        errorMessage = "Please enter a title"
                    }
                    normalizedAmount.toDoubleOrNull() == null -> {
                        errorMessage = "Amount must be a number"
                    }
                    (normalizedAmount.toDoubleOrNull() ?: 0.0) <= 0.0 -> {
                        errorMessage = "Amount must be greater than zero"
                    }
                    else -> {
                        val invoiceAmount = normalizedAmount.toDoubleOrNull() ?: 0.0
                        errorMessage = ""
                        viewModel.addInvoice(
                            title = title,
                            amount = invoiceAmount,
                            expiryDate = expiryDate
                        )
                        title = ""
                        amount = ""
                        expiryDate = Date()

                        viewModel.loadInvoicesByStatus(processed = selectedTabIndex == 1)
                    }
                }
            },
            isIncome = false,
            isExpense = true,
            isThirdButton = false,
            width = 0
        )

        Spacer(modifier = Modifier.height(16.dp))

        SegmentedButtonRow(
            options = listOf("Unprocessed Invoices", "Processed Invoices"),
            selectedIndex = selectedTabIndex,
            onSelectionChanged = { index ->
                selectedTabIndex = index
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTabIndex) {
            0 -> {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    if (unprocessedInvoiceState.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .padding(top = 20.dp)
                        ) {
                            Text(
                                "You have no unprocessed invoices right now.",
                                style = TextStyle(textAlign = TextAlign.Center),
                                fontSize = 23.sp,
                                color = if (isDarkMode) Color.White else colorResource(id = R.color.text_color)
                            )
                        }
                    } else {
                        CustomListView(
                            items = unprocessedInvoiceState,
                            deleteAction = { invoice ->
                                viewModel.deleteInvoice(invoiceId = invoice.id)
                            },
                            itemContent = { invoice ->
                                Triple(invoice.title, invoice.amount, invoice.expiryDate)
                            },
                            showNegativeAmount = false,
                            alignAmountInMiddle = true,
                            isInvoice = true,
                            onMarkAsProcessed = { item ->
                                invoiceToMarkAsProcessed = item
                                showAlertDialog.value = true
                            }
                        )
                    }
                }
            }

            1 -> {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    if (processedInvoices.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .padding(top = 20.dp)
                        ) {
                            Text(
                                "You have no processed invoices right now.",
                                style = TextStyle(textAlign = TextAlign.Center),
                                fontSize = 23.sp,
                                color = if (isDarkMode) Color.White else colorResource(id = R.color.text_color)
                            )
                        }
                    } else {
                        CustomListView(
                            items = processedInvoices,
                            deleteAction = { invoice ->
                                viewModel.deleteInvoice(invoiceId = invoice.id)
                            },
                            itemContent = { invoice ->
                                Triple(invoice.title, invoice.amount, invoice.expiryDate)
                            },
                            showNegativeAmount = false,
                            alignAmountInMiddle = true,
                            isInvoice = false
                        )
                    }
                }
            }
        }
        if (showAlertDialog.value) {
            CustomAlertDialog(
                show = true,
                onDismiss = {
                    showAlertDialog.value = false
                },
                onConfirm = {
                    val invoice = invoiceToMarkAsProcessed
                    if (invoice != null) {
                        viewModel.markInvoiceAsProcessed(
                            invoiceId = invoice.id,
                            processed = true
                        )
                    }
                    showAlertDialog.value = false
                },
                title = "Mark as processed",
                message = "Are you sure you want to mark this invoice as processed?",
                customColor = colorResource(id = R.color.income_color),
                confirmText = "Yes",
                cancelButtonText = "Go Back",
                onCancel = {
                    showAlertDialog.value = false
                },
            )
        }
    }
}

@Preview
@Composable
fun InvoiceReminderPreview() {
    InvoiceReminder(onDismiss = {})
}