package com.example.mybudgetbuddy

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mybudgetbuddy.models.BudgetPeriod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import java.util.UUID

class BudgetManager : ViewModel() {
    // Repository for Firebase operations
    private val repository = BudgetRepository()

    // LiveData for UI
    private val _currentPeriod = MutableLiveData<BudgetPeriod>()
    val currentPeriod: LiveData<BudgetPeriod> get() = _currentPeriod

    private val _historicalPeriods = MutableLiveData<List<BudgetPeriod>>(emptyList())
    val historicalPeriods: LiveData<List<BudgetPeriod>> get() = _historicalPeriods

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    init {
        loadData()
    }

    fun createCleanBudgetPeriod(
        startDate: Date,
        endDate: Date,
        completion: (Boolean) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create new period with empty lists
                val newPeriod = BudgetPeriod(
                    id = UUID.randomUUID().toString(),
                    startDate = startDate,
                    endDate = endDate,
                    incomes = emptyList(),
                    fixedExpenses = emptyList(),
                    variableExpenses = emptyList()
                )

                // Save directly to Firebase
                val success = repository.saveBudgetPeriod(newPeriod)

                // Update UI if successful
                if (success) {
                    withContext(Dispatchers.Main) {
                        _currentPeriod.value = newPeriod
                    }
                }

                // Notify completion
                withContext(Dispatchers.Main) {
                    completion(success)
                }
            } catch (e: Exception) {
                Log.e("BudgetManager", "Error creating clean budget period: ${e.message}")
                withContext(Dispatchers.Main) {
                    completion(false)
                }
            }
        }
    }

    fun loadData() {
        _isLoading.value = true
        loadCurrentBudgetPeriod()
        loadHistoricalPeriods()
    }

    private fun loadCurrentBudgetPeriod() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val budgetPeriod = repository.loadCurrentPeriod()

                if (budgetPeriod == null) {
                    withContext(Dispatchers.Main) {
                        _isLoading.value = false
                    }
                    return@launch
                }

                // Check if period has expired
                val calendar = Calendar.getInstance()
                val today = calendar.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time

                if (budgetPeriod.endDate < today) {
                    // Save as historical and create new
                    repository.saveHistoricalPeriod(budgetPeriod)
                    withContext(Dispatchers.Main) {
                        // initializeDefaultPeriod()
                        _isLoading.value = false
                    }
                    return@launch
                }

                // Update the UI with loaded data
                withContext(Dispatchers.Main) {
                    budgetPeriod.let {
                        _currentPeriod.value = it // Only assign if budgetPeriod is not null
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e("BudgetManager", "Error loading budget period: ${e.message}")
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                }
            }
        }
    }

    private fun loadHistoricalPeriods() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val periods = repository.loadHistoricalPeriods()

                withContext(Dispatchers.Main) {
                    _historicalPeriods.value = periods
                }
            } catch (e: Exception) {
                Log.e("BudgetManager", "Error loading historical periods: ${e.message}")
            }
        }
    }

    fun startNewPeriod(
        startDate: Date,
        endDate: Date
    ) {
        val currentPeriod = _currentPeriod.value

        // Save current period as historical first - fixed to use coroutine
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Save current period if it exists
                if (currentPeriod != null) {
                    repository.saveHistoricalPeriod(currentPeriod)
                }

                // Create new period
                val newPeriod = BudgetPeriod(
                    id = UUID.randomUUID().toString(),
                    startDate = startDate,
                    endDate = endDate,
                    incomes = emptyList(),
                    fixedExpenses = emptyList(),
                    variableExpenses = emptyList(),
                    expired = false
                )

                // Save the new period
                repository.saveBudgetPeriod(newPeriod)

                withContext(Dispatchers.Main) {
                    // Update UI with new period
                    _currentPeriod.value = newPeriod
                }
            } catch (e: Exception) {
                Log.e("BudgetManager", "Error in startNewPeriod: ${e.message}")
            }
        }
    }
}