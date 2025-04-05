package com.kat.mybudgetbuddy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kat.mybudgetbuddy.budget.BudgetViewModel
import com.kat.mybudgetbuddy.screens.HomeView
import com.kat.mybudgetbuddy.screens.LoginScreen
import com.kat.mybudgetbuddy.screens.RegisterScreen
import com.kat.mybudgetbuddy.screens.UnloggedScreen

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
            LoginScreen(budgetViewModel)
        }
        composable("register") {
            RegisterScreen(budgetViewModel)
        }
        composable("main") {
            HomeView(budgetViewModel)
        }
    }
}
