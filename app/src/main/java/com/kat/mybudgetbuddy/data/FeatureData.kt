package com.kat.mybudgetbuddy.data

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kat.mybudgetbuddy.R
import com.kat.mybudgetbuddy.components.CreditsText

data class FeatureItem(
    val icon: ImageVector,
    val text: String? = null,
    val content: (@Composable () -> Unit)? = null
)

@Composable
fun createFeaturesList(): List<FeatureItem> {
    return listOf(
        FeatureItem(
            Icons.Default.FormatListNumbered,
            text = stringResource(R.string.add_your_income_and_expenses)
        ),
        FeatureItem(
            Icons.Default.Category,
            text = stringResource(R.string.organize_your_finances)
        ),
        FeatureItem(
            Icons.Default.CalendarMonth,
            text = stringResource(R.string.customize_budget_periods)
        ),
        FeatureItem(
            Icons.Default.BarChart,
            text = stringResource(R.string.keep_track_of_your_entire_budget)
        )
    )
}

@Composable
fun createExtraFeaturesList(): List<FeatureItem> {
    return listOf(
        FeatureItem(
            Icons.Default.AccessTime,
            text = stringResource(R.string.stay_in_control)
        ),
        FeatureItem(
            Icons.Default.Alarm,
            text = stringResource(R.string.never_miss_a_payment)
        )
    )
}

@Composable
fun createAboutDeveloperList(): List<FeatureItem> {
    return listOf(
        FeatureItem(
            Icons.Default.Computer,
            text = stringResource(R.string.about_developer)
        ),
        FeatureItem(
            Icons.Default.Star, content = {
            Text(
                text = stringResource(R.string.the_icon_is_taken_from),
                modifier = Modifier.padding(horizontal = 6.dp)
            )
            CreditsText()
        })
    )
}