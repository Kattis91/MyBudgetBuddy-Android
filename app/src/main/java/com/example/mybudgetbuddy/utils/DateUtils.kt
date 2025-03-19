package com.example.mybudgetbuddy.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun formattedDateRange(startDate: Date, endDate: Date): String {
    val formatter = SimpleDateFormat("d MMM yyyy", Locale.ENGLISH)
    val calendar = Calendar.getInstance()

    calendar.time = startDate
    val startYear = calendar.get(Calendar.YEAR)

    calendar.time = endDate
    val endYear = calendar.get(Calendar.YEAR)

    return if (startYear == endYear) {
        val shortFormatter = SimpleDateFormat("d MMM", Locale.ENGLISH)
        "${shortFormatter.format(startDate)} - ${shortFormatter.format(endDate)} $startYear"
    } else {
        "${formatter.format(startDate)} - ${formatter.format(endDate)}"
    }
}