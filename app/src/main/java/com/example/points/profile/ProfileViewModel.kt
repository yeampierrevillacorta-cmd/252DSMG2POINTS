package com.example.points.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.points.models.TipoUsuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _profile = MutableStateFlow(UserProfile())
    val profile: StateFlow<UserProfile> = _profile

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val uid: String? get() = auth.currentUser?.uid

    init {
        loadProfile()
    }

    fun loadProfile() {
        val id = uid ?: return
        _isLoading.value = true
        
        firestore.collection("users").document(id).get()
            .addOnSuccessListener { doc ->
                _isLoading.value = false
                if (doc.exists()) {
                    val name = doc.getString("nombre") ?: ""
                    val phone = doc.getString("telefono") ?: ""
                    val email = doc.getString("email") ?: auth.currentUser?.email ?: ""
                    val tipoString = doc.getString("tipo") ?: "CIUDADANO"
                    val notificaciones = doc.getBoolean("notificaciones") ?: true
                    val photo = doc.getString("photoUrl")
                    
                    val tipo = when (tipoString) {
                        "ADMINISTRADOR" -> TipoUsuario.ADMINISTRADOR
                        "MODERADOR" -> TipoUsuario.MODERADOR
                        "CIUDADANO" -> TipoUsuario.CIUDADANO
                        else -> TipoUsuario.CIUDADANO
                    }
                    
                    _profile.value = UserProfile(
                        name = name,
                        phone = phone,
                        email = email,
                        tipo = tipo,
                        notificaciones = notificaciones,
                        photoUrl = photo
                    )
                } else {
                    _errorMessage.value = "No se encontró el perfil del usuario"
                }
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _errorMessage.value = "Error al cargar perfil: ${e.message}"
            }
    }

    fun updateProfile(name: String, phone: String, photoUri: Uri?, onSuccess: () -> Unit) {
        val id = uid ?: return
        _isLoading.value = true
        
        // Mantener todos los campos existentes y solo actualizar los que se modifican
        val currentProfile = _profile.value
        val updates = hashMapOf<String, Any>(
            "nombre" to name,
            "telefono" to phone,
            "email" to currentProfile.email,
            "tipo" to currentProfile.tipo.name,
            "notificaciones" to currentProfile.notificaciones
        )

        if (photoUri != null) {
            val ref = storage.reference.child("profile_images/$id.jpg")
            ref.putFile(photoUri).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { url ->
                    updates["photoUrl"] = url.toString()
                    firestore.collection("users").document(id).set(updates)
                        .addOnSuccessListener {
                            _isLoading.value = false
                            // Recargar el perfil completo desde Firestore
                            loadProfile()
                            onSuccess()
                        }
                        .addOnFailureListener {
                            _isLoading.value = false
                            _errorMessage.value = it.localizedMessage
                        }
                }.addOnFailureListener {
                    _isLoading.value = false
                    _errorMessage.value = it.localizedMessage
                }
            }.addOnFailureListener {
                _isLoading.value = false
                _errorMessage.value = it.localizedMessage
            }
        } else {
            firestore.collection("users").document(id).set(updates)
                .addOnSuccessListener {
                    _isLoading.value = false
                    // Recargar el perfil completo desde Firestore
                    loadProfile()
                    onSuccess()
                }
                .addOnFailureListener {
                    _isLoading.value = false
                    _errorMessage.value = it.localizedMessage
                }
        }
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }

    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val id = uid
        if (user == null || id == null) {
            onError("No hay usuario autenticado")
            return
        }
        // Nota: Firebase puede requerir re-autenticación antes de eliminar la cuenta.
        user.delete()
            .addOnSuccessListener {
                // eliminar datos en Firestore
                firestore.collection("users").document(id).delete()
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError(it.localizedMessage ?: "Error al eliminar datos") }
            }
            .addOnFailureListener {
                onError(it.localizedMessage ?: "No se pudo eliminar la cuenta (posible re-autenticación requerida)")
            }
    }
}
