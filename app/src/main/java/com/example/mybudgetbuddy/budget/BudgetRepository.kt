package com.example.mybudgetbuddy.budget

import android.util.Log
import com.example.mybudgetbuddy.models.BudgetPeriod
import com.example.mybudgetbuddy.models.CategoryType
import com.example.mybudgetbuddy.models.Expense
import com.example.mybudgetbuddy.models.Income
import com.example.mybudgetbuddy.models.Invoice
import com.example.mybudgetbuddy.models.defaultCategories
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.Calendar
import java.util.Date
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BudgetRepository {

    suspend fun checkForAnyBudgetPeriod(): Boolean {
        val userId = Firebase.auth.currentUser?.uid ?: return false

        val database = Firebase.database
        val ref = database.reference
        val budgetPeriodsRef = ref.child("budgetPeriods").child(userId)
        val historicalPeriodsRef = ref.child("historicalPeriods").child(userId)

        // Check both current and historical periods in parallel
        return coroutineScope {
            val hasCurrentPeriods = async {
                val snapshot = budgetPeriodsRef.get().await()
                snapshot.exists() && snapshot.hasChildren()
            }

            val hasHistoricalPeriods = async {
                val snapshot = historicalPeriodsRef.get().await()
                snapshot.exists() && snapshot.hasChildren()
            }

            // Wait for both checks and return the combined result
            hasCurrentPeriods.await() || hasHistoricalPeriods.await()
        }
    }

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
                withTimeout(10000) {
                    budgetPeriodsRef.orderByChild("startDate").limitToLast(1).get().await()
                }
            } catch (e: TimeoutCancellationException) {
                Log.e("BudgetRepository", "Query timed out", e)
                return null
            }

            // Add more debug logging
            Log.d(
                "BudgetRepository",
                "Query complete. Snapshot exists: ${snapshot.exists()}, child count: ${snapshot.childrenCount}"
            )

            if (!snapshot.exists() || snapshot.childrenCount <= 0) {
                Log.d("BudgetRepository", "No current period found in database")
                return null
            }

            // Get the period data
            val periodData = snapshot.children.iterator().next()
            Log.d(
                "BudgetRepository",
                "Period key: ${periodData.key}, has value: ${periodData.value != null}"
            )

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
                Log.d(
                    "BudgetRepository",
                    "Period expired: end date $periodEndDate is before today $today"
                )
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

                // Ensure proper type conversion
                val formattedDict = dict.mapValues {
                    when (it.value) {
                        is Long -> (it.value as Long).toInt() // Convert Long to Int
                        is Double -> (it.value as Double).toInt() // Convert Double to Int
                        else -> it.value
                    }
                }

                val period =
                    BudgetPeriod.fromDict(formattedDict)?.copy(id = periodSnapshot.key ?: "")
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

    fun saveIncomeData(amount: Double, category: String, onComplete: () -> Unit) {
        val userId = Firebase.auth.currentUser?.uid ?: return
        val database = Firebase.database
        val ref = database.reference

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val budgetPeriod = loadCurrentPeriod() ?: return@launch
                val periodId = budgetPeriod.id

                Log.d("BudgetRepo", "Starting income save for period: $periodId, category: $category, amount: $amount")

                // Reference to the incomes array in the current budget period
                val incomesRef = ref.child("budgetPeriods")
                    .child(userId)
                    .child(periodId)
                    .child("incomes")

                // Get current incomes as an array
                val snapshot = incomesRef.get().await()
                val incomesList = mutableListOf<Map<String, Any>>()

                // Process the snapshot data to a list
                if (snapshot.exists()) {
                    // Convert the data to a list
                    val genericList = snapshot.getValue<ArrayList<HashMap<String, Any>>>()
                    if (genericList != null) {
                        incomesList.addAll(genericList)
                        Log.d("BudgetRepo", "Loaded existing incomes: ${incomesList.size}")
                    }
                }

                // Check if we already have an income with this category
                var categoryFound = false
                for (i in incomesList.indices) {
                    val income = incomesList[i]
                    if ((income["category"] as? String) == category) {
                        // Update existing category
                        val existingAmount = when (val amountValue = income["amount"]) {
                            is Number -> amountValue.toDouble()
                            else -> 0.0
                        }
                        val newAmount = existingAmount + amount

                        // Create updated entry
                        val updatedIncome = income.toMutableMap()
                        updatedIncome["amount"] = newAmount
                        incomesList[i] = updatedIncome

                        categoryFound = true
                        Log.d("BudgetRepo", "Updated category: $category from $existingAmount to $newAmount")
                        break
                    }
                }

                // If no matching category, add new one
                if (!categoryFound) {
                    val newIncome = mapOf(
                        "id" to UUID.randomUUID().toString(),
                        "amount" to amount,
                        "category" to category
                    )
                    incomesList.add(newIncome)
                    Log.d("BudgetRepo", "Added new category: $category with amount: $amount")
                }

                // First update the incomes list
                incomesRef.setValue(incomesList).await()

                // Calculate new total
                val newTotal = incomesList.sumOf {
                    when (val amountValue = it["amount"]) {
                        is Number -> amountValue.toDouble()
                        else -> 0.0
                    }
                }

                // Then update the total income
                ref.child("budgetPeriods")
                    .child(userId)
                    .child(periodId)
                    .child("totalIncome")
                    .setValue(newTotal).await()

                withContext(Dispatchers.Main) {
                    onComplete()
                }
            } catch (e: Exception) {
                Log.e("BudgetRepo", "Error in saveIncomeData: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            }
        }
    }

    suspend fun loadIncomeData(): Triple<List<Income>, Map<String, Double>, Double> {
        val userId = Firebase.auth.currentUser?.uid ?: return Triple(emptyList(), emptyMap(), 0.0)
        val database = Firebase.database
        val ref = database.reference

        val budgetPeriod = loadCurrentPeriod() ?: return Triple(emptyList(), emptyMap(), 0.0)

        val incomesSnapshot = ref.child("budgetPeriods")
            .child(userId)
            .child(budgetPeriod.id)
            .child("incomes")
            .get()
            .await()

        // If snapshot doesn't exist or has no children, return empty data
        if (!incomesSnapshot.exists() || !incomesSnapshot.hasChildren()) {
            return Triple(emptyList(), emptyMap(), 0.0)
        }

        return processIncomeData(incomesSnapshot)
    }

    private fun processIncomeData(snapshot: DataSnapshot): Triple<List<Income>, Map<String, Double>, Double> {
        val incomeList = mutableListOf<Income>()
        val groupedIncome = mutableMapOf<String, Double>()
        var totalIncome = 0.0

        for (incomeSnapshot in snapshot.children) {
            val incomeDataDict = incomeSnapshot.value as? Map<*, *> ?: continue

            val fetchedIncome = Income(
                id = incomeSnapshot.key ?: "",
                amount = when (val rawAmount = incomeDataDict["amount"]) {
                    is Double -> rawAmount
                    is Long -> rawAmount.toDouble()
                    is Int -> rawAmount.toDouble()
                    is String -> rawAmount.toString().toDoubleOrNull() ?: 0.0
                    else -> 0.0
                },
                category = incomeDataDict["category"]?.toString() ?: "Unknown"
            )

            incomeList.add(fetchedIncome)

            val category = fetchedIncome.category
            groupedIncome[category] = (groupedIncome[category] ?: 0.0) + fetchedIncome.amount
        }

        totalIncome = incomeList.sumOf { it.amount }

        return Triple(incomeList, groupedIncome, totalIncome)
    }

    // In BudgetRepository.kt
    fun deleteIncome(incomeId: String, onComplete: (Boolean) -> Unit = {}) {
        val userId = Firebase.auth.currentUser?.uid ?: run {
            onComplete(false)
            return
        }
        val databaseRef = Firebase.database.reference

        // Launch a coroutine to handle the suspend function
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val budgetPeriod = loadCurrentPeriod() ?: run {
                    withContext(Dispatchers.Main) { onComplete(false) }
                    return@launch
                }

                // Delete from Firebase
                databaseRef.child("budgetPeriods")
                    .child(userId)
                    .child(budgetPeriod.id)
                    .child("incomes")
                    .child(incomeId)
                    .removeValue()
                    .await()

                // Update the budget period's income list in memory
                val updatedIncomes = budgetPeriod.incomes.filter { it.id != incomeId }

                // Update the total income in Firebase
                val totalIncome = updatedIncomes.sumOf { it.amount }
                databaseRef.child("budgetPeriods")
                    .child(userId)
                    .child(budgetPeriod.id)
                    .child("totalIncome")
                    .setValue(totalIncome)
                    .await()

                withContext(Dispatchers.Main) {
                    onComplete(true)
                }
            } catch (e: Exception) {
                Log.e("BudgetRepository", "Error deleting income: ${e.message}")
                withContext(Dispatchers.Main) {
                    onComplete(false)
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

    fun saveExpenseData(amount: Double, category: String, isfixed: Boolean, onComplete: () -> Unit) {
        val userId = Firebase.auth.currentUser?.uid ?: return
        val database = Firebase.database
        val ref = database.reference

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val budgetPeriod = loadCurrentPeriod() ?: return@launch
                val periodId = budgetPeriod.id

                Log.d("BudgetRepo", "Starting expense save for period: $periodId, category: $category, amount: $amount, isFixed: $isfixed")

                // Reference to the incomes array in the current budget period
                val expensesRef = ref.child("budgetPeriods")
                    .child(userId)
                    .child(periodId)
                    .child(if (isfixed) "fixedExpenses" else "variableExpenses")

                // Get current incomes as an array
                val snapshot = expensesRef.get().await()
                val expensesList = mutableListOf<Map<String, Any>>()

                // Process the snapshot data to a list
                if (snapshot.exists()) {
                    // Convert the data to a list
                    val genericList = snapshot.getValue<ArrayList<HashMap<String, Any>>>()
                    if (genericList != null) {
                        expensesList.addAll(genericList)
                        Log.d("BudgetRepo", "Loaded existing incomes: ${expensesList.size}")
                    }
                }

                // Check if we already have an expense with this category
                var categoryFound = false
                for (i in expensesList.indices) {
                    val expense = expensesList[i]
                    if ((expense["category"] as? String) == category) {
                        // Update existing category
                        val existingAmount = when (val amountValue = expense["amount"]) {
                            is Number -> amountValue.toDouble()
                            else -> 0.0
                        }
                        val newAmount = existingAmount + amount

                        // Create updated entry
                        val updatedExpense = expense.toMutableMap()
                        updatedExpense["amount"] = newAmount
                        expensesList[i] = updatedExpense

                        categoryFound = true
                        Log.d("BudgetRepo", "Updated category: $category from $existingAmount to $newAmount")
                        break
                    }
                }

                // If no matching category, add new one
                if (!categoryFound) {
                    val newIncome = mapOf(
                        "id" to UUID.randomUUID().toString(),
                        "amount" to amount,
                        "category" to category,
                        "isfixed" to isfixed
                    )
                    expensesList.add(newIncome)
                    Log.d("BudgetRepo", "Added new category: $category with amount: $amount")
                }

                // First update the expenses list
                expensesRef.setValue(expensesList).await()

                // Calculate new total
                val newTotal = expensesList.sumOf {
                    when (val amountValue = it["amount"]) {
                        is Number -> amountValue.toDouble()
                        else -> 0.0
                    }
                }

                // Then update the total expense
                ref.child("budgetPeriods")
                    .child(userId)
                    .child(periodId)
                    .child(if (isfixed) "totalFixedExpenses" else "totalVariableExpenses")
                    .setValue(newTotal).await()

                withContext(Dispatchers.Main) {
                    onComplete()
                }
            } catch (e: Exception) {
                Log.e("BudgetRepo", "Error in saveExpenseData: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            }
        }
    }

    // Load fixed expenses
    suspend fun loadFixedExpenseData(): Pair<List<Expense>, Double> {
        return loadExpenseData(isfixed = true)
    }

    // Load variable expenses
    suspend fun loadVariableExpenseData(): Pair<List<Expense>, Double> {
        return loadExpenseData(isfixed = false)
    }

    private suspend fun loadExpenseData(isfixed: Boolean): Pair<List<Expense>, Double> {
        val userId = Firebase.auth.currentUser?.uid ?: return Pair(emptyList(), 0.0)
        val database = Firebase.database
        val ref = database.reference

        val budgetPeriod = loadCurrentPeriod() ?: return Pair(emptyList(), 0.0)

        val expensesSnapshot = ref.child("budgetPeriods")
            .child(userId)
            .child(budgetPeriod.id)
            .child(if (isfixed) "fixedExpenses" else "variableExpenses")
            .get()
            .await()

        if (!expensesSnapshot.exists() || !expensesSnapshot.hasChildren()) {
            return Pair(emptyList(), 0.0)
        }

        return processExpenseData(expensesSnapshot, isfixed)
    }

    private fun processExpenseData(snapshot: DataSnapshot, isfixed: Boolean): Pair<List<Expense>, Double>  {
        val expenseList = mutableListOf<Expense>()
        var totalExpenses = 0.0

        for (expensesSnapshot in snapshot.children) {
            val expenseDataDict = expensesSnapshot.value as? Map<*, *> ?: continue

            val fetchedExpense = Expense(
                id = expensesSnapshot.key ?: "",
                amount = when (val rawAmount = expenseDataDict["amount"]) {
                    is Double -> rawAmount
                    is Long -> rawAmount.toDouble()
                    is Int -> rawAmount.toDouble()
                    is String -> rawAmount.toString().toDoubleOrNull() ?: 0.0
                    else -> 0.0
                },
                category = expenseDataDict["category"]?.toString() ?: "Unknown",
                isfixed = expenseDataDict["isfixed"] as? Boolean ?: false
            )

            expenseList.add(fetchedExpense)
        }

        totalExpenses = expenseList.sumOf { it.amount }
        return Pair(expenseList, totalExpenses)

    }

    fun deleteExpense(expenseId: String, isfixed: Boolean, onComplete: (Boolean) -> Unit = {}) {
        val userId = Firebase.auth.currentUser?.uid ?: run {
            onComplete(false)
            return
        }
        val databaseRef = Firebase.database.reference

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val budgetPeriod = loadCurrentPeriod() ?: run {
                    withContext(Dispatchers.Main) { onComplete(false) }
                    return@launch
                }

                // Delete from Firebase
                databaseRef.child("budgetPeriods")
                    .child(userId)
                    .child(budgetPeriod.id)
                    .child(if (isfixed) "fixedExpenses" else "variableExpenses")
                    .child(expenseId)
                    .removeValue()
                    .await()

                // Update totals
                val updatedExpensesList = if (isfixed) {
                    budgetPeriod.fixedExpenses.filter { it.id != expenseId }
                } else {
                    budgetPeriod.variableExpenses.filter { it.id != expenseId }
                }

                val totalExpenses = updatedExpensesList.sumOf { it.amount }
                databaseRef.child("budgetPeriods")
                    .child(userId)
                    .child(budgetPeriod.id)
                    .child(if (isfixed) "totalFixedExpenses" else "totalVariableExpenses")
                    .setValue(totalExpenses)
                    .await()

                withContext(Dispatchers.Main) {
                    onComplete(true)
                }
            } catch (e: Exception) {
                Log.e("BudgetRepository", "Error deleting income: ${e.message}")
                withContext(Dispatchers.Main) {
                    onComplete(false)
                }
            }
        }
    }

    suspend fun loadCategories(type: CategoryType): List<String> {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return emptyList()

        val ref = Firebase.database.reference
            .child("categories")
            .child(userId)
            .child(type.value)

        return try {
            val snapshot = ref.get().await()
            if (snapshot.exists()) {
                snapshot.getValue<List<String>>() ?: emptyList()
            } else {
                // If no categories exist, create default ones
                val defaults = type.defaultCategories
                ref.setValue(defaults).await()
                defaults
            }
        } catch (e: Exception) {
            // Handle potential errors (e.g., network issues)
            println("Error loading categories: ${e.message}")
            emptyList()
        }
    }

    suspend fun addCategory(name: String, type: CategoryType): Boolean {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return false

        val ref = Firebase.database.reference
            .child("categories")
            .child(userId)
            .child(type.value)

        return try {
            val snapshot = ref.get().await()
            val categories = snapshot.getValue<List<String>>()?.toMutableList() ?: mutableListOf()

            if (name in categories) {
                return false
            }

            categories.add(name)
            ref.setValue(categories).await()
            true
        } catch (e: Exception) {
            println("Error adding category: ${e.message}")
            false
        }
    }

    suspend fun editCategory(oldName: String, newName: String, type: CategoryType): Boolean {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return false

        val ref = Firebase.database.reference
            .child("categories")
            .child(userId)
            .child(type.value)

        return try {
            val snapshot = ref.get().await()
            val categories = snapshot.getValue<List<String>>()?.toMutableList() ?: mutableListOf()

            // Find the index of the old category name
            val index = categories.indexOf(oldName)

            // If the category wasn't found, return false
            if (index == -1) {
                return false
            }

            // Check if the new name already exists in the list (and it's not just renaming to the same name)
            if (newName != oldName && newName in categories) {
                return false
            }

            // Replace the old name with the new name
            categories[index] = newName

            // Update the database
            ref.setValue(categories).await()
            true
        } catch (e: Exception) {
            println("Error editing category: ${e.message}")
            false
        }
    }

    suspend fun deleteCategory(name: String, type: CategoryType): Boolean {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return false

        val ref = Firebase.database.reference
            .child("categories")
            .child(userId)
            .child(type.value)

        return try {
            val snapshot = ref.get().await()
            val categories = snapshot.getValue<List<String>>()?.toMutableList() ?: mutableListOf()
            categories.remove(name)
            ref.setValue(categories).await()
            true
        } catch (e: Exception) {
            println("Error deleting category: ${e.message}")
            false
        }
    }

    fun saveInvoiceReminder(title: String, amount: Double, expiryDate: Date) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
            ?: run {
                Log.e("InvoiceReminder", "Error: Unable to retrieve user ID")
                return
            }

        val ref = Firebase.database.reference

        val invoiceEntry = mapOf(
            "title" to title,
            "amount" to amount,
            "expiryDate" to expiryDate.time / 1000,
            "processed" to false,
            "uid" to userId
        )

        ref.child("invoices")
            .child(userId)
            .push()  // This is equivalent to childByAutoId() in iOS
            .setValue(invoiceEntry)
            .addOnSuccessListener {
                Log.d("InvoiceReminder", "Invoice saved successfully")
            }
            .addOnFailureListener { error ->
                Log.e("InvoiceReminder", "Error saving invoice: ${error.message}")
            }
    }

    suspend fun loadInvoices(): List<Invoice> {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
            ?: return emptyList()

        val ref = Firebase.database.reference
            .child("invoices")
            .child(userId)

        return try {
            val snapshot = ref.get().await()
            val invoices = mutableListOf<Invoice>()

            if (snapshot.exists()) {
                for (childSnapshot in snapshot.children) {
                    Log.d("InvoiceReminder", "Child snapshot: ${childSnapshot.value}")
                    val invoiceData = childSnapshot.value as? Map<String, Any> ?: continue

                    val title = invoiceData["title"] as? String ?: continue
                    val amount = when (val rawAmount = invoiceData["amount"]) {
                        is Double -> rawAmount
                        is Long -> rawAmount.toDouble()
                        else -> continue
                    }

                    val processed = invoiceData["processed"] as? Boolean ?: continue
                    val expiryDateTimestamp = invoiceData["expiryDate"] as? Long ?: continue

                    val expiryDate = Date(expiryDateTimestamp * 1000)

                    val invoice = Invoice(
                        id = childSnapshot.key ?: "",
                        title = title,
                        amount = amount,
                        processed = processed,
                        expiryDate = expiryDate
                    )

                    invoices.add(invoice)
                }
            }

            invoices.sortedBy { it.expiryDate }
        } catch (e: Exception) {
            Log.e("InvoiceReminder", "Error loading invoices: ${e.message}")
            emptyList()
        }
    }

    suspend fun loadInvoicesByStatus(processed: Boolean): List<Invoice> {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
            ?: return emptyList()

        val ref = Firebase.database.reference
            .child("invoices")
            .child(userId)

        return suspendCoroutine { continuation ->
            // Use orderByChild and equalTo for filtering
            ref.orderByChild("processed")
                .equalTo(processed)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val invoices = mutableListOf<Invoice>()

                        for (childSnapshot in snapshot.children) {
                            try {
                                val invoiceData = childSnapshot.getValue(object :
                                    GenericTypeIndicator<Map<String, Any>>() {}) ?: continue

                                val title = invoiceData["title"] as? String ?: continue
                                val amount = when (val rawAmount = invoiceData["amount"]) {
                                    is Double -> rawAmount
                                    is Long -> rawAmount.toDouble()
                                    else -> continue
                                }
                                val processedStatus =
                                    invoiceData["processed"] as? Boolean ?: continue
                                val expiryDateTimestamp =
                                    (invoiceData["expiryDate"] as? Number)?.toLong() ?: continue

                                val expiryDate = Date(expiryDateTimestamp * 1000)

                                val invoice = Invoice(
                                    id = childSnapshot.key ?: "",
                                    title = title,
                                    amount = amount,
                                    processed = processedStatus,
                                    expiryDate = expiryDate
                                )

                                invoices.add(invoice)
                            } catch (e: Exception) {
                                Log.e("InvoiceReminder", "Error parsing invoice: ${e.message}")
                            }
                        }

                        // Sort invoices by expiry date
                        val sortedInvoices = invoices.sortedBy { it.expiryDate }
                        continuation.resume(sortedInvoices)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("InvoiceReminder", "Error loading invoices: ${error.message}")
                        continuation.resume(emptyList())
                    }
                })
        }
    }

    suspend fun updateInvoiceStatus(invoiceId: String, processed: Boolean) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw Exception("User not authenticated")

        val ref = Firebase.database.reference
            .child("invoices")
            .child(userId)
            .child(invoiceId)

        try {
            ref.updateChildren(mapOf("processed" to processed)).await()
        } catch (e: Exception) {
            throw Exception("Failed to update invoice status: ${e.message}")
        }
    }

}

