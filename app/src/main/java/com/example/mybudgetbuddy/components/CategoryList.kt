package com.example.mybudgetbuddy.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.mybudgetbuddy.R

@Composable
fun CategoryList(
    categories: List<String>,
    onAddCategoryClick: () -> Unit,
    onEditCategoryClick: (String) -> Unit,
    onDeleteCategoryClick: (String) -> Unit
) {
    val isDarkMode = isSystemInDarkTheme()
    val textColor = if (isDarkMode) Color.White else colorResource(id = R.color.text_color)

    val showAlertDialog = remember { mutableStateOf(false) }
    val categoryToDelete = remember { mutableStateOf<String?>(null) }

    val rowReference = remember { mutableStateOf<LayoutCoordinates?>(null) }

    val gradientColors = if (isDarkMode) {
        listOf(Color.DarkGray, Color.Black)
    } else {
        listOf(
            Color(red = 245f/255f, green = 247f/255f, blue = 245f/255f),
            Color(red = 240f/255f, green = 242f/255f, blue = 240f/255f)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(categories) { category ->
                Card(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 5.dp)
                        .shadow(
                            elevation = if (isDarkMode) 2.dp else 1.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = Color.Black.copy(alpha = if (isDarkMode) 0.35f else 0.25f),
                            ambientColor = Color.Black.copy(alpha = if (isDarkMode) 0.35f else 0.25f),
                            clip = false
                        )
                        .graphicsLayer(
                            shadowElevation = 4f,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .offset(x = (3).dp, y = (-3).dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = gradientColors,
                            ),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .border(
                            width = if (isDarkMode) 0.6.dp else 0.8.dp,
                            color = Color.White.copy(alpha = if (isDarkMode) 0.3f else 0.4f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .onGloballyPositioned { coordinates ->
                            rowReference.value = coordinates
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = category, color = textColor)

                        Row {
                            IconButton(onClick = { onEditCategoryClick(category) }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = if (isDarkMode) Color.White else Color.Blue
                                )
                            }
                            IconButton(onClick = {
                                categoryToDelete.value = category
                                showAlertDialog.value = true
                            }) {
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

        if (showAlertDialog.value && categoryToDelete.value != null) {
            CustomAlertDialog(
                show = true,
                onDismiss = {
                    categoryToDelete.value = null
                    showAlertDialog.value = false
                },
                onConfirm = {
                    categoryToDelete.value?.let { category ->
                        onDeleteCategoryClick(category)
                    }
                    categoryToDelete.value = null
                    showAlertDialog.value = false
                },
                message = "Are you sure you want to delete ${categoryToDelete.value}?",
                customColor = colorResource(id = R.color.error_message_color),
                confirmText = "Delete!",
                cancelButtonText = "Go back!",
                onCancel = {
                    showAlertDialog.value = false
                    categoryToDelete.value = null
                },
            )
        }

        // Position the FloatingActionButton at the bottom center of the screen
        FloatingActionButton(
            onClick = onAddCategoryClick,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(26.dp),
            containerColor = if (isDarkMode) Color.DarkGray else colorResource(id = R.color.background_tint_dark),
            contentColor = Color.White
        ) {
            Icon(Icons.Filled.Add, "Add Category")
        }
    }
}