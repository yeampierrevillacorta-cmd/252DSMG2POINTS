package com.example.points.utils

import android.util.Log
import com.example.points.models.TipoUsuario
import com.example.points.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object TestUserCreator {
    
    /**
     * Crea usuarios de prueba con diferentes tipos
     */
    suspend fun createTestUsers() {
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        
        try {
            // Obtener el usuario actual autenticado
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.w("TestUserCreator", "No hay usuario autenticado")
                return
            }
            
            // Crear o actualizar el usuario actual como administrador para pruebas
            val adminUser = User(
                id = currentUser.uid,
                nombre = currentUser.displayName ?: "Admin Test",
                email = currentUser.email ?: "admin@test.com",
                tipo = TipoUsuario.ADMINISTRADOR,
                notificaciones = true,
                photoUrl = currentUser.photoUrl?.toString(),
                telefono = ""
            )
            
            // Guardar en Firestore
            firestore.collection("usuarios")
                .document(currentUser.uid)
                .set(adminUser)
                .await()
            
            Log.d("TestUserCreator", "Usuario administrador creado: ${adminUser.nombre}")
            
            // Crear usuarios de prueba adicionales
            val testUsers = listOf(
                User(
                    id = "ciudadano_test_1",
                    nombre = "Juan Pérez",
                    email = "juan@test.com",
                    tipo = TipoUsuario.CIUDADANO,
                    notificaciones = true,
                    telefono = "123456789"
                ),
                User(
                    id = "moderador_test_1",
                    nombre = "María González",
                    email = "maria@test.com",
                    tipo = TipoUsuario.MODERADOR,
                    notificaciones = true,
                    telefono = "987654321"
                ),
                User(
                    id = "admin_test_2",
                    nombre = "Carlos Admin",
                    email = "carlos@admin.com",
                    tipo = TipoUsuario.ADMINISTRADOR,
                    notificaciones = true,
                    telefono = "555123456"
                )
            )
            
            // Guardar usuarios de prueba
            testUsers.forEach { user ->
                firestore.collection("usuarios")
                    .document(user.id)
                    .set(user)
                    .await()
                
                Log.d("TestUserCreator", "Usuario de prueba creado: ${user.nombre} - ${user.tipo}")
            }
            
            Log.d("TestUserCreator", "Todos los usuarios de prueba creados exitosamente")
            
        } catch (e: Exception) {
            Log.e("TestUserCreator", "Error al crear usuarios de prueba", e)
        }
    }
    
    /**
     * Convierte el usuario actual en administrador (para pruebas)
     */
    suspend fun makeCurrentUserAdmin(): Boolean {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            
            if (currentUser == null) {
                Log.w("TestUserCreator", "No hay usuario autenticado")
                return false
            }
            
            // Actualizar solo el tipo de usuario
            firestore.collection("usuarios")
                .document(currentUser.uid)
                .update("tipo", TipoUsuario.ADMINISTRADOR.name)
                .await()
            
            Log.d("TestUserCreator", "Usuario actual convertido a administrador")
            true
        } catch (e: Exception) {
            Log.e("TestUserCreator", "Error al convertir usuario a administrador", e)
            false
        }
    }
    
    /**
     * Convierte el usuario actual en ciudadano (para pruebas)
     */
    suspend fun makeCurrentUserCitizen(): Boolean {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            
            if (currentUser == null) {
                Log.w("TestUserCreator", "No hay usuario autenticado")
                return false
            }
            
            // Actualizar solo el tipo de usuario
            firestore.collection("usuarios")
                .document(currentUser.uid)
                .update("tipo", TipoUsuario.CIUDADANO.name)
                .await()
            
            Log.d("TestUserCreator", "Usuario actual convertido a ciudadano")
            true
        } catch (e: Exception) {
            Log.e("TestUserCreator", "Error al convertir usuario a ciudadano", e)
            false
        }
    }
}
