package com.example.mybudgetbuddy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun MyBudgetBuddyNav(budgetViewModel: BudgetViewModel = viewModel())  {

    val navController = rememberNavController()
    val loggedIn by budgetViewModel.loggedIn.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (loggedIn) "main" else "unlogged"
    ) {
        composable("unlogged") {
            UnloggedScreen(navController)
        }
        composable("login") {
            LoginScreen(navController, budgetViewModel)
        }
        composable("register") {
            RegisterScreen(budgetViewModel)
        }
        composable("forgotPassword") {
            ForgotPasswordScreen(navController)
        }
        composable("main") {
            HomeView(budgetViewModel)
        }
    }
}
