package com.example.points.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.points.models.TipoUsuario
import com.example.points.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository(
        com.google.firebase.firestore.FirebaseFirestore.getInstance(),
        auth
    )

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun loginUser(onSuccess: (TipoUsuario) -> Unit) {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Correo y contraseña no pueden estar vacíos")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Obtener el tipo de usuario después del login exitoso
                        viewModelScope.launch {
                            try {
                                val userType = userRepository.getCurrentUserType()
                                _uiState.value = _uiState.value.copy(isLoading = false)
                                onSuccess(userType)
                            } catch (e: Exception) {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = "Error al obtener información del usuario: ${e.message}"
                                )
                            }
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = task.exception?.localizedMessage
                        )
                    }
                }
        }
    }
}
