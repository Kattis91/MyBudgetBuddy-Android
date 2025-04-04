package com.kat.mybudgetbuddy.utils

object ValidationUtils {

    // Validate email format
    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}$".toRegex()
        return emailRegex.matches(email)
    }

    // Validate password strength (e.g., minimum 6 characters)
    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    // Check if a field is empty
    fun isNotEmpty(text: String): Boolean {
        return text.trim().isNotEmpty()
    }

    fun validateEmail(email: String): String? {
        if (!isNotEmpty(email)) {
            return "Email field cannot be empty."
        } else if (!isValidEmail(email)) {
            return "Invalid email format."
        }
        return null
    }

    fun validatePassword(password: String): String? {
        if (!isNotEmpty(password)) {
            return "Password field cannot be empty."
        } else if (!isValidPassword(password)) {
            return "Password must be 6+ characters."
        }
        return null
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        if (!isNotEmpty(password)) {
            return "Password field cannot be empty."
        } else if (password != confirmPassword) {
            return "Passwords do not match."
        }
        return null
    }

    fun validateReset(email: String): String? {
        if (!isNotEmpty(email)) {
            return "Email field cannot be empty."
        } else if (!isValidEmail(email)) {
            return "Invalid email format."
        }
        return null
    }
}
