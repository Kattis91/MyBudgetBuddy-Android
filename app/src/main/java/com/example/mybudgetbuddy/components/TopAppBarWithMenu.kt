package com.example.mybudgetbuddy.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mybudgetbuddy.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithMenu(
    isDarkMode: Boolean,
    showDropdownMenu: Boolean,
    onDropdownMenuChange: (Boolean) -> Unit,
    onCategoryClick: () -> Unit
) {
    TopAppBar(
        title = { },
        actions = {
            IconButton(onClick = { onDropdownMenuChange(true) }) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings")
            }

            DropdownMenu(
                expanded = showDropdownMenu,
                onDismissRequest = { onDropdownMenuChange(false) }
            ) {
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = "Categories",
                                tint = Color(0xFF175FF3),
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .size(30.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Manage Categories",
                                color = if (isDarkMode) Color.White else colorResource(id = R.color.text_color),
                                fontSize = 18.sp,
                                modifier = Modifier.padding(end = 30.dp)
                            )
                        }
                    },
                    onClick = {
                        onCategoryClick()
                        onDropdownMenuChange(false)
                    }
                )
                Divider()
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = "Invoices",
                                tint = Color(0xFFBF0449),
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .size(30.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Invoice Reminders",
                                color = if (isDarkMode) Color.White else colorResource(id = R.color.text_color),
                                fontSize = 19.sp,
                                modifier = Modifier.padding(end = 30.dp)
                            )
                        }
                    },
                    onClick = { onDropdownMenuChange(false) }
                )
            }
        }
    )
}
