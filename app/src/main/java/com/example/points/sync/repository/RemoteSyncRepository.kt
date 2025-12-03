package com.example.points.sync.repository

import android.util.Log
import com.example.points.sync.model.SyncRequest
import com.example.points.sync.model.SyncResponse
import com.example.points.sync.network.SyncApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Repositorio para sincronización remota con el backend
 * Implementa operaciones de push y pull
 */
class RemoteSyncRepository(
    private val syncApiService: SyncApiService
) {
    
    companion object {
        private const val TAG = "RemoteSyncRepository"
    }
    
    /**
     * Enviar cambios locales al servidor
     */
    suspend fun pushChanges(request: SyncRequest): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Enviando cambios al servidor...")
            val response = syncApiService.pushChanges(request)
            
            if (response.isSuccessful) {
                Log.d(TAG, "Cambios enviados exitosamente")
                Result.success(Unit)
            } else {
                val errorMsg = "Error HTTP ${response.code()}: ${response.message()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: HttpException) {
            val errorMsg = "Error HTTP al enviar cambios: ${e.code()}"
            Log.e(TAG, errorMsg, e)
            Result.failure(Exception(errorMsg, e))
        } catch (e: IOException) {
            val errorMsg = "Error de red al enviar cambios: ${e.message}"
            Log.e(TAG, errorMsg, e)
            Result.failure(Exception(errorMsg, e))
        } catch (e: Exception) {
            val errorMsg = "Error inesperado al enviar cambios: ${e.message}"
            Log.e(TAG, errorMsg, e)
            Result.failure(Exception(errorMsg, e))
        }
    }
    
    /**
     * Obtener cambios del servidor
     */
    suspend fun pullChanges(userId: String, lastSyncAt: String? = null): Result<SyncResponse> = 
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Obteniendo cambios del servidor para usuario: $userId")
                val response = syncApiService.pullChanges(userId, lastSyncAt)
                
                if (response.isSuccessful) {
                    val syncResponse = response.body()
                    if (syncResponse != null) {
                        Log.d(TAG, "Cambios obtenidos exitosamente")
                        Result.success(syncResponse)
                    } else {
                        val errorMsg = "Respuesta vacía del servidor"
                        Log.e(TAG, errorMsg)
                        Result.failure(Exception(errorMsg))
                    }
                } else {
                    val errorMsg = "Error HTTP ${response.code()}: ${response.message()}"
                    Log.e(TAG, errorMsg)
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: HttpException) {
                val errorMsg = "Error HTTP al obtener cambios: ${e.code()}"
                Log.e(TAG, errorMsg, e)
                Result.failure(Exception(errorMsg, e))
            } catch (e: IOException) {
                val errorMsg = "Error de red al obtener cambios: ${e.message}"
                Log.e(TAG, errorMsg, e)
                Result.failure(Exception(errorMsg, e))
            } catch (e: Exception) {
                val errorMsg = "Error inesperado al obtener cambios: ${e.message}"
                Log.e(TAG, errorMsg, e)
                Result.failure(Exception(errorMsg, e))
            }
        }
}

