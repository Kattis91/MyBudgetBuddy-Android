package com.kat.mybudgetbuddy.budget

import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.kat.mybudgetbuddy.models.BudgetPeriod
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BudgetViewModel : ViewModel() {

    private val _loggedIn = MutableStateFlow(false)
    val loggedIn = _loggedIn.asStateFlow()

    private val _currentBudgetPeriod = MutableStateFlow<BudgetPeriod?>(null)
    val currentBudgetPeriod = _currentBudgetPeriod.asStateFlow()

    init {
        checkLogin()
    }

    private fun checkLogin() {
        if (Firebase.auth.currentUser == null) {
            _loggedIn.value = false
        } else {
            _loggedIn.value = true
        }
    }

    fun login(email: String, password: String, onError: (String) -> Unit) {
        Firebase.auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                checkLogin()  // Proceed if login is successful
            }
            .addOnFailureListener { exception ->
                // Handle error if login fails
                val errorMessage = exception.localizedMessage ?: "Login failed. Please try again."
                onError(errorMessage)  // Pass error message to onError callback
            }
    }


    fun register(email: String, password: String, onError: (String) -> Unit) {
        Firebase.auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
            checkLogin()
        }
        .addOnFailureListener { exception ->
            // Handle error if login fails
            val errorMessage = exception.localizedMessage ?: "Registration failed. Please try again."
            onError(errorMessage)  // Pass error message to onError callback
        }
    }

    fun logout() {
        Firebase.auth.signOut()
        checkLogin()
    }

    fun resetPassword(email: String, onResult: (String) -> Unit) {
        Firebase.auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                onResult("") // Empty string means success, no error
            }
            .addOnFailureListener { exception ->
                val errorMessage = exception.localizedMessage ?: "Reset failed. Please try again."
                onResult(errorMessage) // Return error message
            }
    }

    fun deleteAccount(
        password: String,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            onError("No user is signed in.")
            return
        }

        if (password.isEmpty()) {
            onError("Please enter your password.")
            return
        }

        val email = user.email
        if (email == null) {
            onError("No email associated with user.")
            return
        }

        val credential = EmailAuthProvider.getCredential(email, password)

        user.reauthenticate(credential)
            .addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    // Delete user-related data
                    val userId = user.uid
                    val databaseRef = FirebaseDatabase.getInstance().reference
                    val paths = listOf(
                        "budgetPeriods/$userId",
                        "categories/$userId",
                        "historicalPeriods/$userId",
                        "invoices/$userId",
                        "userTokens/$userId"
                    )

                    val deletions = paths.map { path ->
                        databaseRef.child(path).removeValue()
                    }

                    Tasks.whenAll(deletions)
                        .addOnCompleteListener {
                            user.delete()
                                .addOnCompleteListener { deleteTask ->
                                    if (deleteTask.isSuccessful) {
                                        FirebaseAuth.getInstance().signOut() // 🚨 LOG OUT USER
                                        checkLogin()
                                        onComplete()
                                    } else {
                                        onError("Account deletion failed: ${deleteTask.exception?.message}")
                                    }
                                }
                        }
                        .addOnFailureListener { e ->
                            onError("Failed to delete user data: ${e.localizedMessage}")
                        }

                } else {
                    onError("Incorrect password.")
                }
            }
            .addOnFailureListener { e ->
                onError("Reauthentication failed: ${e.localizedMessage}")
            }
    }
}

