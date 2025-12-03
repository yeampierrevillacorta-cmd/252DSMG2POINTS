package com.example.points.repository

import com.example.points.models.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import android.util.Log

class NotificationRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val notificationsCollection = firestore.collection("notificaciones")
    
    // Obtener todas las notificaciones del usuario
    // Nota: Ordenamos en memoria para evitar requerir índice compuesto en Firestore
    fun getUserNotifications(userId: String): Flow<List<Notification>> = callbackFlow {
        val listener = notificationsCollection
            .whereEqualTo("usuarioId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("NotificationRepository", "Error getting notifications", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data
                        Notification(
                            id = doc.id,
                            tipo = com.example.points.models.TipoNotificacion.valueOf(
                                data?.get("tipo") as? String ?: "SISTEMA"
                            ),
                            mensaje = data?.get("mensaje") as? String ?: "",
                            fechaHora = data?.get("fechaHora") as? com.google.firebase.Timestamp 
                                ?: com.google.firebase.Timestamp.now(),
                            usuarioId = data?.get("usuarioId") as? String ?: "",
                            leida = data?.get("leida") as? Boolean ?: false,
                            incidenteId = data?.get("incidenteId") as? String,
                            eventoId = data?.get("eventoId") as? String
                        )
                    } catch (e: Exception) {
                        Log.e("NotificationRepository", "Error parsing notification ${doc.id}", e)
                        null
                    }
                }?.sortedByDescending { it.fechaHora } ?: emptyList() // Ordenar en memoria
                
                trySend(notifications)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Crear una nueva notificación
    suspend fun createNotification(notification: Notification): Result<String> {
        return try {
            val docRef = notificationsCollection.add(notification).await()
            Log.d("NotificationRepository", "Notificación creada: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error creating notification", e)
            Result.failure(e)
        }
    }
    
    // Marcar notificación como leída
    suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            notificationsCollection.document(notificationId)
                .update("leida", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error marking notification as read", e)
            Result.failure(e)
        }
    }
    
    // Marcar todas las notificaciones como leídas
    suspend fun markAllAsRead(userId: String): Result<Unit> {
        return try {
            val snapshot = notificationsCollection
                .whereEqualTo("usuarioId", userId)
                .whereEqualTo("leida", false)
                .get()
                .await()
            
            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, "leida", true)
            }
            batch.commit().await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error marking all as read", e)
            Result.failure(e)
        }
    }
    
    // Eliminar notificación
    suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            notificationsCollection.document(notificationId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error deleting notification", e)
            Result.failure(e)
        }
    }
    
    // Obtener conteo de notificaciones no leídas
    suspend fun getUnreadCount(userId: String): Result<Int> {
        return try {
            // Obtener todas las notificaciones del usuario y filtrar en memoria
            // para evitar requerir índice compuesto
            val snapshot = notificationsCollection
                .whereEqualTo("usuarioId", userId)
                .get()
                .await()
            
            val unreadCount = snapshot.documents.count { doc ->
                (doc.data?.get("leida") as? Boolean) == false
            }
            
            Result.success(unreadCount)
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error getting unread count", e)
            Result.failure(e)
        }
    }
}

