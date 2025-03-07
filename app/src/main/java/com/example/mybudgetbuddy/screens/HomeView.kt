package com.example.mybudgetbuddy.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mybudgetbuddy.BudgetViewModel
import com.example.mybudgetbuddy.components.TabBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(budgetViewModel: BudgetViewModel) {
    val navController = rememberNavController()

    val startDate = "2023-01-01"
    val endDate = "2023-12-31"
    val totalIncome = 10000.0
    val totalExpense = 5000.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MyBudgetBuddy") },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Info, contentDescription = "Info")
                    }
                    IconButton(onClick = { budgetViewModel.logout() }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sign Out")
                    }
                }
            )
        },
        bottomBar = {
            TabBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeTabView()
            }
            composable("incomes") {
                IncomesTabView(startDate, endDate, totalIncome)
            }
            composable("expenses") {
                ExpensesTabView(startDate, endDate, totalExpense)
            }
            composable("overview") {
                OverviewTabView()
            }
        }
    }
}