package com.example.mybudgetbuddy.utils

import java.text.NumberFormat
import java.util.Locale

fun formatAmount(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.FRANCE).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return formatter.format(amount)
}