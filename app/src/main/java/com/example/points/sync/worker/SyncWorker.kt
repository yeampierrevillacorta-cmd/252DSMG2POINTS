package com.example.points.sync.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.points.database.PointsDatabase
import com.example.points.database.entity.CachedPOI
import com.example.points.database.entity.FavoritePOI
import com.example.points.database.entity.SearchHistory
import com.example.points.sync.data.SyncPreferences
import com.example.points.sync.model.CachedPOIDto
import com.example.points.sync.model.FavoritePOIDto
import com.example.points.sync.model.SearchHistoryDto
import com.example.points.sync.model.SyncRequest
import com.example.points.sync.repository.RemoteSyncRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.first

/**
 * Worker de WorkManager para sincronización en segundo plano
 * Implementa la Unidad 7 de Android Basics: WorkManager
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        private const val TAG = "SyncWorker"
        private val ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    }
    
    private val database = PointsDatabase.getDatabase(context)
    private val syncPreferences = SyncPreferences(context)
    private val auth = FirebaseAuth.getInstance()
    
    // Obtener repositorio desde AppContainer
    private val remoteSyncRepository: RemoteSyncRepository by lazy {
        val application = context.applicationContext as com.example.points.PointsApplication
        application.container.remoteSyncRepository
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Iniciando sincronización en segundo plano...")
            
            // Verificar si la sincronización está habilitada
            val syncEnabled = syncPreferences.getSyncEnabled()
            if (!syncEnabled) {
                Log.d(TAG, "Sincronización deshabilitada, cancelando trabajo")
                return@withContext Result.success()
            }
            
            // Verificar autenticación
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.w(TAG, "Usuario no autenticado, cancelando sincronización")
                return@withContext Result.retry() // Reintentar más tarde
            }
            
            val userId = currentUser.uid
            val deviceId = android.provider.Settings.Secure.getString(
                applicationContext.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            ) ?: "unknown"
            
            // Obtener última sincronización
            val lastSyncTimestamp = syncPreferences.lastSyncTimestamp.first()
            val lastSyncAt = if (lastSyncTimestamp > 0) {
                LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(lastSyncTimestamp),
                    ZoneId.systemDefault()
                ).format(ISO_FORMATTER)
            } else {
                null
            }
            
            // 1. PULL: Obtener cambios del servidor
            Log.d(TAG, "Obteniendo cambios del servidor...")
            val pullResult = remoteSyncRepository.pullChanges(userId, lastSyncAt)
            
            pullResult.fold(
                onSuccess = { syncResponse ->
                    Log.d(TAG, "Cambios recibidos del servidor")
                    
                    // Procesar favoritos recibidos
                    syncResponse.favorites?.let { favorites ->
                        processFavoritesFromServer(favorites, userId)
                    }
                    
                    // Procesar caché recibido
                    syncResponse.cached?.let { cached ->
                        processCachedFromServer(cached, userId)
                    }
                    
                    // Procesar historial recibido
                    syncResponse.searchHistory?.let { history ->
                        processSearchHistoryFromServer(history, userId)
                    }
                    
                    // 2. PUSH: Enviar cambios locales al servidor
                    Log.d(TAG, "Enviando cambios locales al servidor...")
                    val localChanges = prepareLocalChanges(userId, deviceId, lastSyncAt)
                    val pushResult = remoteSyncRepository.pushChanges(localChanges)
                    
                    pushResult.fold(
                        onSuccess = {
                            Log.d(TAG, "Sincronización completada exitosamente")
                            
                            // Actualizar timestamp de última sincronización
                            val serverTimestamp = syncResponse.serverTimestamp
                            if (serverTimestamp.isNotEmpty()) {
                                try {
                                    val timestamp = LocalDateTime.parse(serverTimestamp, ISO_FORMATTER)
                                    val epochMillis = timestamp.atZone(ZoneId.systemDefault())
                                        .toInstant().toEpochMilli()
                                    syncPreferences.updateLastSyncTimestamp(epochMillis)
                                } catch (e: Exception) {
                                    Log.w(TAG, "Error al parsear timestamp del servidor", e)
                                    syncPreferences.updateLastSyncTimestamp()
                                }
                            } else {
                                syncPreferences.updateLastSyncTimestamp()
                            }
                            
                            Result.success()
                        },
                        onFailure = { error ->
                            Log.e(TAG, "Error al enviar cambios al servidor", error)
                            // Aún consideramos éxito parcial si recibimos cambios
                            Result.success()
                        }
                    )
                },
                onFailure = { error ->
                    Log.e(TAG, "Error al obtener cambios del servidor", error)
                    // Reintentar si es un error de red
                    if (error is java.net.UnknownHostException || 
                        error is java.net.ConnectException) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado en sincronización", e)
            Result.failure()
        }
    }
    
    /**
     * Procesar favoritos recibidos del servidor
     */
    private suspend fun processFavoritesFromServer(
        favorites: List<FavoritePOIDto>,
        userId: String
    ) = withContext(Dispatchers.IO) {
        val favoriteDao = database.favoritePOIDao()
        
        favorites.forEach { dto ->
            try {
                if (dto.deleted) {
                    // Eliminar si está marcado como eliminado
                    favoriteDao.deleteFavoriteById(dto.poiId)
                } else {
                    // Insertar o actualizar
                    val favorite = FavoritePOI(
                        poiId = dto.poiId,
                        nombre = dto.nombre,
                        descripcion = dto.descripcion,
                        categoria = dto.categoria,
                        direccion = dto.direccion,
                        lat = dto.lat,
                        lon = dto.lon,
                        calificacion = dto.calificacion,
                        imagenUrl = dto.imagenUrl,
                        fechaAgregado = parseDateTime(dto.createdAt) ?: System.currentTimeMillis(),
                        fechaActualizacion = parseDateTime(dto.updatedAt) ?: System.currentTimeMillis()
                    )
                    favoriteDao.insertFavorite(favorite)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al procesar favorito ${dto.poiId}", e)
            }
        }
    }
    
    /**
     * Procesar caché recibido del servidor
     */
    private suspend fun processCachedFromServer(
        cached: List<CachedPOIDto>,
        userId: String
    ) = withContext(Dispatchers.IO) {
        val cachedDao = database.cachedPOIDao()
        
        cached.forEach { dto ->
            try {
                val cachedPOI = CachedPOI(
                    poiId = dto.poiId,
                    nombre = dto.nombre,
                    descripcion = dto.descripcion,
                    categoria = dto.categoria,
                    direccion = dto.direccion,
                    lat = dto.lat,
                    lon = dto.lon,
                    calificacion = dto.calificacion,
                    totalCalificaciones = 0,
                    imagenUrl = dto.imagenUrl,
                    estado = "APROBADO",
                    fechaCreacionMillis = parseDateTime(dto.cachedAt) ?: System.currentTimeMillis(),
                    fechaActualizacionMillis = System.currentTimeMillis(),
                    fechaCache = parseDateTime(dto.cachedAt) ?: System.currentTimeMillis(),
                    jsonData = "" // Se puede mejorar guardando el JSON completo
                )
                cachedDao.insertCachedPOI(cachedPOI)
            } catch (e: Exception) {
                Log.e(TAG, "Error al procesar caché ${dto.poiId}", e)
            }
        }
    }
    
    /**
     * Procesar historial recibido del servidor
     */
    private suspend fun processSearchHistoryFromServer(
        history: List<SearchHistoryDto>,
        userId: String
    ) = withContext(Dispatchers.IO) {
        val searchDao = database.searchHistoryDao()
        
        history.forEach { dto ->
            try {
                if (!dto.deleted) {
                    val searchHistory = SearchHistory(
                        id = dto.id ?: 0L,
                        query = dto.searchQuery,
                        category = dto.searchType,
                        fechaBusqueda = parseDateTime(dto.createdAt) ?: System.currentTimeMillis(),
                        resultados = 0
                    )
                    searchDao.insertSearch(searchHistory)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al procesar historial ${dto.id}", e)
            }
        }
    }
    
    /**
     * Preparar cambios locales para enviar al servidor
     */
    private suspend fun prepareLocalChanges(
        userId: String,
        deviceId: String,
        lastSyncAt: String?
    ): SyncRequest = withContext(Dispatchers.IO) {
        val favoriteDao = database.favoritePOIDao()
        val cachedDao = database.cachedPOIDao()
        val searchDao = database.searchHistoryDao()
        
        // Obtener favoritos locales
        val localFavorites = favoriteDao.getAllFavorites().first()
            .map { favorite ->
                FavoritePOIDto(
                    poiId = favorite.poiId,
                    userId = userId,
                    nombre = favorite.nombre,
                    descripcion = favorite.descripcion,
                    categoria = favorite.categoria,
                    direccion = favorite.direccion,
                    lat = favorite.lat,
                    lon = favorite.lon,
                    calificacion = favorite.calificacion,
                    imagenUrl = favorite.imagenUrl,
                    createdAt = formatDateTime(favorite.fechaAgregado),
                    updatedAt = formatDateTime(favorite.fechaActualizacion),
                    deleted = false
                )
            }
        
        // Obtener caché local (últimos 50)
        val localCached = cachedDao.getCachedPOIs(50).first()
            .map { cached ->
                CachedPOIDto(
                    poiId = cached.poiId,
                    userId = userId,
                    nombre = cached.nombre,
                    descripcion = cached.descripcion,
                    categoria = cached.categoria,
                    direccion = cached.direccion,
                    lat = cached.lat,
                    lon = cached.lon,
                    calificacion = cached.calificacion,
                    imagenUrl = cached.imagenUrl,
                    cachedAt = formatDateTime(cached.fechaCache),
                    expiresAt = null
                )
            }
        
        // Obtener historial local (últimos 20)
        val localHistory = searchDao.getRecentSearches(20).first()
            .map { search ->
                SearchHistoryDto(
                    id = search.id,
                    userId = userId,
                    deviceId = deviceId,
                    searchQuery = search.query,
                    searchType = search.category,
                    latitude = null,
                    longitude = null,
                    createdAt = formatDateTime(search.fechaBusqueda),
                    deleted = false
                )
            }
        
        SyncRequest(
            deviceId = deviceId,
            userId = userId,
            lastSyncAt = lastSyncAt,
            favorites = localFavorites,
            cached = localCached,
            searchHistory = localHistory
        )
    }
    
    /**
     * Parsear fecha ISO a timestamp
     */
    private fun parseDateTime(isoString: String?): Long? {
        if (isoString == null || isoString.isEmpty()) return null
        return try {
            val dateTime = LocalDateTime.parse(isoString, ISO_FORMATTER)
            dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Formatear timestamp a ISO string
     */
    private fun formatDateTime(timestamp: Long): String {
        return try {
            val dateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp),
                ZoneId.systemDefault()
            )
            dateTime.format(ISO_FORMATTER)
        } catch (e: Exception) {
            ""
        }
    }
}

