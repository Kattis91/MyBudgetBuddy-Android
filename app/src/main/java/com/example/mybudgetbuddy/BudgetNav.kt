package com.example.mybudgetbuddy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mybudgetbuddy.budget.BudgetViewModel
import com.example.mybudgetbuddy.screens.ForgotPasswordScreen
import com.example.mybudgetbuddy.screens.HomeView
import com.example.mybudgetbuddy.screens.LoginScreen
import com.example.mybudgetbuddy.screens.RegisterScreen
import com.example.mybudgetbuddy.screens.UnloggedScreen

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
            ForgotPasswordScreen(navController, budgetViewModel)
        }
        composable("main") {
            HomeView(budgetViewModel)
        }
    }
}
