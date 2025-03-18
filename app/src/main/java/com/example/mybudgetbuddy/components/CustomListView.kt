package com.example.mybudgetbuddy.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mybudgetbuddy.models.Income
import com.example.mybudgetbuddy.utils.formatAmount

@SuppressLint("DefaultLocale")
@Composable
fun IncomeItem(income: Income) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = income.category)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = formatAmount(income.amount),
                fontWeight = FontWeight.Bold
            )
        }
    }
}