package com.kat.mybudgetbuddy.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kat.mybudgetbuddy.R
import com.kat.mybudgetbuddy.models.Category
import com.kat.mybudgetbuddy.models.CategoryType

@Composable
fun HandleCategoryDialog(
    onDismiss: () -> Unit,
    onSaveCategory: (String) -> Unit,
    isEditing: Boolean = false,
    category: Category? = null,
    errorMessage: String = "",
    onErrorMessageChange: (String) -> Unit
) {
    val isDarkMode = isSystemInDarkTheme()
    var categoryName by remember { mutableStateOf(if (isEditing && category != null) category.name else "") }

    val titleText = if (isEditing) "Editing" else "Adding"
    val categoryTypeText = if (category != null) {
        when (category.type) {
            CategoryType.INCOME -> "Income Category"
            CategoryType.FIXED_EXPENSE -> "Fixed Expense Category"
            CategoryType.VARIABLE_EXPENSE -> "Variable Expense Category"
        }
    } else {
        ""
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .padding(horizontal = 18.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row {
                    Spacer(Modifier.weight(1f))
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colorResource(id = R.color.expense_color),
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 5.dp),
                        color = if (isDarkMode) Color.White else colorResource(id = R.color.text_color)
                    )

                    if (categoryTypeText.isNotEmpty()) {
                        Text(
                            text = categoryTypeText,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isDarkMode) Color.White else colorResource(id = R.color.text_color),
                            modifier = Modifier.padding(bottom = 10.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.padding(bottom = 10.dp))

                CustomTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = "Category Name",
                    icon = Icons.Default.FolderOpen,
                    onChange = {
                        onErrorMessageChange("")
                    }
                )

                Spacer(modifier = Modifier.padding(bottom = 10.dp))

                Box(modifier = Modifier.heightIn(min = 30.dp)
                ) {
                    if (errorMessage.isNotEmpty()) {
                        Text(text = errorMessage,
                            color = colorResource(id = R.color.error_message_color)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CustomButton(
                        buttonText = "Save",
                        onClick = {
                            onSaveCategory(categoryName)
                        },
                        isIncome = true,
                        isExpense = false,
                        isThirdButton = false
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HandleCategoryDialogPreview() {
    HandleCategoryDialog(
        onDismiss = {},
        onSaveCategory = {},
        onErrorMessageChange = {}
    )
}