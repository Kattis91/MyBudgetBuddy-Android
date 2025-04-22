package com.kat.mybudgetbuddy.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Doorbell
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.kat.mybudgetbuddy.R
import com.kat.mybudgetbuddy.budget.BudgetManager
import com.kat.mybudgetbuddy.components.AnimatedSegmentedButtonRow
import com.kat.mybudgetbuddy.components.CustomAlertDialog
import com.kat.mybudgetbuddy.components.CustomButton
import com.kat.mybudgetbuddy.components.CustomListView
import com.kat.mybudgetbuddy.components.CustomTextField
import com.kat.mybudgetbuddy.components.DatePickerButton
import com.kat.mybudgetbuddy.models.Invoice
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceReminder(
    viewModel: BudgetManager = viewModel(),
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val invoices by viewModel.invoices.collectAsState()
    val unprocessedInvoiceState by viewModel.unprocessedInvoices.collectAsState()
    val isLoading by viewModel.isLoading.observeAsState(initial = false)
    val unprocessedInvoices = remember { mutableStateListOf<Invoice>() }
    val processedInvoices = remember { mutableStateListOf<Invoice>() }

    var selectedTabIndex by remember { mutableStateOf(0) }
    val showAlertDialog = remember { mutableStateOf(false) }
    var invoiceToMarkAsProcessed by remember { mutableStateOf<Invoice?>(null) }
    var showScannerSheet by remember { mutableStateOf(false) }

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
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "Manage Invoices",
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
                    modifier = Modifier.padding(end = 10.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
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

            Spacer(modifier = Modifier.height(12.dp))

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
                        color = if (isDarkMode) Color.White else colorResource(id = R.color.text_color)
                    )
                }

                DatePickerButton(
                    label = "Start Date",
                    date = expiryDate.takeIf { it.isNotEmpty() }?.let {
                        try {
                            SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(it) ?: Date()
                        } catch (e: Exception) {
                            Log.e("InvoiceReminder", "Failed to parse date: $it", e)
                            Date() // Fallback to current date
                        }
                    } ?: Date(),
                    onDateSelected = { newDate ->
                        expiryDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(newDate)
                    },
                    modifier = Modifier
                        .background(
                            color = Color.Gray.copy(alpha = 0.3f), // Light gray background
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(onClick = {
                showScannerSheet = true
            }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Scanner,
                        contentDescription = "Scan",
                        tint = if (isDarkMode) Color.White else colorResource(id = R.color.text_color),
                        modifier = Modifier.size(45.dp)
                    )
                    Text("Scan the invoice")
                }
            }
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
                            expiryDate = expiryDate.takeIf { it.isNotEmpty() }?.let {
                                try {
                                    SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(it) ?: Date()
                                } catch (e: Exception) {
                                    Log.e("InvoiceReminder", "Failed to parse date: $it", e)
                                    Date() // Fallback to current date
                                }
                            } ?: Date()
                        )
                        title = ""
                        amount = ""
                        expiryDate = Date().toString()

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

        AnimatedSegmentedButtonRow (
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
                message = "Are you sure you want to mark ${invoiceToMarkAsProcessed?.title} as processed?",
                customColor = colorResource(id = R.color.income_color),
                confirmText = "Yes",
                cancelButtonText = "Go Back",
                onCancel = {
                    showAlertDialog.value = false
                },
            )
        }
        if (showScannerSheet) {
            ModalBottomSheet(
                onDismissRequest = { showScannerSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                windowInsets = WindowInsets(0, 0, 0, 0),
                containerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxHeight(0.93f) // Takes 93% of screen height
            ) {
                InvoiceScannerView(
                    onDismiss = { showScannerSheet = false },
                    scannedAmount = amount,
                    onAmountChange = { amount = it },
                    scannedDueDate = expiryDate,
                    onDueDateChange = { expiryDate = it }
                )
            }
        }
    }
}

@Preview
@Composable
fun InvoiceReminderPreview() {
    InvoiceReminder(onDismiss = {})
}