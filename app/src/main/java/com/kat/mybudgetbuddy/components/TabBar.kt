package com.kat.mybudgetbuddy.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.StackedBarChart
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kat.mybudgetbuddy.R
import com.kat.mybudgetbuddy.TabItem

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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
    ) {
        Surface(
            color = Color(0xFFF5F1FB),
            tonalElevation = 4.dp,
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = currentRoute == item.route
                    if (!isSelected) {
                        NavigationIcon(item, isSelected = false) {
                            navController.navigate(item.route)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp)) // space for FAB
                    }
                }
            }
        }

        val activeIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Bottom
        ) {
            repeat(items.size) { index ->
                if (index == activeIndex) {
                    Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        FloatingActionButton(
                            onClick = { navController.navigate(items[index].route) },
                            containerColor = Color.White,
                            contentColor = colorResource(id = R.color.expense_color),
                            shape = CircleShape,
                            elevation = FloatingActionButtonDefaults.elevation(8.dp),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = items[index].icon,
                                contentDescription = ""
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = items[index].title,
                            color = colorResource(id = R.color.expense_color),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(56.dp))
                }
            }
        }
    }
}
