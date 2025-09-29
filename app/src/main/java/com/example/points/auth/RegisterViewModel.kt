package com.example.points.auth

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.points.utils.PasswordUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RegisterViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun onNameChange(value: String) { _uiState.value = _uiState.value.copy(name = value) }
    fun onPhoneChange(value: String) { _uiState.value = _uiState.value.copy(phone = value) }
    fun onEmailChange(value: String) { _uiState.value = _uiState.value.copy(email = value) }
    fun onPasswordChange(value: String) { _uiState.value = _uiState.value.copy(password = value) }
    fun onConfirmPasswordChange(value: String) { _uiState.value = _uiState.value.copy(confirmPassword = value) }
    fun onPhotoSelected(uri: Uri?) { _uiState.value = _uiState.value.copy(photoUri = uri) }

    fun registerUser(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (state.name.isBlank() || state.phone.isBlank() || state.email.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Todos los campos son obligatorios")
            return
        }
        if (state.password != state.confirmPassword) {
            _uiState.value = state.copy(errorMessage = "Las contraseñas no coinciden")
            return
        }
        if (!PasswordUtils.isStrongPassword(state.password)) {
            _uiState.value = state.copy(errorMessage = "Contraseña débil (8+, mayúscula, número, símbolo)")
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        auth.createUserWithEmailAndPassword(state.email.trim(), state.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: run {
                        _uiState.value = state.copy(isLoading = false, errorMessage = "No se obtuvo uid")
                        return@addOnCompleteListener
                    }

                    val userMap = hashMapOf<String, Any>(
                        "name" to state.name,
                        "phone" to state.phone,
                        "email" to state.email.trim()
                    )

                    // Si hay foto -> subir primero
                    val photoUri = state.photoUri
                    if (photoUri != null) {
                        val ref = storage.reference.child("profile_images/$uid.jpg")
                        ref.putFile(photoUri)
                            .addOnSuccessListener {
                                ref.downloadUrl.addOnSuccessListener { url ->
                                    userMap["photoUrl"] = url.toString()
                                    firestore.collection("users").document(uid).set(userMap)
                                        .addOnSuccessListener {
                                            _uiState.value = state.copy(isLoading = false)
                                            onSuccess()
                                        }
                                        .addOnFailureListener { e ->
                                            _uiState.value = state.copy(isLoading = false, errorMessage = e.message)
                                        }
                                }.addOnFailureListener { e ->
                                    _uiState.value = state.copy(isLoading = false, errorMessage = e.message)
                                }
                            }
                            .addOnFailureListener { e ->
                                _uiState.value = state.copy(isLoading = false, errorMessage = e.message)
                            }
                    } else {
                        // sin foto
                        firestore.collection("users").document(uid).set(userMap)
                            .addOnSuccessListener {
                                _uiState.value = state.copy(isLoading = false)
                                onSuccess()
                            }
                            .addOnFailureListener { e ->
                                _uiState.value = state.copy(isLoading = false, errorMessage = e.message)
                            }
                    }

                } else {
                    _uiState.value = state.copy(isLoading = false, errorMessage = task.exception?.localizedMessage)
                }
            }
    }
}
