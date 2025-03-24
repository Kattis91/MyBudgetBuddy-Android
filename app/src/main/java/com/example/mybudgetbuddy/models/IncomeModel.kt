package com.example.mybudgetbuddy.models

interface Identifiable {
    val id: String
}

data class Income(
    override val id: String,
    val category: String,
    val amount: Double,
    // other fields
) : Identifiable