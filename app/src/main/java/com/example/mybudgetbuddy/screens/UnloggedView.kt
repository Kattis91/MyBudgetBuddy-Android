package com.example.mybudgetbuddy.screens

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mybudgetbuddy.R

@Composable
fun UnloggedScreen(navController: NavController) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {

        Icon(
            painter = painterResource(id = R.drawable.savings),
            contentDescription = "App Logo",
            modifier = Modifier
                .height(225.dp)
                .width(225.dp),

            tint = Color.Unspecified
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            "MyBudgetBuddy",
            fontSize = 30.sp,
            color = colorResource(id = R.color.text_color),
            modifier = Modifier
                .padding(top = 55.dp)
        )

        Text(
            "Managing Money shouldn't be hard",
            fontSize = 20.sp,
            color = colorResource(id = R.color.text_color),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 10.dp)
        )

        Spacer(modifier = Modifier.height(70.dp))


        Button(
            onClick = {
                navController.navigate("login")
            },
            modifier = Modifier
                .width(180.dp)
                .padding(bottom = 5.dp)
        ) {
            Text("Sign In")
        }

        Button(
            onClick = {
                navController.navigate("register")
            },
            modifier = Modifier.width(180.dp)
        ) {
            Text("Create account")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UnloggedScreenPreview() {
    val previewNavController = rememberNavController()
    UnloggedScreen(previewNavController)
}