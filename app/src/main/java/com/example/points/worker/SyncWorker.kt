package com.example.points.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.points.PointsApplication
import com.example.points.repository.SyncRepository
import com.google.firebase.auth.FirebaseAuth

/**
 * Worker para sincronización automática en background
 * 
 * Este worker se ejecuta periódicamente para sincronizar
 * los datos locales con el backend Spring Boot.
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        private const val TAG = "SyncWorker"
        const val WORK_NAME = "sync_work"
    }
    
    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "🔄 Iniciando sincronización automática...")
            
            // Verificar si hay usuario autenticado
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Log.w(TAG, "⚠️ Usuario no autenticado, cancelando sincronización")
                return Result.success() // No es un error, simplemente no hay nada que sincronizar
            }
            
            // Obtener el repositorio de sincronización desde el AppContainer
            val app = applicationContext as? PointsApplication
            val syncRepository = app?.container?.syncRepository
            
            if (syncRepository == null) {
                Log.e(TAG, "❌ SyncRepository no disponible")
                return Result.failure()
            }
            
            // Realizar sincronización
            val userId = currentUser.uid
            val syncResult = syncRepository.sync(userId)
            
            syncResult.onSuccess { result ->
                Log.d(TAG, "✅ Sincronización automática completada:")
                Log.d(TAG, "   - Favoritos agregados: ${result.favoritesAdded}")
                Log.d(TAG, "   - Favoritos actualizados: ${result.favoritesUpdated}")
                Log.d(TAG, "   - Favoritos eliminados: ${result.favoritesRemoved}")
                Log.d(TAG, "   - Mensaje: ${result.message}")
            }.onFailure { error ->
                Log.e(TAG, "❌ Error en sincronización automática: ${error.message}", error)
            }
            
            // Retornar resultado basado en si fue exitoso o no
            return if (syncResult.isSuccess) {
                Result.success()
            } else {
                val error = syncResult.exceptionOrNull()
                if (error is java.net.UnknownHostException || 
                    error is java.net.ConnectException) {
                    // Error de red, reintentar más tarde
                    Result.retry()
                } else {
                    // Otro tipo de error
                    Result.failure()
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error inesperado en SyncWorker", e)
            Result.failure()
        }
    }
}

