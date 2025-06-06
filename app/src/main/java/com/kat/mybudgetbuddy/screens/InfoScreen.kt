package com.kat.mybudgetbuddy.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kat.mybudgetbuddy.R
import com.kat.mybudgetbuddy.components.AnimatedSegmentedButtonRow
import com.kat.mybudgetbuddy.components.FeatureCard
import com.kat.mybudgetbuddy.data.createAboutDeveloperList
import com.kat.mybudgetbuddy.data.createExtraFeaturesList
import com.kat.mybudgetbuddy.data.createFeaturesList


@Composable
fun InfoScreen(
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()
    var selectedTabIndex by remember { mutableStateOf(0) }

    val isDarkMode = isSystemInDarkTheme()
    val textColor = if (isDarkMode) Color.White else colorResource(id = R.color.text_color)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "MyBudgetBuddy",
                fontSize = 27.sp,
                color = textColor,
                modifier = Modifier.padding(start = 18.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = colorResource(id = R.color.expense_color),
                    modifier = Modifier.padding(end = 18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.budgeting_made_easy),
            fontSize = 20.sp,
            modifier = Modifier.padding(horizontal = 20.dp),
            color = textColor
        )

        Spacer(modifier = Modifier.height(20.dp))

        AnimatedSegmentedButtonRow (
            options = listOf(stringResource(R.string.about), stringResource(R.string.extra), stringResource(R.string.development)),
            selectedIndex = selectedTabIndex,
            onSelectionChanged = { index ->
                selectedTabIndex = index
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        when (selectedTabIndex) {
            0 -> {
                val features = createFeaturesList()
                features.forEach( { FeatureCard(it) })

            }
            1 -> {
                val extraFeatures = createExtraFeaturesList()
                extraFeatures.forEach { FeatureCard(it) }

            }
            2 -> {
                val aboutTheDeveloper = createAboutDeveloperList()
                aboutTheDeveloper.forEach { FeatureCard(it) }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InfoScreenPreview() {
    InfoScreen(onDismiss = {})
}