package com.example.points.network

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Servicio de API para sincronización con el backend Spring Boot
 * Documentación: Ver INDICACIONES_BACKEND_SPRINGBOOT_POSTGRES.md
 */
interface SyncApiService {
    /**
     * Envía cambios del cliente al servidor
     * @param request Request con los datos a sincronizar
     * @return Response vacío si es exitoso
     */
    @POST("api/v1/sync/push")
    suspend fun pushChanges(@Body request: SyncRequest): Response<Unit>
    
    /**
     * Obtiene cambios del servidor desde la última sincronización
     * @param userId ID del usuario
     * @param lastSyncAt Fecha de la última sincronización en formato ISO 8601
     * @return Response con los cambios del servidor
     */
    @GET("api/v1/sync/pull")
    suspend fun pullChanges(
        @Query("userId") userId: String,
        @Query("lastSyncAt") lastSyncAt: String
    ): Response<SyncResponse>
}

/**
 * Request para sincronización push
 * Estructura basada en la entidad FavoritePOI del backend
 */
@Serializable
data class SyncRequest(
    val favorites: List<FavoritePOIDto> = emptyList()
)

/**
 * Response de sincronización pull
 * Nota: Si el backend no tiene endpoint de pull, esta estructura puede no ser necesaria
 */
@Serializable
data class SyncResponse(
    val serverTimestamp: String? = null,
    val favorites: List<FavoritePOIDto> = emptyList()
)

/**
 * DTO para POI favorito
 * Estructura basada en la entidad FavoritePOI del backend Spring Boot
 * El backend espera todos los campos de la entidad FavoritePOI
 */
@Serializable
data class FavoritePOIDto(
    val userId: String,
    val poiId: String,
    val nombre: String, // Campo requerido (not null en backend)
    val descripcion: String? = null,
    val categoria: String? = null,
    val direccion: String? = null,
    val lat: Double? = null,
    val lon: Double? = null,
    val calificacion: Double? = null,
    val imagenUrl: String? = null,
    val isFavorite: Boolean = true,
    val timestamp: String? = null // ISO-8601 format
)

/**
 * DTO para POI en caché
 */
@Serializable
data class CachedPOIDto(
    val poiId: String,
    val userId: String,
    val data: String, // JSON serializado
    val cachedAt: String? = null
)

/**
 * DTO para historial de búsqueda
 */
@Serializable
data class SearchHistoryDto(
    val id: String? = null,
    val userId: String,
    val query: String,
    val timestamp: String? = null
)

