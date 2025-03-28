package com.example.mybudgetbuddy.budget

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybudgetbuddy.models.BudgetPeriod
import com.example.mybudgetbuddy.models.CategoryType
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
    private val _currentPeriod = MutableLiveData<BudgetPeriod?>()
    val currentPeriod: MutableLiveData<BudgetPeriod?> get() = _currentPeriod

    private val _hasExistingPeriods = MutableLiveData<Boolean>()
    val hasExistingPeriods: LiveData<Boolean> = _hasExistingPeriods

    private val _isCheckingPeriods = MutableLiveData<Boolean>()
    val isCheckingPeriods: LiveData<Boolean> = _isCheckingPeriods

    private val _historicalPeriods = MutableStateFlow<List<BudgetPeriod>>(emptyList())
    val historicalPeriods: StateFlow<List<BudgetPeriod>> get() = _historicalPeriods

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

    private val _fixedExpensesItemsFlow = MutableStateFlow<List<Expense>>(emptyList())
    val fixedExpenseItems: StateFlow<List<Expense>> = _fixedExpensesItemsFlow.asStateFlow()

    private val _variableExpensesItemsFlow = MutableStateFlow<List<Expense>>(emptyList())
    val variableExpenseItems: StateFlow<List<Expense>> = _variableExpensesItemsFlow.asStateFlow()

    private val _totalExpensesFlow = MutableStateFlow(0.0)
    val totalExpenses: StateFlow<Double> = _totalExpensesFlow.asStateFlow()

    private val _incomeCategories = MutableStateFlow<List<String>>(emptyList())
    val incomeCategories: StateFlow<List<String>> = _incomeCategories

    private val _fixedExpenseCategories = MutableStateFlow<List<String>>(emptyList())
    val fixedExpenseCategories: StateFlow<List<String>> = _fixedExpenseCategories

    private val _variableExpenseCategories = MutableStateFlow<List<String>>(emptyList())
    val variableExpenseCategories: StateFlow<List<String>> = _variableExpenseCategories

    fun getPeriodById(periodId: String?): BudgetPeriod? {
        return historicalPeriods.value.find { it.id == periodId } // Assuming periodList is a LiveData or StateFlow
    }

    init {
        loadData()
        checkInitialState()
    }

    fun loadData() {
        _isLoading.value = true
        loadCurrentBudgetPeriod()
        loadHistoricalPeriods()
    }

    fun checkInitialState() {
        _isCheckingPeriods.value = true

        viewModelScope.launch {
            val exists = repository.checkForAnyBudgetPeriod()
            _hasExistingPeriods.value = exists

            if (exists) {
                val currentPeriod = repository.loadCurrentPeriod()
                _currentPeriod.value = currentPeriod
            }

            _isCheckingPeriods.value = false
        }
    }

    private fun loadCurrentBudgetPeriod() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Initialize with default empty values
                var budgetPeriod: BudgetPeriod? = null
                var incomeList = emptyList<Income>()
                var groupedIncome = emptyMap<String, Double>()
                var totalIncome = 0.0

                var fixedExpenseList = emptyList<Expense>()
                var totalFixedExpenses = 0.0

                var variableExpenseList = emptyList<Expense>()
                var totalVariableExpense = 0.0

                // Load the current budget period
                try {
                    budgetPeriod = repository.loadCurrentPeriod()
                } catch (e: Exception) {
                    Log.e("BudgetManager", "Error loading budget period: ${e.message}")
                    // Continue with null budgetPeriod
                }

                // Only try to load data if we have a valid budget period
                if (budgetPeriod != null) {
                    // Load income data
                    try {
                        val incomeData = repository.loadIncomeData()
                        incomeList = incomeData.first
                        groupedIncome = incomeData.second
                        totalIncome = incomeData.third
                    } catch (e: Exception) {
                        Log.e("BudgetManager", "Error loading income data: ${e.message}")
                        // Continue with empty defaults
                    }

                    // Load fixed expense data
                    try {
                        val fixedExpenseData = repository.loadFixedExpenseData()
                        fixedExpenseList = fixedExpenseData.first
                        totalFixedExpenses = fixedExpenseData.second
                    } catch (e: Exception) {
                        Log.e("BudgetManager", "Error loading fixed expense data: ${e.message}")
                        // Continue with empty defaults
                    }

                    // Load variable expense data
                    try {
                        val variableExpenseData = repository.loadVariableExpenseData()
                        variableExpenseList = variableExpenseData.first
                        totalVariableExpense = variableExpenseData.second
                    } catch (e: Exception) {
                        Log.e("BudgetManager", "Error loading variable expense data: ${e.message}")
                        // Continue with empty defaults
                    }
                }

                // Update the UI with loaded data (or empty defaults)
                withContext(Dispatchers.Main) {
                    _incomeItemsFlow.value = incomeList
                    _totalIncomeFlow.value = totalIncome

                    _fixedExpensesItemsFlow.value = fixedExpenseList
                    _variableExpensesItemsFlow.value = variableExpenseList
                    _totalExpensesFlow.value = totalFixedExpenses + totalVariableExpense

                    // Only set currentPeriod if we have a valid one
                    budgetPeriod?.let {
                        _currentPeriod.value = it

                        // Update in-memory lists
                        _incomeList.clear()
                        _incomeList.addAll(it.incomes)

                        _fixedExpenseList.clear()
                        _fixedExpenseList.addAll(it.fixedExpenses)

                        _variableExpenseList.clear()
                        _variableExpenseList.addAll(it.variableExpenses)

                        updateGroupedData()
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e("BudgetManager", "Unexpected error in loadCurrentBudgetPeriod: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    // Ensure UI is updated even in case of critical failure
                    _incomeItemsFlow.value = emptyList()
                    _totalIncomeFlow.value = 0.0
                    _fixedExpensesItemsFlow.value = emptyList()
                    _variableExpensesItemsFlow.value = emptyList()
                    _totalExpensesFlow.value = 0.0

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
            } catch (e: Exception) {
                Log.e("BudgetManager", "Error saving income data: ${e.message}")
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
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addExpense(amount: Double, category: String, isfixed: Boolean) {
        if (amount <= 0 || category.isEmpty()) {
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                repository.saveExpenseData(amount, category, isfixed) {
                    // This runs when the income has been successfully saved
                    loadCurrentBudgetPeriod()
                }
            } catch (e: Exception) {
                Log.e("BudgetManager", "Error saving expense data: ${e.message}")
                // Still need to update UI state
                _isLoading.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteExpenseItem(expense: Expense, isfixed: Boolean) {
        // First, update local state for immediate UI feedback
        if (isfixed) {
            val currentFixedItems = _fixedExpensesItemsFlow.value.toMutableList()
            currentFixedItems.removeIf { it.id == expense.id }
            _fixedExpensesItemsFlow.value = currentFixedItems
            _fixedExpenseList.clear()
            _fixedExpenseList.addAll(currentFixedItems)
        } else {
            val currentVariableItems = _variableExpensesItemsFlow.value.toMutableList()
            currentVariableItems.removeIf { it.id == expense.id }
            _variableExpensesItemsFlow.value = currentVariableItems
            _variableExpenseList.clear()
            _variableExpenseList.addAll(currentVariableItems)
        }

        _totalExpensesFlow.value = _fixedExpensesItemsFlow.value.sumOf { it.amount } + _variableExpensesItemsFlow.value.sumOf { it.amount }
        updateGroupedData()

        // Call repository to update Firebase
        viewModelScope.launch {
            try {
                repository.deleteExpense(expense.id, isfixed = expense.isfixed) { success ->
                    if (!success) {
                        // If deletion fails, reload from server to ensure consistency
                        loadCurrentBudgetPeriod()
                    }
                }
            } catch (e: Exception) {
                Log.e("BudgetManager", "Error deleting expense: ${e.message}")
                // On error, reload from server
                loadCurrentBudgetPeriod()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadIncomeCategories() {
        viewModelScope.launch {
            val categories = repository.loadCategories(CategoryType.INCOME)
            _incomeCategories.value = categories
        }
    }

    fun loadFixedExpenseCategories() {
        viewModelScope.launch {
            val categories = repository.loadCategories(CategoryType.FIXED_EXPENSE)
            _fixedExpenseCategories.value = categories
        }
    }

    fun loadVariableExpenseCategories() {
        viewModelScope.launch {
            val categories = repository.loadCategories(CategoryType.VARIABLE_EXPENSE)
            _variableExpenseCategories.value = categories
        }
    }

    fun addCategory(category: String, type: CategoryType) {
        if (category.isBlank()) return

        viewModelScope.launch {
            try {
                repository.addCategory(category, type)

                when (type) {
                    CategoryType.INCOME -> _incomeCategories.value += category
                    CategoryType.FIXED_EXPENSE -> _fixedExpenseCategories.value += category
                    CategoryType.VARIABLE_EXPENSE -> _variableExpenseCategories.value += category
                }
            } catch (e: Exception) {
                Log.e("BudgetManager", "Error adding category: ${e.message}")
            }
        }
    }
}