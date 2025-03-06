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

@Composable
fun UnloggedScreen(navController: NavController) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Icon(
                painter = painterResource(id = R.drawable.savings),
                contentDescription = "App Logo",
                modifier = Modifier
                    .height(300.dp)
                    .width(300.dp)
                    .padding(bottom = 16.dp)
                    .padding(top = 70.dp),
                tint = Color.Unspecified
            )

            Text(
                "MyBudgetBuddy",
                fontSize = 30.sp,
                color = colorResource(id = R.color.text_color),
                modifier = Modifier
                    .padding(top = 75.dp)
            )

            Text(
                "Managing Money shouldn't be hard",
                fontSize = 20.sp,
                color = colorResource(id = R.color.text_color),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 10.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 100.dp)
        ) {
            Button(onClick = {
                navController.navigate("login")
            },
                modifier = Modifier.width(180.dp)
            ) {
                Text("Sign In")
            }

            Button(onClick = {
                navController.navigate("register")
            },
                modifier = Modifier.width(180.dp)
            ) {
                Text("Create account")
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }

}

@Preview(showBackground = true)
@Composable
fun UnloggedScreenPreview() {
    val previewNavController = rememberNavController()
    UnloggedScreen(previewNavController)
}