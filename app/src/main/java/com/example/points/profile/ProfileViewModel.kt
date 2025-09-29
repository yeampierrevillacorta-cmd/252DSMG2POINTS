package com.example.points.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
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
        firestore.collection("users").document(id).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val name = doc.getString("name") ?: ""
                    val phone = doc.getString("phone") ?: ""
                    val photo = doc.getString("photoUrl")
                    _profile.value = UserProfile(name, phone, photo)
                }
            }
    }

    fun updateProfile(name: String, phone: String, photoUri: Uri?, onSuccess: () -> Unit) {
        val id = uid ?: return
        _isLoading.value = true
        val updates = hashMapOf<String, Any>("name" to name, "phone" to phone)

        if (photoUri != null) {
            val ref = storage.reference.child("profile_images/$id.jpg")
            ref.putFile(photoUri).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { url ->
                    updates["photoUrl"] = url.toString()
                    firestore.collection("users").document(id).set(updates)
                        .addOnSuccessListener {
                            _isLoading.value = false
                            _profile.value = UserProfile(name, phone, updates["photoUrl"] as? String)
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
                    _profile.value = UserProfile(name, phone, updates["photoUrl"] as? String)
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
