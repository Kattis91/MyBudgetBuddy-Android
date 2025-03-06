package com.example.mybudgetbuddy

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BudgetViewModel : ViewModel() {

    private val _loggedin = MutableStateFlow(false)
    val loggedin = _loggedin.asStateFlow()

    init {
        checklogin()
    }

    fun checklogin() {
        if(Firebase.auth.currentUser == null) {
            _loggedin.value = false
        } else {
            _loggedin.value = true
        }
    }

    fun login(email : String, password : String) {
        Firebase.auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            checklogin()
        }.addOnFailureListener {

        }
    }

    fun register(email : String, password : String) {
        Firebase.auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
            checklogin()
        }.addOnFailureListener {

        }
    }

    fun logout() {
        Firebase.auth.signOut()
        checklogin()
    }
}