package com.kat.mybudgetbuddy.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun SectionHeader(
    title: String,
    textColor: Color
) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall, // Use a theme-defined style
        modifier = Modifier.padding(vertical = 10.dp),
        color = textColor
    )
}