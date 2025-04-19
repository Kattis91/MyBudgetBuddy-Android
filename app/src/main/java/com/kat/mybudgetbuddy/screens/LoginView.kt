package com.kat.mybudgetbuddy.screens

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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kat.mybudgetbuddy.budget.BudgetViewModel
import com.kat.mybudgetbuddy.R
import com.kat.mybudgetbuddy.components.CustomButton
import com.kat.mybudgetbuddy.components.CustomTextField
import com.kat.mybudgetbuddy.utils.ValidationUtils

@Composable
fun LoginScreen(budgetViewModel: BudgetViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailErrorMessage by remember { mutableStateOf("") }
    var passwordErrorMessage by remember { mutableStateOf("") }
    var generalErrorMessage by remember { mutableStateOf("") }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Show login content only when forgot password is not shown
        if (!showForgotPasswordDialog) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.savings),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .height(200.dp)
                        .width(200.dp)
                        .padding(bottom = 16.dp),
                    tint = Color.Unspecified
                )

                Spacer(modifier = Modifier.height(70.dp))

                Box(modifier = Modifier.padding(horizontal = 30.dp)) {
                    CustomTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        icon = Icons.Default.Email,
                        onChange = {
                            emailErrorMessage = ""
                        }
                    )
                }

                Box(
                    modifier = Modifier
                        .heightIn(min = 30.dp)
                        .align(Alignment.Start)
                        .padding(start = 30.dp)
                ) {
                    if (emailErrorMessage.isNotEmpty()) {
                        Text(
                            text = emailErrorMessage,
                            color = colorResource(id = R.color.error_message_color)
                        )
                    }
                }

                Box(modifier = Modifier.padding(horizontal = 30.dp)) {
                    CustomTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        icon = Icons.Default.Lock,
                        onChange = {
                            passwordErrorMessage = ""
                        },
                        isSecure = true
                    )
                }

                Box(
                    modifier = Modifier
                        .heightIn(min = 30.dp)
                        .align(Alignment.Start)
                        .padding(start = 30.dp)
                ) {
                    if (passwordErrorMessage.isNotEmpty()) {
                        Text(
                            text = passwordErrorMessage,
                            color = colorResource(id = R.color.error_message_color)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(
                        onClick = {
                            showForgotPasswordDialog = true
                        },
                        modifier = Modifier.padding(end = 45.dp)
                    ) {
                        Text("Forgot Password?")
                    }
                }

                Box(
                    modifier = Modifier
                        .heightIn(min = 50.dp)
                        .padding(horizontal = 60.dp)
                ) {
                    if (generalErrorMessage.isNotEmpty()) {
                        Text(
                            text = generalErrorMessage,
                            color = colorResource(id = R.color.error_message_color)
                        )
                    }
                }

                CustomButton(
                    buttonText = "Sign In",
                    onClick = {
                        emailErrorMessage = ValidationUtils.validateEmail(email) ?: ""
                        passwordErrorMessage = ValidationUtils.validatePassword(password) ?: ""

                        // Check if there are any validation errors
                        if (emailErrorMessage.isEmpty() && passwordErrorMessage.isEmpty()) {
                            // Proceed with the register or login process
                            budgetViewModel.login(email, password) { firebaseError ->
                                generalErrorMessage = firebaseError ?: ""
                            }
                        }
                    },
                    isIncome = false,
                    isExpense = true,
                    isThirdButton = false,
                    width = 200
                )
            }
        } else {
            // Show only the forgot password screen
            ForgotPasswordScreen(
                budgetViewModel = budgetViewModel,
                onDismiss = { showForgotPasswordDialog = false },
                deletingAccountReset = false
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(viewModel())
}