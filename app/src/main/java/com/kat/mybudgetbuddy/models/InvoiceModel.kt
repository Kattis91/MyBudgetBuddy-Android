package com.kat.mybudgetbuddy.models

import java.util.Date

data class Invoice(
    override val id: String,
    val title: String,
    val amount: Double,
    var processed: Boolean,
    val expiryDate: Date
) : Identifiable
