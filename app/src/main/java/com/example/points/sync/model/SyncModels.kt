package com.example.points.sync.model

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

/**
 * Modelos de datos para sincronización con el backend
 */

@Serializable
data class FavoritePOIDto(
    val poiId: String = "",
    val userId: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val direccion: String = "",
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val calificacion: Double = 0.0,
    val imagenUrl: String? = null,
    val createdAt: String? = null, // ISO DateTime string
    val updatedAt: String? = null, // ISO DateTime string
    val deleted: Boolean = false
)

@Serializable
data class CachedPOIDto(
    val poiId: String = "",
    val userId: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val direccion: String = "",
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val calificacion: Double = 0.0,
    val imagenUrl: String? = null,
    val cachedAt: String? = null, // ISO DateTime string
    val expiresAt: String? = null // ISO DateTime string
)

@Serializable
data class SearchHistoryDto(
    val id: Long? = null,
    val userId: String = "",
    val deviceId: String = "",
    val searchQuery: String = "",
    val searchType: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAt: String? = null, // ISO DateTime string
    val deleted: Boolean = false
)

@Serializable
data class SyncRequest(
    val deviceId: String,
    val userId: String,
    val lastSyncAt: String? = null, // ISO DateTime string
    val favorites: List<FavoritePOIDto>? = null,
    val cached: List<CachedPOIDto>? = null,
    val searchHistory: List<SearchHistoryDto>? = null
)

@Serializable
data class SyncResponse(
    val serverTimestamp: String, // ISO DateTime string
    val favorites: List<FavoritePOIDto>? = null,
    val cached: List<CachedPOIDto>? = null,
    val searchHistory: List<SearchHistoryDto>? = null
)

