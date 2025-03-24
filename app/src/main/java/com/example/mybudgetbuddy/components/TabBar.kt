package com.example.mybudgetbuddy.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.StackedBarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mybudgetbuddy.R
import com.example.mybudgetbuddy.TabItem

@Composable
fun TabBar(navController: NavController) {
    val items = listOf(
        TabItem("Home", Icons.Filled.Home, "home"),
        TabItem("Incomes", Icons.Filled.AddCircle, "incomes"),
        TabItem("Expenses", Icons.Filled.RemoveCircle, "expenses"),
        TabItem("Overview", Icons.Filled.StackedBarChart, "overview")
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isDarkMode = isSystemInDarkTheme()

    val gradientColors = if (isDarkMode) {
        listOf(Color(40, 40, 45), Color(28, 28, 32))
    } else {
        listOf(Color(252, 242, 230), Color(245, 235, 220))
    }

    NavigationBar(
        modifier = Modifier.drawBehind {
            drawRect(
                brush = Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
        },
        containerColor = Color.Transparent, // Set containerColor to transparent
        tonalElevation = 0.dp // Remove tonal elevation
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // re-selecting the same item
                            launchSingleTop = true
                            // Restore state when re-selecting a previously selected item
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colorResource(id = R.color.expense_color),
                    unselectedIconColor = colorResource(id = R.color.background_tint_dark),
                    selectedTextColor = colorResource(id = R.color.expense_color),
                    unselectedTextColor = colorResource(id = R.color.background_tint_dark),
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}