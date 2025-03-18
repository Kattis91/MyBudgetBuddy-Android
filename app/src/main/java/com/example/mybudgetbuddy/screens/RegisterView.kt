package com.example.mybudgetbuddy.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetbuddy.BudgetViewModel
import com.example.mybudgetbuddy.R
import com.example.mybudgetbuddy.utils.ValidationUtils

@Composable
fun RegisterScreen(budgetViewModel : BudgetViewModel) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var emailErrorMessage by remember { mutableStateOf("") }
    var passwordErrorMessage by remember { mutableStateOf("") }
    var confirmPasswordErrorMessage by remember { mutableStateOf("") }
    var generalErrorMessage by remember { mutableStateOf("") }

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

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )

        Box(modifier = Modifier.heightIn(min = 30.dp)) {
            if (emailErrorMessage.isNotEmpty()) {
                Text(text = emailErrorMessage,
                    color = colorResource(id = R.color.error_message_color)
                )
            }
        }

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") }
        )

        Box(modifier = Modifier.heightIn(min = 30.dp)) {
            if (passwordErrorMessage.isNotEmpty()) {
                Text(text = passwordErrorMessage,
                    color = colorResource(id = R.color.error_message_color))
            }
        }

        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") }
        )

        Box(modifier = Modifier.heightIn(min = 30.dp)) {
            if (confirmPasswordErrorMessage.isNotEmpty()) {
                Text(text = confirmPasswordErrorMessage,
                    color = colorResource(id = R.color.error_message_color))
            }
        }

        Box(modifier = Modifier.heightIn(min = 50.dp).padding(horizontal = 60.dp)) {
            if (generalErrorMessage.isNotEmpty()) {
                Text(text = generalErrorMessage,
                    color = colorResource(id = R.color.error_message_color))
            }
        }

        Button(onClick = {
            emailErrorMessage = ValidationUtils.validateEmail(email) ?: ""
            passwordErrorMessage = ValidationUtils.validatePassword(password) ?: ""
            confirmPasswordErrorMessage = ValidationUtils.validateConfirmPassword(password, confirmPassword) ?: ""

            // Check if there are any validation errors
            if (emailErrorMessage.isEmpty() && passwordErrorMessage.isEmpty() && confirmPasswordErrorMessage.isEmpty()) {
                // Proceed with the register or login process
                budgetViewModel.register(email, password) { firebaseError ->
                    generalErrorMessage = firebaseError ?: ""
                }
            }
        },
            modifier = Modifier.width(150.dp)
        ) {
            Text("Sign Up")
        }

    }

}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(viewModel())
}