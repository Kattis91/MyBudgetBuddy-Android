package com.example.mybudgetbuddy

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetbuddy.models.BudgetPeriod
import com.example.mybudgetbuddy.models.Expense
import com.example.mybudgetbuddy.models.Income
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private val _incomeList = mutableListOf<Income>()
    private val _fixedExpenseList = mutableListOf<Expense>()
    private val _variableExpenseList = mutableListOf<Expense>()

    private val _groupedIncome = MutableLiveData<Map<String, Double>>(emptyMap())
    private val _groupedExpense = MutableLiveData<Map<String, Double>>(emptyMap())
    private val _totalIncome = MutableLiveData(0.0)
    private val _totalExpenses = MutableLiveData(0.0)

    private val _incomeItemsFlow = MutableStateFlow<List<Income>>(emptyList())
    val incomeItems: StateFlow<List<Income>> = _incomeItemsFlow.asStateFlow()

    private val _totalIncomeFlow = MutableStateFlow(0.0)
    val totalIncome: StateFlow<Double> = _totalIncomeFlow.asStateFlow()

    init {
        loadData()
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
                val (incomeList, groupedIncome, totalIncome) = repository.loadIncomeData()

                // Update the UI with loaded data
                withContext(Dispatchers.Main) {
                    _incomeItemsFlow.value = incomeList
                    _totalIncomeFlow.value = totalIncome
                    budgetPeriod.let {
                        _currentPeriod.value = it // Only assign if budgetPeriod is not null

                        // Update in-memory lists
                        _incomeList.clear()
                        if (it != null) {
                            _incomeList.addAll(it.incomes)
                        }

                        _fixedExpenseList.clear()
                        if (it != null) {
                            _fixedExpenseList.addAll(it.fixedExpenses)
                        }

                        _variableExpenseList.clear()
                        it?.let { it1 -> _variableExpenseList.addAll(it1.variableExpenses) }

                        updateGroupedData()
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
        endDate: Date,
        includeIncomes : Boolean = false,
        includeFixedExpenses : Boolean = false,
    ) {

        val currentPeriod = _currentPeriod.value

        val transferredIncomes = if (includeIncomes) {
            _incomeList.map { income ->
                Income(
                    id = UUID.randomUUID().toString(),
                    amount = income.amount,
                    category = income.category
                )
            }
        } else emptyList()

        val transferredFixedExpenses = if (includeFixedExpenses) {
            _fixedExpenseList.map { expense ->
                Expense(
                    id = UUID.randomUUID().toString(),
                    amount = expense.amount,
                    category = expense.category,
                    isfixed = true
                )
            }
        } else emptyList()

        // Create new period
        val newPeriod = BudgetPeriod(
            id = UUID.randomUUID().toString(),
            startDate = startDate,
            endDate = endDate,
            incomes = transferredIncomes,
            fixedExpenses = transferredFixedExpenses,
            variableExpenses = emptyList(),
            expired = false
        )

        _currentPeriod.value = newPeriod

        _incomeList.clear()
        _incomeList.addAll(transferredIncomes)

        _fixedExpenseList.clear()
        _fixedExpenseList.addAll(transferredFixedExpenses)

        _variableExpenseList.clear()

        updateGroupedData()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.saveBudgetPeriod(
                    newPeriod,
                    Pair(includeIncomes, includeFixedExpenses)
                )
            } catch (e: Exception) {
                Log.e("BudgetManager", "Error saving new period: ${e.message}")
            }
        }
    }

    private fun updateGroupedData() {

        // Update income grouping and total
        val incomesGrouped = _incomeList.groupBy { it.category }
            .mapValues { (_, incomes) ->
                incomes.sumOf { it.amount }
            }
        _groupedIncome.value = incomesGrouped
        _totalIncome.value = _incomeList.sumOf { it.amount }

        // Update expense grouping and total
        val allExpenses = _fixedExpenseList + _variableExpenseList
        val expensesGrouped = allExpenses.groupBy { it.category }
            .mapValues { (_, expenses) ->
                expenses.sumOf { it.amount }
            }
        _groupedExpense.value = expensesGrouped
        _totalExpenses.value = allExpenses.sumOf { it.amount }
    }

    fun addIncome(amount: Double, category: String) {
        if (amount <= 0 || category.isEmpty()) {
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                repository.saveIncomeData(amount, category) {
                    // This runs when the income has been successfully saved
                    loadCurrentBudgetPeriod()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    // In BudgetManager.kt
    fun deleteIncomeItem(income: Income) {
        // First, update local state for immediate UI feedback
        val currentItems = _incomeItemsFlow.value.toMutableList()
        currentItems.removeIf { it.id == income.id }

        // Update UI state
        _incomeItemsFlow.value = currentItems
        _totalIncomeFlow.value = currentItems.sumOf { it.amount }

        // Keep in-memory list in sync for existing code that uses it
        _incomeList.clear()
        _incomeList.addAll(currentItems)

        // Update grouped data
        updateGroupedData()

        // Call repository to update Firebase
        viewModelScope.launch {
            try {
                repository.deleteIncome(income.id) { success ->
                    if (!success) {
                        // If deletion fails, reload from server to ensure consistency
                        loadCurrentBudgetPeriod()
                    }
                }
            } catch (e: Exception) {
                Log.e("BudgetManager", "Error deleting income: ${e.message}")
                // On error, reload from server
                loadCurrentBudgetPeriod()
            }
        }
    }
}