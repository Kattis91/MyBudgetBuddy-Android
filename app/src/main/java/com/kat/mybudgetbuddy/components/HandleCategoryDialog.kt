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
import androidx.compose.ui.res.stringResource
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
    errorMessageResId: Int? = null,  // Changed to Int? for resource ID
    onErrorMessageChange: (Int?) -> Unit  // Changed to take Int? for resource ID
) {
    val isDarkMode = isSystemInDarkTheme()
    var categoryName by remember { mutableStateOf(if (isEditing && category != null) category.name else "") }

    val titleText = if (isEditing) stringResource(R.string.editing) else stringResource(R.string.adding)
    val categoryTypeText = if (category != null) {
        when (category.type) {
            CategoryType.INCOME -> stringResource(R.string.income_category)
            CategoryType.FIXED_EXPENSE -> stringResource(R.string.fixed_expense_category)
            CategoryType.VARIABLE_EXPENSE -> stringResource(R.string.variable_expense_category)
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
                    label = stringResource(R.string.category_name),
                    icon = Icons.Default.FolderOpen,
                    onChange = {
                        onErrorMessageChange(null)  // Clear error with null instead of empty string
                    }
                )

                Spacer(modifier = Modifier.padding(bottom = 10.dp))

                Box(modifier = Modifier.heightIn(min = 30.dp)
                ) {
                    // Use stringResource to convert the resource ID to a localized string
                    errorMessageResId?.let { resId ->
                        Text(
                            text = stringResource(resId),
                            color = colorResource(id = R.color.error_message_color)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CustomButton(
                        buttonText = stringResource(R.string.save),
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