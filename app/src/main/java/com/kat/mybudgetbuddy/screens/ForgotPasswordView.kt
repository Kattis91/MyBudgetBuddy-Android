package com.kat.mybudgetbuddy.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kat.mybudgetbuddy.budget.BudgetViewModel
import com.kat.mybudgetbuddy.R
import com.kat.mybudgetbuddy.components.CustomButton
import com.kat.mybudgetbuddy.components.CustomTextField
import com.kat.mybudgetbuddy.utils.ValidationUtils

@Composable
fun ForgotPasswordScreen(
    budgetViewModel : BudgetViewModel,
    onDismiss: () -> Unit,
    deletingAccountReset : Boolean
) {
    val isDarkMode = isSystemInDarkTheme()

    var email by remember { mutableStateOf("") }
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
                        text = stringResource(R.string.reset_password),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 50.dp),
                        color = if (isDarkMode) Color.White else colorResource(id = R.color.text_color)
                    )
                }

                Spacer(modifier = Modifier.padding(bottom = 10.dp))

                CustomTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    icon = Icons.Default.Email,
                    onChange = {
                        errorMessage = ""
                    }
                )

                Spacer(modifier = Modifier.padding(bottom = 10.dp))

                Box(modifier = Modifier
                    .heightIn(min = 55.dp)
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
                    buttonText = stringResource(R.string.send_reset_link),
                    onClick = {
                        val validationError = ValidationUtils.validateReset(email)
                        if (validationError != null) {
                            errorMessage = validationError
                            successMessage = ""
                        } else {
                            budgetViewModel.resetPassword(email) { firebaseError ->
                                if (firebaseError.isNotEmpty()) {
                                    errorMessage = firebaseError
                                    successMessage = ""
                                } else {
                                    successMessage = if (deletingAccountReset)  "Please check your email. Once password is reset, return to delete your account." else "If the email you provided is registered, we've sent a reset link to your inbox."
                                    email = ""
                                    errorMessage = ""
                                }
                            }
                        }
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
fun ForgotPasswordScreenPreview() {
    ForgotPasswordScreen(viewModel(), onDismiss = {}, deletingAccountReset = false)
}