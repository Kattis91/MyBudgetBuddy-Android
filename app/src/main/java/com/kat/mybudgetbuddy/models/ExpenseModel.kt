package com.kat.mybudgetbuddy.models

data class Expense(
    override val id: String,
    val amount: Double,
    val category: String,
    val isfixed: Boolean
    
) : Identifiable