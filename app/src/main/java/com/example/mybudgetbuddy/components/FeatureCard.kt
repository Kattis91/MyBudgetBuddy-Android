package com.example.mybudgetbuddy.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.mybudgetbuddy.R
import com.example.mybudgetbuddy.data.FeatureItem

@Composable
fun FeatureCard(feature: FeatureItem) {
    val isDarkMode = isSystemInDarkTheme()
    val textColor = if (isDarkMode) Color.White else colorResource(id = R.color.text_color)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ){
        Row (
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = "",
                tint = colorResource(id = R.color.expense_color)
            )
            Text(
                text = feature.text,
                modifier = Modifier.padding(16.dp),
                color = textColor
            )
        }
    }
}