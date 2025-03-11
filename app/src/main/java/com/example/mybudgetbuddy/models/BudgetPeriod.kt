package com.example.mybudgetbuddy.models

import android.util.Log
import java.util.Date
import java.util.UUID

data class BudgetPeriod(
    var id: String = UUID.randomUUID().toString(),
    val startDate: Date,
    val endDate: Date,
    val incomes: List<Income> = emptyList(),
    val fixedExpenses: List<Expense> = emptyList(),
    val variableExpenses: List<Expense> = emptyList(),
    val totalIncome: Double = incomes.sumOf { it.amount },
    val totalFixedExpenses: Double = fixedExpenses.sumOf { it.amount },
    val totalVariableExpenses: Double = variableExpenses.sumOf { it.amount },
    val expired: Boolean = false,
    val becameHistoricalDate: Date = Date()
) {
    // Firebase conversion methods
    fun toDictionary(): Map<String, Any> {
        val dict = mutableMapOf<String, Any>(
            "id" to id,
            "startDate" to startDate.time / 1000,  // Convert to seconds
            "endDate" to endDate.time / 1000,
            "becameHistoricalDate" to becameHistoricalDate.time / 1000,
            "totalIncome" to totalIncome,
            "totalFixedExpenses" to totalFixedExpenses,
            "totalVariableExpenses" to totalVariableExpenses,
            "expired" to expired
        )

        // Only include arrays if they're not empty
        if (incomes.isNotEmpty()) {
            dict["incomes"] = incomes.map { income ->
                mapOf(
                    "id" to income.id,
                    "amount" to income.amount,
                    "category" to income.category
                )
            }
        }

        if (fixedExpenses.isNotEmpty()) {
            dict["fixedExpenses"] = fixedExpenses.map { expense ->
                mapOf(
                    "id" to expense.id,
                    "amount" to expense.amount,
                    "category" to expense.category,
                    "isfixed" to expense.isfixed
                )
            }
        }

        if (variableExpenses.isNotEmpty()) {
            dict["variableExpenses"] = variableExpenses.map { expense ->
                mapOf(
                    "id" to expense.id,
                    "amount" to expense.amount,
                    "category" to expense.category,
                    "isfixed" to expense.isfixed
                )
            }
        }
        return dict
    }

    companion object {
        fun fromDict(dict: Map<String, Any>): BudgetPeriod? {
            try {
                Log.d("BudgetPeriod", "Parsing dictionary: ${dict.keys.joinToString()}")

                // Use Number to handle both Long and Double
                val startDateRaw = dict["startDate"] as? Number
                if (startDateRaw == null) {
                    Log.e("BudgetPeriod", "Missing startDate")
                    return null
                }

                val endDateRaw = dict["endDate"] as? Number
                if (endDateRaw == null) {
                    Log.e("BudgetPeriod", "Missing endDate")
                    return null
                }

                // Convert to Double (seconds since epoch)
                val startDateTimestamp = startDateRaw.toDouble()
                val endDateTimestamp = endDateRaw.toDouble()

                // Convert timestamps to Date objects
                val startDate = Date((startDateTimestamp * 1000).toLong())
                val endDate = Date((endDateTimestamp * 1000).toLong())

                // Get historical date or use current
                val historicalDateRaw = dict["becameHistoricalDate"] as? Number
                val becameHistoricalDate = if (historicalDateRaw != null) {
                    Date((historicalDateRaw.toDouble() * 1000).toLong())
                } else {
                    Date()
                }

                // Get ID or create new
                val id = dict["id"] as? String ?: UUID.randomUUID().toString()

                // Process incomes if present
                val incomes = mutableListOf<Income>()
                (dict["incomes"] as? List<*>)?.forEach { incomeObj ->
                    (incomeObj as? Map<*, *>)?.let { incomeDict ->
                        val category = incomeDict["category"] as? String ?: return@let
                        val amount = (incomeDict["amount"] as? Number)?.toDouble() ?: return@let

                        incomes.add(
                            Income(
                                id = incomeDict["id"] as? String ?: UUID.randomUUID().toString(),
                                amount = amount,
                                category = category
                            )
                        )
                    }
                }

                // Process fixed expenses if present
                val fixedExpenses = mutableListOf<Expense>()
                (dict["fixedExpenses"] as? List<*>)?.forEach { expenseObj ->
                    (expenseObj as? Map<*, *>)?.let { expenseDict ->
                        val category = expenseDict["category"] as? String ?: return@let
                        val amount = (expenseDict["amount"] as? Number)?.toDouble() ?: return@let

                        fixedExpenses.add(
                            Expense(
                                id = expenseDict["id"] as? String ?: UUID.randomUUID().toString(),
                                amount = amount,
                                category = category,
                                isfixed = true
                            )
                        )
                    }
                }

                // Process variable expenses if present
                val variableExpenses = mutableListOf<Expense>()
                (dict["variableExpenses"] as? List<*>)?.forEach { expenseObj ->
                    (expenseObj as? Map<*, *>)?.let { expenseDict ->
                        val category = expenseDict["category"] as? String ?: return@let
                        val amount = (expenseDict["amount"] as? Number)?.toDouble() ?: return@let

                        variableExpenses.add(
                            Expense(
                                id = expenseDict["id"] as? String ?: UUID.randomUUID().toString(),
                                amount = amount,
                                category = category,
                                isfixed = false
                            )
                        )
                    }
                }

                // Get or calculate totals
                val totalIncome = (dict["totalIncome"] as? Number)?.toDouble() ?: incomes.sumOf { it.amount }
                val totalFixedExpenses = (dict["totalFixedExpenses"] as? Number)?.toDouble() ?: fixedExpenses.sumOf { it.amount }
                val totalVariableExpenses = (dict["totalVariableExpenses"] as? Number)?.toDouble() ?: variableExpenses.sumOf { it.amount }
                val expired = dict["expired"] as? Boolean ?: false

                return BudgetPeriod(
                    id = id,
                    startDate = startDate,
                    endDate = endDate,
                    incomes = incomes,
                    fixedExpenses = fixedExpenses,
                    variableExpenses = variableExpenses,
                    totalIncome = totalIncome,
                    totalFixedExpenses = totalFixedExpenses,
                    totalVariableExpenses = totalVariableExpenses,
                    expired = expired,
                    becameHistoricalDate = becameHistoricalDate
                )
            } catch (e: Exception) {
                Log.e("BudgetPeriod", "Error parsing dictionary: ")
                return null
            }
        }
    }
}