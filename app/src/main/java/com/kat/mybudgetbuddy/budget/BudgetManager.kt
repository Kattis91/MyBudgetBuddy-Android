package com.kat.mybudgetbuddy.budget

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kat.mybudgetbuddy.models.BudgetPeriod
import com.kat.mybudgetbuddy.models.CategoryType
import com.kat.mybudgetbuddy.models.Expense
import com.kat.mybudgetbuddy.models.Income
import com.kat.mybudgetbuddy.models.Invoice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    private val _invoicesFlow = MutableStateFlow<List<Invoice>>(emptyList())
    val invoices: StateFlow<List<Invoice>> = _invoicesFlow.asStateFlow()

    private val _unprocessedInvoices = MutableStateFlow<List<Invoice>>(emptyList())
    val unprocessedInvoices: StateFlow<List<Invoice>> = _unprocessedInvoices

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

    fun loadCurrentBudgetPeriod() {
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

    fun loadHistoricalPeriods() {
        _isLoading.value = true // Set loading to true at start

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val periods = repository.loadHistoricalPeriods()

                withContext(Dispatchers.Main) {
                    _historicalPeriods.value = periods
                    _isLoading.value = false // Set loading to false when complete
                }
            } catch (e: Exception) {
                Log.e("BudgetManager", "Error loading historical periods: ${e.message}")

                withContext(Dispatchers.Main) {
                    _isLoading.value = false // Also set loading to false on error
                }
            }
        }
    }

    fun deleteHistoricalPeriod(periodId: String) {
        // Keep track of items being deleted to prevent visual glitches
        val currentList = _historicalPeriods.value.toMutableList()

        // Immediately remove from UI list
        val updatedList = currentList.filter { it.id != periodId }
        _historicalPeriods.value = updatedList

        // Then delete from Firebase
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val success = repository.deleteHistoricalPeriod(periodId)

                if (!success) {
                    // If deletion failed, restore the item to the list
                    withContext(Dispatchers.Main) {
                        _historicalPeriods.value = currentList
                    }
                    Log.e("BudgetManager", "Failed to delete period $periodId")
                }
            } catch (e: Exception) {
                // If error occurred, restore the item to the list
                withContext(Dispatchers.Main) {
                    _historicalPeriods.value = currentList
                }
                Log.e("BudgetManager", "Error deleting historical period: ${e.message}")
            }
        }
    }

    fun startNewPeriod(
        startDate: Date,
        endDate: Date,
        includeIncomes: Boolean = false,
        includeFixedExpenses: Boolean = false,
    ) {
        // Create new period without transferred data initially
        val newPeriod = BudgetPeriod(
            id = UUID.randomUUID().toString(),
            startDate = startDate,
            endDate = endDate,
            incomes = emptyList(),
            fixedExpenses = emptyList(),
            variableExpenses = emptyList(),
            expired = false
        )

        // Clear all lists first
        _incomeList.clear()
        _fixedExpenseList.clear()
        _variableExpenseList.clear()

        // Set the initial state
        _currentPeriod.value = newPeriod
        updateGroupedData()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Let the repository handle the transfer
                val success = repository.saveBudgetPeriod(
                    newPeriod,
                    Pair(includeIncomes, includeFixedExpenses)
                )

                if (success) {
                    // Reload the current period to get the transferred data
                    val updatedPeriod = repository.loadCurrentPeriod()

                    withContext(Dispatchers.Main) {
                        updatedPeriod?.let { period ->
                            _currentPeriod.value = period
                            _incomeList.clear() // Clear again to prevent duplicates
                            _incomeList.addAll(period.incomes)
                            _fixedExpenseList.clear() // Clear again to prevent duplicates
                            _fixedExpenseList.addAll(period.fixedExpenses)
                            updateGroupedData()
                        }
                    }
                }
                loadCurrentBudgetPeriod()

            } catch (e: Exception) {
                Log.e("BudgetManager", "Error saving new period: ${e.message}")
            }
        }
    }

    fun createCleanBudgetPeriodAndRefresh(
        startDate: Date,
        endDate: Date,
        onComplete: (Boolean) -> Unit
    ) {
        repository.createCleanBudgetPeriod(startDate, endDate) { success ->
            if (success) {
                // First load the current period
                loadCurrentBudgetPeriod()

                // Use a coroutine for the sequential operations and delay
                viewModelScope.launch {
                    // Give some time for current period to be processed
                    delay(500)

                    // Then load historical periods
                    loadHistoricalPeriods()

                    // Then call the completion handler
                    onComplete(true)
                }
            } else {
                onComplete(false)
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

    suspend fun addCategory(category: String, type: CategoryType): Boolean {
        if (category.isBlank()) return false

        return try {
            val success = repository.addCategory(category, type)

            if (success) {
                when (type) {
                    CategoryType.INCOME -> _incomeCategories.value += category
                    CategoryType.FIXED_EXPENSE -> _fixedExpenseCategories.value += category
                    CategoryType.VARIABLE_EXPENSE -> _variableExpenseCategories.value += category
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("BudgetManager", "Error adding category: ${e.message}")
            false
        }
    }

    fun editCategory(oldName: String, newName: String, type: CategoryType) {
        if (newName.isBlank()) return

        viewModelScope.launch {
            try {
                val success = repository.editCategory(oldName, newName, type)

                if (success) {
                    // Update the appropriate category list with the new name
                    when (type) {
                        CategoryType.INCOME -> {
                            _incomeCategories.value = _incomeCategories.value.map {
                                if (it == oldName) newName else it
                            }
                        }
                        CategoryType.FIXED_EXPENSE -> {
                            _fixedExpenseCategories.value = _fixedExpenseCategories.value.map {
                                if (it == oldName) newName else it
                            }
                        }
                        CategoryType.VARIABLE_EXPENSE -> {
                            _variableExpenseCategories.value = _variableExpenseCategories.value.map {
                                if (it == oldName) newName else it
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("BudgetManager", "Error editing category: ${e.message}")
            }
        }
    }

    fun deleteCategory(category: String, type: CategoryType) {
        viewModelScope.launch {
            try {
                val success = repository.deleteCategory(category, type)

                if (success) {
                    when (type) {
                        CategoryType.INCOME -> _incomeCategories.value -= category
                        CategoryType.FIXED_EXPENSE -> _fixedExpenseCategories.value -= category
                        CategoryType.VARIABLE_EXPENSE -> _variableExpenseCategories.value -= category
                    }
                }
                } catch (e: Exception) {
                Log.e("BudgetManager", "Error deleting category: ${e.message}")
            }
        }
    }

    fun addInvoice(title: String, amount: Double, expiryDate: Date) {
        Log.d("BudgetManager", "Attempting to save invoice: $title, $amount, $expiryDate")
        viewModelScope.launch {
            try {
                repository.saveInvoiceReminder(title, amount, expiryDate)
                Log.d("BudgetManager", "Invoice successfully added!")
            } catch (e: Exception) {
                Log.e("BudgetManager", "Error adding invoice: ${e.message}")
            }
        }
    }

    fun loadInvoices() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val loadedInvoices = repository.loadInvoices()
                _invoicesFlow.value = loadedInvoices
                Log.d("BudgetManager", "Loaded ${loadedInvoices.size} invoices")
            } catch (e: Exception) {
                Log.e("BudgetManager", "Error loading invoices: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadInvoicesByStatus(processed: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            val invoices = repository.loadInvoicesByStatus(processed)
            _invoicesFlow.value = invoices

            // Also update the unprocessed invoices state if we're loading unprocessed ones
            if (!processed) {
                _unprocessedInvoices.value = invoices
            }

            _isLoading.value = false
        }
    }

    fun markInvoiceAsProcessed(invoiceId: String, processed: Boolean) {
        viewModelScope.launch {
            try {
                // First update the local state for immediate UI feedback
                _unprocessedInvoices.update { currentList ->
                    currentList.filter { it.id != invoiceId }
                }

                // Also update the regular invoices list if needed
                _invoicesFlow.value = _invoicesFlow.value.filter { it.id != invoiceId }

                // Then update Firebase
                repository.updateInvoiceStatus(invoiceId, processed)

                // After a short delay, refresh processed invoices if we're on that tab
                delay(500)
                loadInvoicesByStatus(processed = true)
            } catch (e: Exception) {
                Log.e("BudgetManager", "Error marking invoice as processed", e)
            }
        }
    }

    fun deleteInvoice(invoiceId: String) {
        viewModelScope.launch {
            try {
                // Update the local state immediately for UI feedback
                _unprocessedInvoices.update { currentList ->
                    currentList.filter { it.id != invoiceId }
                }

                // Also update the regular invoices list
                _invoicesFlow.value = _invoicesFlow.value.filter { it.id != invoiceId }

                // Then delete from Firebase
                val result = repository.deleteInvoiceReminder(invoiceId)
                if (result) {
                    Log.d("BudgetManager", "Invoice deleted successfully")
                }
            } catch (e: Exception) {
                Log.e("BudgetManager", "Error deleting invoice: ${e.message}")
                // If there's an error, you might want to restore the item in your UI state
            }
        }
    }
}