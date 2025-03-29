package com.example.mybudgetbuddy.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Doorbell
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mybudgetbuddy.R
import com.example.mybudgetbuddy.components.CustomButton
import com.example.mybudgetbuddy.components.CustomTextField
import com.example.mybudgetbuddy.components.DatePickerButton
import java.util.Date

@Composable
fun InvoiceReminder() {

    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf(Date()) }

    val isDarkMode = isSystemInDarkTheme()

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Manage Invoices",
                fontSize = 20.sp,
                color = if (isDarkMode) Color.White else colorResource(id = R.color.text_color)
            )

            Spacer(modifier = Modifier.height(20.dp))
            CustomTextField(
                value = title,
                onValueChange = { title = it },
                label = "Title",
                icon = Icons.Default.Doorbell
            )
            Spacer(modifier = Modifier.height(14.dp))
            CustomTextField(
                value = amount,
                onValueChange = { amount = it },
                label = "Amount",
                icon = Icons.Default.AttachMoney
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                Icon(
                    imageVector = Icons.Filled.AccessTime,
                    contentDescription = "Clock",
                    tint = Color.Gray,
                    modifier = Modifier.padding(start = 10.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "Due to:",
                    color = Color.Gray,
                )
            }

            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DatePickerButton(
                    label = "Start Date",
                    date = expiryDate,
                    onDateSelected = { newDate ->
                        expiryDate = newDate
                    }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            CustomButton(
                buttonText = "Save",
                onClick = {
                    // Handle save button click
                },
                isIncome = false,
                isExpense = true,
                isThirdButton = false,
                width = 0
            )
        }
    }
}

@Preview
@Composable
fun InvoiceReminderPreview() {
    InvoiceReminder()
}