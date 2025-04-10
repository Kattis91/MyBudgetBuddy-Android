package com.kat.mybudgetbuddy.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.kat.mybudgetbuddy.R
import com.kat.mybudgetbuddy.TabItem

@Composable
fun NavigationIcon(item: TabItem, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = "Icon Label",
            tint = if (isSelected) Color(0xFF8A3FFC) else colorResource(id = R.color.background_tint_dark),
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = item.title,
            color = if (isSelected) Color(0xFF8A3FFC) else colorResource(id = R.color.background_tint_dark),
            style = MaterialTheme.typography.labelSmall
        )
    }
}