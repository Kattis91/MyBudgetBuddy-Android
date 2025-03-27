package com.example.mybudgetbuddy.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mybudgetbuddy.components.SegmentedButtonRow

@Composable
fun CategoryManagement() {

    var selectedTabIndex by remember { mutableStateOf(0) }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(top = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    )  {

        Text("Manage Categories",
            fontSize = 20.sp,
        )

        Spacer(modifier = Modifier.height(20.dp))

        SegmentedButtonRow(
            options = listOf("Incomes", "Fixed", "Variable"),
            selectedIndex = selectedTabIndex,
            onSelectionChanged = { index ->
                selectedTabIndex = index
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        when (selectedTabIndex) {
            0 -> {
                Text("Income Categories")
            }
            1 -> {
                Text("Fixed Expense Categories")
            }
            2 -> {
                Text("Variable Expense Categories")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryManagementPreview() {
    CategoryManagement()
}