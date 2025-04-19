package com.kat.mybudgetbuddy.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import com.kat.mybudgetbuddy.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithMenu(
    isDarkMode: Boolean,
    showDropdownMenu: Boolean,
    onDropdownMenuChange: (Boolean) -> Unit,
    onCategoryClick: () -> Unit,
    onInvoiceClick: () -> Unit,
    onAccountDeleteClick: () -> Unit
) {
    TopAppBar(
        title = { },
        actions = {
            IconButton(onClick = { onDropdownMenuChange(true) }) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier.size(30.dp),
                    tint = colorResource(id = R.color.background_tint_dark)
                )
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
                HorizontalDivider()
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = "Invoices",
                                tint = Color(0xFFFF9800),
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
                    onClick = {
                        onInvoiceClick()
                        onDropdownMenuChange(false) }
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = "Delete account",
                                tint = Color(0xFFBF0449),
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .size(30.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Delete Account",
                                color = if (isDarkMode) Color.White else colorResource(id = R.color.text_color),
                                fontSize = 19.sp,
                                modifier = Modifier.padding(end = 30.dp)
                            )
                        }
                    },
                    onClick = {
                        onDropdownMenuChange(false)
                        onAccountDeleteClick()
                    }
                )
            }
        }
    )
}
