package com.example.mybudgetbuddy.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.FormatListNumbered
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mybudgetbuddy.R
import com.example.mybudgetbuddy.components.FeatureCard
import com.example.mybudgetbuddy.components.SegmentedButtonRow

@Composable
fun InfoScreen() {
    val scrollState = rememberScrollState()
    var selectedTabIndex by remember { mutableStateOf(0) }

    val isDarkMode = isSystemInDarkTheme()
    val textColor = if (isDarkMode) Color.White else colorResource(id = R.color.text_color)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(
            "MyBudgetBuddy",
            fontSize = 30.sp,
            color = textColor
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Budgeting made easy, so you can focus on what matters most to you!",
            fontSize = 20.sp,
            modifier = Modifier.padding(horizontal = 10.dp),
            color = textColor
        )

        Spacer(modifier = Modifier.height(20.dp))

        SegmentedButtonRow(
            options = listOf("About The App", "Development"),
            selectedIndex = selectedTabIndex,
            onSelectionChanged = { index ->
                selectedTabIndex = index
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (selectedTabIndex == 0) {
            Text(
                "How it works:",
                fontSize = 18.sp,
                modifier = Modifier.padding(vertical = 10.dp),
                color = textColor
            )

            FeatureCard(
                icon = Icons.Default.FormatListNumbered,
                text = "Add your income and expenses, with expenses categorized as fixed or variable."
            )

            FeatureCard(
                icon = Icons.Default.Category,
                text = "Organize your finances better by adding your own categories, or stick with the default ones."
            )

            FeatureCard(
                icon = Icons.Default.CalendarMonth,
                text = "Customize your own budget periods: choose from monthly, weekly, or even 5-day periods to match your needs."
            )

            FeatureCard(
                icon = Icons.Default.BarChart,
                text = "Keep track of your entire budget history, so you can see exactly where your money goes."
            )

            Text(
                "Coming Soon:",
                fontSize = 18.sp,
                modifier = Modifier.padding(vertical = 10.dp)
            )

            FeatureCard(
                icon = Icons.Default.AccessTime,
                text = "Stay in Control – Get timely reminders when your budget period is ending, so you can plan ahead with confidence."
            )

            FeatureCard(
                icon = Icons.Default.Alarm,
                text = "Never Miss a Payment – Invoice reminders ensure you stay on track and stress-free when it comes to due dates."
            )
        } else if (selectedTabIndex == 1) {
            Text(
                "The Developer:",
                fontSize = 18.sp,
                modifier = Modifier.padding(vertical = 10.dp),
                color = textColor
            )

            FeatureCard(
                icon = Icons.Default.Computer,
                text = "I’m Ekaterina Durneva Svedmark, the creator of this app and an aspiring app developer. This is my third project and my first independent app, built from concept to launch."
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InfoScreenPreview() {
    InfoScreen()
}