package com.example.mybudgetbuddy.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mybudgetbuddy.R
import com.example.mybudgetbuddy.budget.BudgetManager
import com.example.mybudgetbuddy.budget.BudgetViewModel
import com.example.mybudgetbuddy.components.TabBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(budgetViewModel: BudgetViewModel, viewModel: BudgetManager = viewModel()) {
    val navController = rememberNavController()

    val currentPeriod by viewModel.currentPeriod.observeAsState()
    val hasExistingPeriods by viewModel.hasExistingPeriods.observeAsState(false)
    val isCheckingPeriods by viewModel.isCheckingPeriods.observeAsState(true)

    var showInfoSheet by remember { mutableStateOf(false) }
    var showCategorySheet by remember { mutableStateOf(false) }

    var isVisible by remember { mutableStateOf(false) }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination?.route

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Scaffold(
        topBar = {
            when (currentDestination) {
                "home" -> {
                    TopAppBar(
                        title = { },
                        navigationIcon = {
                            IconButton(onClick = { budgetViewModel.logout() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Sign Out"
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { showInfoSheet = true }) {
                                Icon(Icons.Outlined.Info, contentDescription = "Info")
                            }
                        }
                    )
                }
                "incomes" -> {
                    TopAppBar(
                        title = { },
                        actions = {
                            IconButton(onClick = { showCategorySheet = true }) {
                                Icon(Icons.Filled.Settings, contentDescription = "Settings")
                            }
                        }
                    )
                }
                "expenses" -> {
                    TopAppBar(
                        title = { },
                        actions = {
                            IconButton(onClick = { showCategorySheet = true }) {
                                Icon(Icons.Filled.Settings, contentDescription = "Settings")
                            }
                        }
                    )
                }
            }
        },
        bottomBar = {
            if (!isCheckingPeriods && currentPeriod != null) {
                TabBar(navController = navController)
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when {
                isCheckingPeriods -> {
                    // Show a loading indicator while fetching data
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AnimatedVisibility(
                            visible = isVisible,
                            enter = fadeIn(animationSpec = tween(durationMillis = 1000))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.savings),
                                contentDescription = "App Icon",
                                modifier = Modifier.size(125.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(text = "Preparing your budget details...")
                    }
                }
                currentPeriod != null -> {
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
                                OverviewTabView(period = period, navController = navController)
                            }
                        }
                        composable("periodDetail/{periodId}") { backStackEntry ->
                            val periodId = backStackEntry.arguments?.getString("periodId")

                            // Fetch the period based on the periodId
                            val period = viewModel.getPeriodById(periodId)

                            period?.let {
                                PeriodDetailView(period = it)
                            } ?: run {
                                Text("Period not found") // Handle the case where period is null
                            }
                        }

                    }
                }
                hasExistingPeriods -> {
                    // Show no current period view
                    NoCurrentPeriodScreen(
                        onPeriodCreated = {
                            viewModel.checkInitialState()
                        }
                    )
                }
                else -> {
                    // Show screen for creating a new period
                    NoCurrentPeriodScreen(
                        onPeriodCreated = {
                            viewModel.checkInitialState()
                        }
                    )
                }
            }
        }
        if (showInfoSheet) {
            ModalBottomSheet(
                onDismissRequest = { showInfoSheet = false }
            ) {
                InfoScreen()
            }
        }

        if (showCategorySheet) {
            ModalBottomSheet(
                onDismissRequest = { showCategorySheet = false }
            ) {
                CategoryManagement()
            }
        }
    }
}
