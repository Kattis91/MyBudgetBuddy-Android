package com.example.mybudgetbuddy.models

import java.util.UUID

data class Income(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val category: String
)