package com.example.mybudgetbuddy.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mybudgetbuddy.BudgetViewModel
import com.example.mybudgetbuddy.R
import com.example.mybudgetbuddy.components.CustomButton
import com.example.mybudgetbuddy.utils.ValidationUtils

@Composable
fun ForgotPasswordScreen(navController: NavController, budgetViewModel : BudgetViewModel) {

    var email by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),

                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.weight(1f))

                TextButton(
                    onClick = {
                        navController.popBackStack()
                    },
                    modifier = Modifier.padding(end = 20.dp)
                ) {
                    Text("X")
                }
            }

            Text(
                "Reset Password",
                fontSize = 20.sp,
                color = colorResource(id = R.color.text_color),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 50.dp)
            )

            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") }
            )

            Box(modifier = Modifier
                .heightIn(min = 55.dp)
                .padding(top = 10.dp)
                .padding(horizontal = 60.dp)) {
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
                buttonText = "Send Reset Link",
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
                                successMessage = "If the email you provided is registered, we've sent a reset link to your inbox."
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

@Preview(showBackground = true)
@Composable
fun ForgotPasswordScreenPreview() {
    val previewNavController = rememberNavController()
    ForgotPasswordScreen(previewNavController, viewModel())
}