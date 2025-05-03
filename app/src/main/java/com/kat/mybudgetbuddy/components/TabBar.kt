package com.kat.mybudgetbuddy.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kat.mybudgetbuddy.R
import com.kat.mybudgetbuddy.TabItem

@Composable
fun TabBar(navController: NavController) {
    val items = listOf(
        TabItem(title = stringResource(R.string.home), Icons.Filled.Home, "home"),
        TabItem(title = stringResource(R.string.incomes), Icons.Filled.AddCircle, "incomes"),
        TabItem(title = stringResource(R.string.expenses), Icons.Filled.RemoveCircle, "expenses"),
        TabItem(title = stringResource(R.string.overview), Icons.Filled.StackedBarChart, "overview")
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    // Check if current route is a period detail page
    val isPeriodDetailRoute = currentRoute?.startsWith("periodDetail/") ?: false

    val isDarkMode = isSystemInDarkTheme()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
    ) {
        // Background surface
        Surface(
            color = if (isDarkMode) Color.Black else Color(0xFFF5F1FB),
            tonalElevation = 4.dp,
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .align(Alignment.BottomCenter)
        ) {}

        // Single row for all tab items - fixed positions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 5.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Bottom
        ) {
            items.forEachIndexed { index, item ->
                // Consider period detail routes as part of the overview tab
                val isSelected = if (item.route == "overview" && isPeriodDetailRoute) {
                    true
                } else {
                    currentRoute == item.route
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.width(56.dp)
                ) {
                    // Tab icon - either floating or regular
                    if (isSelected) {
                        FloatingActionButton(
                            onClick = {
                                if (isPeriodDetailRoute && item.route == "overview") {
                                    navController.popBackStack(item.route, false)
                                }
                            },
                            containerColor = if (isDarkMode) Color.Black.copy(alpha = 0.4f) else Color(0xFFF5F1FB),
                            contentColor = if (isDarkMode) Color.White else colorResource(id = R.color.text_color),
                            shape = CircleShape,
                            elevation = FloatingActionButtonDefaults.elevation(8.dp),
                            modifier = Modifier
                                .size(56.dp)
                                .offset(y = (-10).dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { navController.navigate(item.route) },
                            modifier = Modifier.size(48.dp).padding(top = 10.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = colorResource(id = R.color.background_tint_dark),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Tab text - consistently positioned
                    Text(
                        text = item.title,
                        color = if (isSelected)
                            if (isDarkMode) Color.White else colorResource(id = R.color.text_color)
                        else
                            colorResource(id = R.color.background_tint_dark),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }
            }
        }
    }
}
