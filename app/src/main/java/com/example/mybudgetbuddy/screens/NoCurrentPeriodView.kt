package com.example.mybudgetbuddy.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mybudgetbuddy.R
import com.example.mybudgetbuddy.components.CustomButton

@Composable
fun NoCurrentPeriodScreen(
    onPeriodCreated: () -> Unit,
    isFirstTime: Boolean = false
) {
    var showNewPeriodDialog by remember { mutableStateOf(false) }

    val isDarkMode = isSystemInDarkTheme()

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

        Text(
            if (isFirstTime) "Welcome to MyBudgetBuddy!" else "Your last budget period has ended",
            fontSize = 25.sp,
            modifier = Modifier.padding(top = 16.dp),
            color = if (isDarkMode) Color.White else colorResource(id = R.color.text_color)
        )

        Spacer(modifier = Modifier.height(26.dp))

        Text(
            if (isFirstTime) "To start tracking your budget, you'll need to create your first budget period."
            else "Start a new period to continue tracking your budget",
            fontSize = 20.sp,
            modifier = Modifier
                .padding(horizontal = 10.dp),
            color = if (isDarkMode) Color.White else colorResource(id = R.color.text_color),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(26.dp))

        CustomButton(
            onClick = {
                showNewPeriodDialog = true
            },
            buttonText = if (isFirstTime) "Create Budget Period" else "Start New Period",
            isIncome = false,
            isExpense = true,
            isThirdButton = false,
            width = 215
        )

        // Show New Budget Period Dialog when button is clicked
        if (showNewPeriodDialog) {
            NewBudgetPeriodView(
                isPresented = true,
                onDismiss = { showNewPeriodDialog = false },
                onSuccess = {
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NoCurrentPeriodScreenPreview() {
    NoCurrentPeriodScreen(onPeriodCreated = {})
}

