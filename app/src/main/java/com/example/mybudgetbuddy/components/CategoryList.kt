package com.example.mybudgetbuddy.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.mybudgetbuddy.R

@Composable
fun CategoryList(
    categories: List<String>,
    onAddCategoryClick: () -> Unit
) {
    val isDarkMode = isSystemInDarkTheme()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(categories) { category ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 26.dp)
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = category)

                        Row {
                            IconButton(onClick = { /*TODO*/ }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = if (isDarkMode) Color.White else Color.Blue
                                )
                            }
                            IconButton(onClick = { /*TODO*/ }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = colorResource(id = R.color.error_message_color)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Position the FloatingActionButton at the bottom center of the screen
        FloatingActionButton(
            onClick = onAddCategoryClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(26.dp),
            containerColor = if (isDarkMode) Color.DarkGray else colorResource(id = R.color.background_tint_dark),
            contentColor = Color.White
        ) {
            Icon(Icons.Filled.Add, "Add Category")
        }
    }
}
