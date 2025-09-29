package com.example.points.auth

import android.net.Uri

data class RegisterUiState(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val photoUri: Uri? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
