package com.example.mybudgetbuddy.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CategoryMenu(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    showNewCategoryField: Boolean,
    onShowNewCategoryFieldChange: (Boolean) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var newCategory by remember { mutableStateOf("") }

    Card(modifier = Modifier
        .padding(vertical = 15.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        if (showNewCategoryField) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = newCategory,
                    onValueChange = { newCategory = it },
                    label = { Text("New category") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.AddCircle,
                            contentDescription = "Tag",
                            modifier = Modifier.padding(start = 33.dp)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    onShowNewCategoryFieldChange(false)
                    onCategorySelected("")
                    newCategory = ""
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Blue,
                        modifier = Modifier.padding(end = 33.dp, bottom = 8.dp)
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedCategory.isEmpty()) "Choose Category" else selectedCategory
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Dropdown",
                    tint = Color.Gray
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            onCategorySelected(category)
                            expanded = false
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Add new category") },
                    onClick = {
                        onCategorySelected("new")
                        onShowNewCategoryFieldChange(true)
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add new category",
                            modifier = Modifier.width(24.dp)
                        )
                    }
                )
            }
        }
    }
}