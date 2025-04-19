package com.kat.mybudgetbuddy.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kat.mybudgetbuddy.R
import com.kat.mybudgetbuddy.models.Identifiable
import com.kat.mybudgetbuddy.utils.formatAmount
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

            val rowReference = remember { mutableStateOf<LayoutCoordinates?>(null) }

            val gradientColors = if (isDarkMode) {
                listOf(Color.DarkGray, Color.Black)
            } else {
                listOf(
                    Color(red = 245f/255f, green = 247f/255f, blue = 245f/255f),
                    Color(red = 240f/255f, green = 242f/255f, blue = 240f/255f)
                )
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
                            .padding(vertical = if (isInvoice) 1.dp else 6.dp)
                            .padding(horizontal = 21.dp)
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
                        width = 235,
                        height = 70
                    )
                }
            }
        }
    }
}