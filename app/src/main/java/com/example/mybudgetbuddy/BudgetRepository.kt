package com.example.mybudgetbuddy

import android.util.Log
import com.example.mybudgetbuddy.models.BudgetPeriod
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.Calendar
import java.util.Date
import java.util.UUID

class BudgetRepository {

    suspend fun loadCurrentPeriod(): BudgetPeriod? {
        val userId = Firebase.auth.currentUser?.uid ?: run {
            Log.e("BudgetRepository", "Failed to load budget period: No user ID")
            return null
        }

        try {
            val database = Firebase.database
            val budgetPeriodsRef = database.reference.child("budgetPeriods").child(userId)

            Log.d("BudgetRepository", "Attempting to load current period for user: $userId")

            val snapshot = try {
                withTimeout(5000) {
                    budgetPeriodsRef.orderByChild("startDate").limitToLast(1).get().await()
                }
            } catch (e: TimeoutCancellationException) {
                Log.e("BudgetRepository", "Query timed out", e)
                return null
            }

            // Add more debug logging
            Log.d("BudgetRepository", "Query complete. Snapshot exists: ${snapshot.exists()}, child count: ${snapshot.childrenCount}")

            if (!snapshot.exists() || snapshot.childrenCount <= 0) {
                Log.d("BudgetRepository", "No current period found in database")
                return null
            }

            // Get the period data
            val periodData = snapshot.children.iterator().next()
            Log.d("BudgetRepository", "Period key: ${periodData.key}, has value: ${periodData.value != null}")

            val dict = periodData.value as? Map<String, Any>
            if (dict == null) {
                Log.e("BudgetRepository", "Failed to cast period data to Map")
                return null
            }

            // Debug the dictionary content
            Log.d("BudgetRepository", "Period dict keys: ${dict.keys.joinToString()}")

            val budgetPeriod = BudgetPeriod.fromDict(dict)?.copy(id = periodData.key ?: "")
            if (budgetPeriod == null) {
                Log.e("BudgetRepository", "Failed to parse budget period from dict")
                return null
            }

            // Check if period has expired
            val calendar = Calendar.getInstance()
            val today = calendar.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val periodEndDate = calendar.apply {
                time = budgetPeriod.endDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            if (periodEndDate.before(today)) {
                // Period has expired, save as historical and remove
                Log.d("BudgetRepository", "Period expired: end date $periodEndDate is before today $today")
                saveHistoricalPeriod(budgetPeriod)
                periodData.ref.removeValue().await()
                return null
            }

            Log.d("BudgetRepository", "Successfully loaded current period: ${budgetPeriod.id}")
            return budgetPeriod
        } catch (e: Exception) {
            Log.e("BudgetRepository", "Error loading budget period", e)
            return null
        }
    }

    suspend fun loadHistoricalPeriods(): List<BudgetPeriod> {
        val userId = Firebase.auth.currentUser?.uid ?: return emptyList()

        try {
            val database = Firebase.database
            val historicalRef = database.reference.child("historicalPeriods").child(userId)

            val snapshot = historicalRef.orderByChild("becameHistoricalDate").get().await()

            val periods = mutableListOf<BudgetPeriod>()

            for (periodSnapshot in snapshot.children) {
                val dict = periodSnapshot.value as? Map<String, Any> ?: continue
                val period = BudgetPeriod.fromDict(dict)?.copy(id = periodSnapshot.key ?: "")
                if (period != null) {
                    periods.add(period)
                }
            }

            return periods
        } catch (e: Exception) {
            Log.e("BudgetRepository", "Error loading historical periods: ${e.message}")
            return emptyList()
        }
    }

    suspend fun saveBudgetPeriod(
        budgetPeriod: BudgetPeriod,
        transferData: Pair<Boolean, Boolean>, // (incomes, expenses)
        isFixed: Boolean = false
    ): Boolean {
        val userId = Firebase.auth.currentUser?.uid ?: run {
            Log.e("BudgetRepository", "Failed to save budget period: No user ID")
            return false
        }

        try {
            val database = Firebase.database
            val ref = database.reference
            val budgetPeriodsRef = ref.child("budgetPeriods").child(userId)
            val newBudgetRef = budgetPeriodsRef.child(budgetPeriod.id)

            // First, get and save the current period as historical
            val currentPeriod = loadCurrentPeriod()

            newBudgetRef.setValue(budgetPeriod.toDictionary()).await()

            // If we're not transferring any data, complete here
            if ((!transferData.first && !transferData.second) || currentPeriod == null) {
                currentPeriod?.let {
                    // Only after saving the new period, save the old one as historical
                    saveHistoricalPeriod(it)
                }
                return true
            }

            // Transfer data from current period
            val currentRef = budgetPeriodsRef.child(currentPeriod.id)

            // Transfer incomes if requested
            if (transferData.first) {
                try {
                    val incomesSnapshot = currentRef.child("incomes").get().await()
                    incomesSnapshot.value?.let { incomes ->
                        newBudgetRef.child("incomes").setValue(incomes).await()
                    }
                } catch (e: Exception) {
                    Log.e("BudgetRepository", "Error transferring incomes: ${e.message}")
                }
            }

            // Transfer expenses if requested
            if (transferData.second) {
                try {
                    val expenseType = if (isFixed) "fixedExpenses" else "variableExpenses"
                    val expensesSnapshot = currentRef.child(expenseType).get().await()
                    expensesSnapshot.value?.let { expenses ->
                        newBudgetRef.child(expenseType).setValue(expenses).await()
                    }
                } catch (e: Exception) {
                    Log.e("BudgetRepository", "Error transferring expenses: ${e.message}")
                }
            }

            return true
        } catch (e: Exception) {
            Log.e("BudgetRepository", "Error saving budget period: ${e.message}")
            return false
        }
    }

    fun createCleanBudgetPeriod(
        startDate: Date,
        endDate: Date,
        completion: (Boolean) -> Unit
    ) {
        val userId = Firebase.auth.currentUser?.uid ?: run {
            Log.e("BudgetRepository", "Failed to create budget period: No user ID")
            completion(false)
            return
        }

        val database = Firebase.database
        val budgetId = UUID.randomUUID().toString()
        val budgetRef = database.reference.child("budgetPeriods").child(userId).child(budgetId)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create new period with empty lists
                val newPeriod = BudgetPeriod(
                    id = budgetId,
                    startDate = startDate,
                    endDate = endDate,
                    incomes = emptyList(),
                    fixedExpenses = emptyList(),
                    variableExpenses = emptyList(),
                    expired = false
                )

                // Save directly to Firebase
                budgetRef.setValue(newPeriod.toDictionary()).await()

                // Notify success on the main thread
                withContext(Dispatchers.Main) {
                    completion(true)
                }
            } catch (e: Exception) {
                Log.e("BudgetRepository", "Error creating clean budget period: ${e.message}")
                withContext(Dispatchers.Main) {
                    completion(false)
                }
            }
        }
    }

    suspend fun saveHistoricalPeriod(budgetPeriod: BudgetPeriod): Boolean {
        return try {
            val userId = Firebase.auth.currentUser?.uid ?: run {
                Log.e("BudgetRepository", "Failed to save historical period: No user ID")
                return false
            }

            val database = Firebase.database

            // First add to historical node
            val historicalRef = database.reference
                .child("historicalPeriods")
                .child(userId)
                .child(budgetPeriod.id)

            historicalRef.setValue(budgetPeriod.toDictionary()).await()
            Log.d("BudgetRepository", "Successfully saved period ${budgetPeriod.id} to historical node")

            // Then remove from active periods
            val activeRef = database.reference
                .child("budgetPeriods")
                .child(userId)
                .child(budgetPeriod.id)

            activeRef.removeValue().await()
            Log.d("BudgetRepository", "Successfully removed period ${budgetPeriod.id} from active node")

            true
        } catch (e: Exception) {
            Log.e("BudgetRepository", "Error saving historical period: ${e.message}")
            false
        }
    }
}