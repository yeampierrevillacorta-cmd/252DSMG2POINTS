package com.example.points.auth

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val rememberCredentials: Boolean = false,
    val keepSessionActive: Boolean = true,
    val isRestoringSession: Boolean = false
)
