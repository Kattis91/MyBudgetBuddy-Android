package com.example.mybudgetbuddy

import android.util.Log
import com.example.mybudgetbuddy.models.BudgetPeriod
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

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

            val snapshot = budgetPeriodsRef.orderByChild("startDate").limitToLast(1).get().await()

            Log.d("BudgetRepository", "Snapshot exists: ${snapshot.exists()}, child count: ${snapshot.childrenCount}")

            if (!snapshot.exists() || snapshot.childrenCount <= 0) {
                Log.d("BudgetRepository", "No current period found in database")
                return null
            }

            val periodData = snapshot.children.iterator().next()
            val dict = periodData.value as? Map<String, Any> ?: return null

            return BudgetPeriod.fromDict(dict)?.copy(id = periodData.key ?: "")
        } catch (e: Exception) {
            Log.e("BudgetRepository", "Error loading budget period: ${e.message}")
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

    suspend fun saveBudgetPeriod(budgetPeriod: BudgetPeriod): Boolean {
        val userId = Firebase.auth.currentUser?.uid ?: run {
            Log.e("BudgetRepository", "Failed to save budget period: No user ID")
            return false
        }

        try {
            val database = Firebase.database
            val budgetRef = database.reference.child("budgetPeriods").child(userId).child(budgetPeriod.id)

            // Save the period
            budgetRef.setValue(budgetPeriod.toDictionary()).await()
            return true
        } catch (e: Exception) {
            Log.e("BudgetRepository", "Error saving budget period: ${e.message}")
            return false
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