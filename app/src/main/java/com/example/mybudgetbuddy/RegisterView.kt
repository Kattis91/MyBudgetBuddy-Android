package com.example.mybudgetbuddy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun RegisterScreen() {

    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }

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
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email") }
        )

        Spacer(modifier = Modifier.height(25.dp))

        TextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("Password") }
        )

        Spacer(modifier = Modifier.height(25.dp))

        TextField(
            value = confirmPassword.value,
            onValueChange = { confirmPassword.value = it },
            label = { Text("Confirm Password") }
        )

        Spacer(modifier = Modifier.height(70.dp))

        Button(onClick = {

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
    RegisterScreen()
}