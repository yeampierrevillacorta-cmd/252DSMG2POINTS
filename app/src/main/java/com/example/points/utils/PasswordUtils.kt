package com.example.points.utils

object PasswordUtils {
    // mínimo 8, al menos una mayúscula, un número y un símbolo
    fun isStrongPassword(password: String): Boolean {
        val regex = Regex("^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#\$%^&+=!]).{8,}\$")
        return regex.matches(password)
    }
}
