package com.kat.mybudgetbuddy.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kat.mybudgetbuddy.R
import com.kat.mybudgetbuddy.budget.BudgetViewModel
import com.kat.mybudgetbuddy.components.CustomButton

@Composable
fun DeleteAccountView(
    budgetViewModel: BudgetViewModel,
    onDismiss: () -> Unit = {}
) {
    val isDarkMode = isSystemInDarkTheme()
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!showForgotPasswordDialog && !showDeleteConfirmationDialog) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    // Centered title
                    Text(text = stringResource(R.string.delete_account),
                        fontSize = 21.sp,
                        color = if (isDarkMode) Color.White else colorResource(id = R.color.text_color),
                        modifier = Modifier.align(Alignment.Center)
                    )

                    // Close button aligned to the end
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colorResource(id = R.color.expense_color)
                        )
                    }
                }
                // Centered Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // Take remaining space
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Warning Icon",
                        tint = Color.Red.copy(alpha = 0.7f),
                        modifier = Modifier.size(45.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.warning_account_deletion),
                        fontSize = 20.sp,
                        color = Color(0xFFFF9800)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.this_action_cannot_be_undone),
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    CustomButton(
                        buttonText = stringResource(R.string.delete_with_password),
                        onClick = { showDeleteConfirmationDialog = true },
                        isIncome = false,
                        isExpense = true,
                        isThirdButton = false,
                        width = 0
                    )
                    CustomButton(
                        buttonText = stringResource(R.string.forgot_password),
                        onClick = { showForgotPasswordDialog = true },
                        isIncome = false,
                        isExpense = true,
                        isThirdButton = false,
                        width = 0
                    )
                }
            }
        } else if (showForgotPasswordDialog) {
            ForgotPasswordScreen(
                budgetViewModel = budgetViewModel,
                onDismiss = { showForgotPasswordDialog = false },
                deletingAccountReset = true
            )
        } else {
            PasswordConfirmationView(
                budgetViewModel = budgetViewModel,
                onDismiss = { showDeleteConfirmationDialog = false }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DeleteAccountViewPreview() {
    DeleteAccountView(viewModel())
}