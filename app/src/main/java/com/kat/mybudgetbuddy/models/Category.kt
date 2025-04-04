package com.kat.mybudgetbuddy.models

import java.util.UUID

data class Category(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: CategoryType
)