package com.example.mybudgetbuddy.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.ui.graphics.vector.ImageVector

data class FeatureItem(
    val icon: ImageVector,
    val text: String
)

val features = listOf(
    FeatureItem(Icons.Default.FormatListNumbered, "Add your income and expenses, with expenses categorized as fixed or variable."),
    FeatureItem(Icons.Default.Category, "Organize your finances better by adding your own categories, or stick with the default ones."),
    FeatureItem(Icons.Default.CalendarMonth, "Customize your own budget periods: choose from monthly, weekly, or even 5-day periods to match your needs."),
    FeatureItem(Icons.Default.BarChart, "Keep track of your entire budget history, so you can see exactly where your money goes.")
)

val extraFeatures = listOf(
    FeatureItem(Icons.Default.AccessTime, "Stay in Control – Get timely reminders when your budget period is ending, so you can plan ahead with confidence."),
    FeatureItem(Icons.Default.Alarm, "Never Miss a Payment – Invoice reminders ensure you stay on track and stress-free when it comes to due dates.")
)

val aboutTheDeveloper = listOf(
    FeatureItem(Icons.Default.Computer, "I’m Ekaterina Durneva Svedmark, the creator of this app and an aspiring app developer. This is my third project and my first independent app, built from concept to launch.")
)