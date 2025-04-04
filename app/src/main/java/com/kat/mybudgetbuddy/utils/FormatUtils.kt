package com.kat.mybudgetbuddy.utils

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

fun formatAmount(amount: Double?, showNegative: Boolean = false): String {
    val formatter = NumberFormat.getNumberInstance(Locale.FRANCE).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    val absAmount = abs(amount ?: 0.0)
    return if (showNegative) "- ${formatter.format(absAmount)}" else formatter.format(absAmount)
}
