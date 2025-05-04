package com.kat.mybudgetbuddy.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kat.mybudgetbuddy.R
import com.kat.mybudgetbuddy.budget.BudgetManager
import com.kat.mybudgetbuddy.models.BudgetPeriod
import com.kat.mybudgetbuddy.utils.formatAmount
import com.kat.mybudgetbuddy.utils.formattedDateRange
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodRowView(
    period: BudgetPeriod,
    navController: NavController,
    textColor: Color,
    viewModel: BudgetManager = viewModel()
) {
    val outcome = period.totalIncome - (period.totalFixedExpenses + period.totalVariableExpenses)
    val isNegative: Boolean = outcome < 0
    val isDarkMode = isSystemInDarkTheme()

    val coroutineScope = rememberCoroutineScope()

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
                // Just delegate to the viewModel and don't maintain local state
                coroutineScope.launch {
                    // Let animation complete before actually deleting
                    delay(300)
                    viewModel.deleteHistoricalPeriod(period.id)
                }
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
                    .padding(vertical = 6.dp)
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
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("periodDetail/${period.id}")
                    },
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            formattedDateRange(period.startDate, period.endDate),
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            if (isNegative) {
                                stringResource(R.string.outcome_negative, formatAmount(outcome))
                            } else {
                                stringResource(R.string.outcome_positive, formatAmount(outcome))
                            },
                            color = textColor
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Column(modifier = Modifier.padding(end = 5.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Right arrow"
                        )
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PeriodRowViewPreview() {
    val previewNavController = rememberNavController()
    PeriodRowView(
        navController = previewNavController,
        period = BudgetPeriod(startDate = Date(), endDate = Date()),
        textColor = Color.Blue)
}


