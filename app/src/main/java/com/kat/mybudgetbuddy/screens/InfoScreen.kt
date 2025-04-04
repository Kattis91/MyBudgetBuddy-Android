package com.kat.mybudgetbuddy.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kat.mybudgetbuddy.R
import com.kat.mybudgetbuddy.components.FeatureCard
import com.kat.mybudgetbuddy.components.SectionHeader
import com.kat.mybudgetbuddy.components.SegmentedButtonRow
import com.kat.mybudgetbuddy.data.aboutTheDeveloper
import com.kat.mybudgetbuddy.data.extraFeatures
import com.kat.mybudgetbuddy.data.features

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
            "Budgeting made easy, so you can focus on what matters most to you!",
            fontSize = 20.sp,
            modifier = Modifier.padding(horizontal = 20.dp),
            color = textColor
        )

        Spacer(modifier = Modifier.height(20.dp))

        SegmentedButtonRow(
            options = listOf("About", "Extra", "Development"),
            selectedIndex = selectedTabIndex,
            onSelectionChanged = { index ->
                selectedTabIndex = index
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        when (selectedTabIndex) {
            0 -> {
                SectionHeader(title = "How it works", textColor = textColor)
                features.forEach { FeatureCard(it) }

            }
            1 -> {
                SectionHeader(title = "Extra Features", textColor = textColor)
                extraFeatures.forEach { FeatureCard(it) }

            }
            2 -> {
                SectionHeader(title = "The Developer", textColor = textColor)
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