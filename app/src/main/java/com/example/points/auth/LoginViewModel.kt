package com.example.points.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.points.data.CredentialsStorage
import com.example.points.models.TipoUsuario
import com.example.points.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val credentialsStorage: CredentialsStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    private var sessionCheckInProgress = false

    init {
        restoreSavedPreferences()
    }

    private fun restoreSavedPreferences() {
        val remember = credentialsStorage.rememberCredentials
        val keepSession = credentialsStorage.keepSessionActive
        val email = if (remember) credentialsStorage.getSavedEmail() else ""
        val password = if (remember) credentialsStorage.getSavedPassword() else ""
        _uiState.value = _uiState.value.copy(
            email = email,
            password = password,
            rememberCredentials = remember,
            keepSessionActive = keepSession
        )
    }

    fun onEmailChange(email: String) {
        val newState = _uiState.value.copy(email = email)
        _uiState.value = newState
        persistDraftCredentialsIfNeeded(newState)
    }

    fun onPasswordChange(password: String) {
        val newState = _uiState.value.copy(password = password)
        _uiState.value = newState
        persistDraftCredentialsIfNeeded(newState)
    }

    fun onRememberCredentialsChange(remember: Boolean) {
        _uiState.value = _uiState.value.copy(rememberCredentials = remember)
        credentialsStorage.rememberCredentials = remember
        if (remember) {
            persistDraftCredentialsIfNeeded(_uiState.value)
        } else {
            credentialsStorage.clearCredentials()
        }
    }

    fun onKeepSessionChange(keepSession: Boolean) {
        _uiState.value = _uiState.value.copy(keepSessionActive = keepSession)
        credentialsStorage.keepSessionActive = keepSession
        if (!keepSession) {
            auth.signOut()
        }
    }

    fun tryRestoreSession(onSuccess: (TipoUsuario) -> Unit) {
        if (!credentialsStorage.keepSessionActive || sessionCheckInProgress) return
        val currentUser = auth.currentUser ?: return
        sessionCheckInProgress = true
        _uiState.value = _uiState.value.copy(isRestoringSession = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val userType = userRepository.getCurrentUserType()
                _uiState.value = _uiState.value.copy(isRestoringSession = false)
                onSuccess(userType)
            } catch (e: Exception) {
                auth.signOut()
                _uiState.value = _uiState.value.copy(
                    isRestoringSession = false,
                    errorMessage = "No se pudo restaurar la sesión: ${e.message}"
                )
            } finally {
                sessionCheckInProgress = false
            }
        }
    }

    fun loginUser(onSuccess: (TipoUsuario) -> Unit) {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Correo y contraseña no pueden estar vacíos")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Por favor ingresa un correo electrónico válido")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        viewModelScope.launch {
                            try {
                                persistPreferencesAfterLogin(email, password)
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

    private fun persistDraftCredentialsIfNeeded(state: LoginUiState) {
        if (!state.rememberCredentials) return
        val email = state.email.trim()
        val password = state.password
        if (email.isBlank() || password.isBlank()) return
        credentialsStorage.saveCredentials(email, password)
    }

    private fun persistPreferencesAfterLogin(email: String, password: String) {
        credentialsStorage.keepSessionActive = _uiState.value.keepSessionActive
        credentialsStorage.rememberCredentials = _uiState.value.rememberCredentials
        if (_uiState.value.rememberCredentials) {
            credentialsStorage.saveCredentials(email, password)
        } else {
            credentialsStorage.clearCredentials()
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory {
            val appContext = context.applicationContext
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val auth = FirebaseAuth.getInstance()
                    val firestore = FirebaseFirestore.getInstance()
                    val userRepository = UserRepository(firestore, auth)
                    val credentialsStorage = CredentialsStorage(appContext)
                    return LoginViewModel(auth, userRepository, credentialsStorage) as T
                }
            }
        }
    }
}
