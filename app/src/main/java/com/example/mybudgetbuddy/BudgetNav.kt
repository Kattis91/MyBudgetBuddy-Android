package com.example.mybudgetbuddy

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun MyBudgetBuddyNav()  {

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "unlogged") {
        composable("unlogged") {
            UnloggedScreen(navController)
        }
        composable("login") {
            LoginScreen(navController)
        }
        composable("register") {
            RegisterScreen()
        }
        composable("forgotPassword") {
            ForgotPasswordScreen()
        }
    }
}
