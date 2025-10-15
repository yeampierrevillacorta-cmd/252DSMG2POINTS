package com.example.points.repository

import com.example.points.models.User
import com.example.points.models.TipoUsuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    /**
     * Obtiene el usuario actual desde Firebase
     */
    suspend fun getCurrentUser(): Result<User?> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Result.success(null)
            } else {
                val userDoc = firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()
                
                if (userDoc.exists()) {
                    val data = userDoc.data
                    val user = User(
                        id = userDoc.id,
                        nombre = data?.get("nombre") as? String ?: "Usuario",
                        email = data?.get("email") as? String ?: currentUser.email ?: "",
                        telefono = data?.get("telefono") as? String ?: "",
                        notificaciones = data?.get("notificaciones") as? Boolean ?: true,
                        tipo = parseTipoUsuario(data?.get("tipo") as? String),
                        photoUrl = data?.get("photoUrl") as? String ?: currentUser.photoUrl?.toString()
                    )
                    Result.success(user)
                } else {
                    // Si no existe en Firestore, crear un usuario básico
                    val basicUser = User(
                        id = currentUser.uid,
                        nombre = currentUser.displayName ?: "Usuario",
                        email = currentUser.email ?: "",
                        tipo = TipoUsuario.CIUDADANO, // Por defecto ciudadano
                        photoUrl = currentUser.photoUrl?.toString()
                    )
                    Result.success(basicUser)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Convierte el string del tipo de usuario a enum
     */
    private fun parseTipoUsuario(tipoString: String?): TipoUsuario {
        return when (tipoString) {
            "ADMINISTRADOR" -> TipoUsuario.ADMINISTRADOR
            "MODERADOR" -> TipoUsuario.MODERADOR
            "CIUDADANO" -> TipoUsuario.CIUDADANO
            else -> TipoUsuario.CIUDADANO // Por defecto ciudadano
        }
    }
    
    /**
     * Obtiene el tipo de usuario actual
     */
    suspend fun getCurrentUserType(): TipoUsuario {
        return try {
            val user = getCurrentUser().getOrNull()
            user?.tipo ?: TipoUsuario.CIUDADANO
        } catch (e: Exception) {
            TipoUsuario.CIUDADANO // Por defecto ciudadano si hay error
        }
    }
    
    /**
     * Verifica si el usuario actual es administrador
     */
    suspend fun isCurrentUserAdmin(): Boolean {
        return getCurrentUserType() == TipoUsuario.ADMINISTRADOR
    }
    
    /**
     * Verifica si el usuario actual es administrador o moderador
     */
    suspend fun isCurrentUserAdminOrModerator(): Boolean {
        val userType = getCurrentUserType()
        return userType == TipoUsuario.ADMINISTRADOR || userType == TipoUsuario.MODERADOR
    }
    
    /**
     * Flow que emite el usuario actual
     */
    fun getCurrentUserFlow(): Flow<User?> = flow {
        val user = getCurrentUser().getOrNull()
        emit(user)
    }
    
    /**
     * Verifica directamente desde Firestore si el usuario actual es administrador
     */
    suspend fun isCurrentUserAdminFromFirestore(): Boolean {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                false
            } else {
                val userDoc = firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()
                
                if (userDoc.exists()) {
                    val tipo = userDoc.data?.get("tipo") as? String
                    tipo == "ADMINISTRADOR"
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Verifica directamente desde Firestore si el usuario actual es administrador o moderador
     */
    suspend fun isCurrentUserAdminOrModeratorFromFirestore(): Boolean {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                false
            } else {
                val userDoc = firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()
                
                if (userDoc.exists()) {
                    val tipo = userDoc.data?.get("tipo") as? String
                    tipo == "ADMINISTRADOR" || tipo == "MODERADOR"
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Actualiza el tipo de usuario (solo para testing o admin functions)
     */
    suspend fun updateUserType(userId: String, newType: TipoUsuario): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .update("tipo", newType.name)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
