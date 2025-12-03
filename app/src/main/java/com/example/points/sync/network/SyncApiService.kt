package com.example.points.sync.network

import com.example.points.sync.model.SyncRequest
import com.example.points.sync.model.SyncResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Interfaz de API para sincronización con el backend
 * Implementa comunicación HTTP con Retrofit
 */
interface SyncApiService {
    
    /**
     * Enviar cambios locales al servidor (push)
     */
    @POST("api/v1/sync/push")
    suspend fun pushChanges(@Body request: SyncRequest): retrofit2.Response<Unit>
    
    /**
     * Obtener cambios del servidor (pull)
     */
    @GET("api/v1/sync/pull")
    suspend fun pullChanges(
        @Query("userId") userId: String,
        @Query("lastSyncAt") lastSyncAt: String? = null
    ): retrofit2.Response<SyncResponse>
}

