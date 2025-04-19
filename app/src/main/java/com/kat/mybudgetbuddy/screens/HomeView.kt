package com.kat.mybudgetbuddy.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kat.mybudgetbuddy.R
import com.kat.mybudgetbuddy.budget.BudgetManager
import com.kat.mybudgetbuddy.budget.BudgetViewModel
import com.kat.mybudgetbuddy.components.CustomAlertDialog
import com.kat.mybudgetbuddy.components.TabBar
import com.kat.mybudgetbuddy.components.TopAppBarWithMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(budgetViewModel: BudgetViewModel, viewModel: BudgetManager = viewModel()) {
    val navController = rememberNavController()

    val currentPeriod by viewModel.currentPeriod.observeAsState()
    val hasExistingPeriods by viewModel.hasExistingPeriods.observeAsState(false)
    val isCheckingPeriods by viewModel.isCheckingPeriods.observeAsState(true)

    var showInfoSheet by remember { mutableStateOf(false) }
    var showCategorySheet by remember { mutableStateOf(false) }
    var showInvoiceSheet by remember { mutableStateOf(false)}
    var showDeleteAccountSheet by remember { mutableStateOf(false)}
    var showLogOutAlert by remember { mutableStateOf(false) }

    var isVisible by remember { mutableStateOf(false) }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination?.route

    var showDropdownMenu by remember { mutableStateOf(false) }

    val isDarkMode = isSystemInDarkTheme()

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
                            IconButton(onClick = { showLogOutAlert = true }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Sign Out",
                                    tint = colorResource(id = R.color.expense_color),
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { showInfoSheet = true }) {
                                Icon(
                                    Icons.Outlined.Info,
                                    contentDescription = "Info",
                                    tint = colorResource(id = R.color.income_color),
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    )
                }
                "incomes" -> {
                    TopAppBarWithMenu(
                        isDarkMode = isDarkMode,
                        showDropdownMenu = showDropdownMenu,
                        onDropdownMenuChange = { showDropdownMenu = it },
                        onCategoryClick = { showCategorySheet = true },
                        onInvoiceClick = { showInvoiceSheet = true },
                        onAccountDeleteClick = { showDeleteAccountSheet = true }
                    )
                }
                "expenses" -> {
                    TopAppBarWithMenu(
                        isDarkMode = isDarkMode,
                        showDropdownMenu = showDropdownMenu,
                        onDropdownMenuChange = { showDropdownMenu = it },
                        onCategoryClick = { showCategorySheet = true },
                        onInvoiceClick = { showInvoiceSheet = true },
                        onAccountDeleteClick = { showDeleteAccountSheet = true }
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
                                PeriodDetailView(period = it, navController = navController)
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
                        },
                        isFirstTime = false
                    )
                }
                else -> {
                    // Show screen for creating a new period
                    NoCurrentPeriodScreen(
                        onPeriodCreated = {
                            viewModel.checkInitialState()
                        },
                        isFirstTime = true
                    )
                }
            }
        }
        if (showInfoSheet) {
            ModalBottomSheet(
                onDismissRequest = { showInfoSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                windowInsets = WindowInsets(0, 0, 0, 0),
                containerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxHeight(0.93f) // Takes 93% of screen height
            ) {
                InfoScreen(onDismiss = { showInfoSheet = false })
            }
        }

        if (showCategorySheet) {
            ModalBottomSheet(
                onDismissRequest = { showCategorySheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                windowInsets = WindowInsets(0, 0, 0, 0),
                containerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxHeight(0.93f) // Takes 93% of screen height
            ) {
                CategoryManagement(onDismiss = { showCategorySheet = false })
            }
        }

        if (showInvoiceSheet) {
            ModalBottomSheet(
                onDismissRequest = { showInvoiceSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                windowInsets = WindowInsets(0, 0, 0, 0),
                containerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxHeight(0.93f) // Takes 93% of screen height
            ) {
                InvoiceReminder(onDismiss = { showInvoiceSheet = false })
            }
        }

        if (showDeleteAccountSheet) {
            ModalBottomSheet(
                onDismissRequest = { showDeleteAccountSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                windowInsets = WindowInsets(0, 0, 0, 0),
                containerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxHeight(0.93f) // Takes 93% of screen height
            ) {
                DeleteAccountView(onDismiss = { showDeleteAccountSheet = false }, budgetViewModel = budgetViewModel)
            }
        }

        if (showLogOutAlert)  {
            CustomAlertDialog(
                show = true,
                onDismiss = {
                    showLogOutAlert = false
                },
                onConfirm = {
                    budgetViewModel.logout()
                },
                message = "Are you sure you want to sign out?",
                customColor = colorResource(id = R.color.error_message_color),
                confirmText = "Sign Out",
                cancelButtonText = "Go back!",
                onCancel = {
                    showLogOutAlert = false
                },
            )
        }
    }
}
