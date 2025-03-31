package com.example.mybudgetbuddy.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mybudgetbuddy.R
import com.example.mybudgetbuddy.models.Identifiable
import com.example.mybudgetbuddy.utils.formatAmount
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Identifiable> CustomListView(
    items: List<T>,
    deleteAction: (T) -> Unit,
    itemContent: (T) -> Triple<String, Double?, Date?>,
    showNegativeAmount: Boolean,
    alignAmountInMiddle: Boolean,
    isInvoice: Boolean,
    onMarkAsProcessed: ((T) -> Unit)? = null
) {
    val isDarkMode = isSystemInDarkTheme()
    val textColor = if (isDarkMode) Color.White else colorResource(id = R.color.text_color)

    LazyColumn {
        items(
            count = items.size,
            key = { items[it].id }
        ) { index ->
            val item = items[index]
            val (category, amount, date) = itemContent(item)
            val dateFormatter: DateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            fun formatDate(date: Date?): String {
                return date?.let { dateFormatter.format(it) } ?: ""
            }

            val swipeState = rememberSwipeToDismissBoxState(
                confirmValueChange = { dismissValue ->
                    if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                        deleteAction(item)
                        true
                    } else {
                        false
                    }
                },
                positionalThreshold = { it * 0.7f }
            )

            SwipeToDismissBox(
                state = swipeState,
                enableDismissFromStartToEnd = false,
                backgroundContent = {},
                content = {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .padding(horizontal = 18.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = category, color = textColor)

                            if (date != null) {
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = if (showNegativeAmount) "- ${formatAmount(amount)}" else formatAmount(amount),
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(formatDate(date))
                            } else {
                                if (alignAmountInMiddle) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = if (showNegativeAmount) "- ${formatAmount(amount)}" else formatAmount(amount),
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = if (showNegativeAmount) "- ${formatAmount(amount)}" else formatAmount(amount),
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }
                }
            )

            if(isInvoice) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CustomButton(
                        buttonText = "Mark as processed",
                        onClick = {
                            if (onMarkAsProcessed != null) {
                                onMarkAsProcessed(item)
                            }
                        },
                        isIncome = true,
                        isExpense = false,
                        isThirdButton = false,
                        width = 230
                    )
                }
            }
        }
    }
}