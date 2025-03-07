package com.example.mybudgetbuddy

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BudgetViewModel : ViewModel() {

    private val _loggedIn = MutableStateFlow(false)
    val loggedIn = _loggedIn.asStateFlow()

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

    fun login(email: String, password: String) {
        Firebase.auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            checkLogin()
        }.addOnFailureListener {

        }
    }

    fun register(email: String, password: String) {
        Firebase.auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
            checkLogin()
        }.addOnFailureListener {

        }
    }

    fun logout() {
        Firebase.auth.signOut()
        checkLogin()
    }

    fun resetPassword(email: String) {
        Firebase.auth.sendPasswordResetEmail(email).addOnSuccessListener {
            checkLogin()
        }.addOnFailureListener {

        }
    }
}

