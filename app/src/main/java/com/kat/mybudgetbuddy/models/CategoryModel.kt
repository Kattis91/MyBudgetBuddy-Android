package com.kat.mybudgetbuddy.models

enum class CategoryType (val value: String) {
    INCOME("incomeCategories"),
    FIXED_EXPENSE("fixedExpenseCategories"),
    VARIABLE_EXPENSE("variableExpenseCategories")
}

val CategoryType.defaultCategories: List<String>
    get() = when (this) {
        CategoryType.INCOME -> listOf("Salary", "Study grant", "Child benefit", "Housing insurance", "Sickness insurance", "Business")
        CategoryType.FIXED_EXPENSE -> listOf("Rent", "Water", "Heat", "Electricity", "Insurance", "WiFi")
        CategoryType.VARIABLE_EXPENSE -> listOf("Groceries", "Dining Out", "Shopping", "Entertainment", "Transport", "Savings")
    }
