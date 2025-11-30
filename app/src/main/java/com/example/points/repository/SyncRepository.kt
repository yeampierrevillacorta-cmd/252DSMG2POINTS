package com.example.points.repository

import com.example.points.models.PointOfInterest

/**
 * Repositorio para sincronización con el backend Spring Boot
 */
interface SyncRepository {
    /**
     * Sincroniza los cambios locales con el servidor (push)
     * @param userId ID del usuario actual
     * @return Result indicando éxito o fallo
     */
    suspend fun pushChanges(userId: String): Result<Unit>
    
    /**
     * Obtiene los cambios del servidor desde la última sincronización (pull)
     * @param userId ID del usuario actual
     * @return Result con los cambios del servidor o error
     */
    suspend fun pullChanges(userId: String): Result<SyncResult>
    
    /**
     * Realiza una sincronización completa (pull + merge + push)
     * @param userId ID del usuario actual
     * @return Result indicando éxito o fallo
     */
    suspend fun sync(userId: String): Result<SyncResult>
    
    /**
     * Obtiene la fecha de la última sincronización
     * @return Timestamp de la última sincronización o null si nunca se ha sincronizado
     */
    suspend fun getLastSyncTimestamp(): String?
    
    /**
     * Guarda la fecha de la última sincronización
     */
    suspend fun saveLastSyncTimestamp(timestamp: String)
}

/**
 * Resultado de una operación de sincronización
 */
data class SyncResult(
    val serverTimestamp: String,
    val favoritesAdded: Int = 0,
    val favoritesUpdated: Int = 0,
    val favoritesRemoved: Int = 0,
    val cachedAdded: Int = 0,
    val message: String = "Sincronización completada"
)

