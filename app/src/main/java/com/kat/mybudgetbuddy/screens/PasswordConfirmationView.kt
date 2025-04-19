package com.kat.mybudgetbuddy.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kat.mybudgetbuddy.R
import com.kat.mybudgetbuddy.budget.BudgetViewModel
import com.kat.mybudgetbuddy.components.CustomButton
import com.kat.mybudgetbuddy.components.CustomTextField

@Composable
fun PasswordConfirmationView(
    budgetViewModel: BudgetViewModel,
    onDismiss: () -> Unit,
) {
    var confirmationText by remember { mutableStateOf("") }
    var confirmationPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .padding(horizontal = 18.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row {
                    Spacer(Modifier.weight(1f))
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colorResource(id = R.color.expense_color),
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Confirm Deletion",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 50.dp),
                        color = colorResource(id = R.color.text_color)
                    )
                }

                CustomTextField(
                    value = confirmationText,
                    onValueChange = { confirmationText = it },
                    label = "Type DELETE to confirm",
                    icon = Icons.Default.Email,
                    onChange = {
                        errorMessage = ""
                    }
                )
                Spacer(modifier = Modifier.height(20.dp))

                CustomTextField(
                    value = confirmationPassword,
                    onValueChange = { confirmationPassword = it },
                    label = "Your current password",
                    icon = Icons.Default.Email,
                    onChange = {
                        errorMessage = ""
                    }
                )

                Spacer(modifier = Modifier.padding(bottom = 10.dp))

                Box(modifier = Modifier
                    .heightIn(min = 75.dp)
                    .animateContentSize()
                    .padding(top = 10.dp)
                    .padding(horizontal = 30.dp)) {
                    val message = errorMessage.takeIf { it.isNotEmpty() } ?: successMessage.takeIf { it.isNotEmpty() }
                    val color = when {
                        errorMessage.isNotEmpty() -> colorResource(id = R.color.error_message_color)
                        successMessage.isNotEmpty() -> colorResource(id = R.color.success_message_color)
                        else -> Color.Unspecified
                    }

                    message?.let {
                        Text(
                            text = it,
                            color = color
                        )
                    }
                }
                CustomButton(
                    buttonText = "Delete Account",
                    onClick = {
                        if (confirmationText != "DELETE") {
                            errorMessage = "You must type DELETE to confirm"
                            return@CustomButton
                        }
                        budgetViewModel.deleteAccount(
                            password = confirmationPassword,
                            onComplete = {
                                successMessage = "Account successfully deleted"
                                errorMessage = ""
                                onDismiss() // Dismiss the dialog or navigate away
                            },
                            onError = { error ->
                                errorMessage = error
                                successMessage = ""
                            }
                        )
                    },
                    isIncome = false,
                    isExpense = true,
                    isThirdButton = false,
                    width = 200
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordViewPreview() {
    PasswordConfirmationView(viewModel(), onDismiss = {})
}