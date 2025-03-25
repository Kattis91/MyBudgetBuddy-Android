package com.example.mybudgetbuddy.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mybudgetbuddy.BudgetManager
import com.example.mybudgetbuddy.BudgetViewModel
import com.example.mybudgetbuddy.components.TabBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(budgetViewModel: BudgetViewModel, viewModel: BudgetManager = viewModel()) {
    val navController = rememberNavController()

    val currentPeriod by viewModel.currentPeriod.observeAsState()
    val hasExistingPeriods by viewModel.hasExistingPeriods.observeAsState(false)
    val isCheckingPeriods by viewModel.isCheckingPeriods.observeAsState(true)

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
            if (currentPeriod != null) {
                TabBar(navController = navController)
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when {
                isCheckingPeriods -> {
                    // Show progress indicator
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Preparing your budget details..."
                        )
                    }
                }
                hasExistingPeriods -> {
                    if (currentPeriod != null) {
                        // Show the main content with tabs
                        NavHost(
                            navController = navController,
                            startDestination = "home",
                        ) {
                            composable("home") { HomeTabView() }
                            composable("incomes") { IncomesTabView() }
                            composable("expenses") { ExpensesTabView() }
                            composable("overview") {
                                currentPeriod?.let { period ->
                                    OverviewTabView(period = period)
                                }
                            }
                        }
                    } else {
                        // Show no current period view
                        NoCurrentPeriodScreen(
                            onPeriodCreated = {
                                viewModel.checkInitialState()
                            }
                        )
                    }
                }
                else -> {
                    // Show no periods view
                    NoCurrentPeriodScreen(
                        onPeriodCreated = {
                            viewModel.checkInitialState()
                        }
                    )
                }
            }
        }
    }
}
