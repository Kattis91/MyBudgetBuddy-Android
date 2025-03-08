package com.example.mybudgetbuddy.models

import java.util.UUID

data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val category: String,
    val isfixed: Boolean
)