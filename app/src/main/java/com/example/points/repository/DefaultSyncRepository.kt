package com.example.points.repository

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.example.points.database.entity.FavoritePOI
import com.example.points.models.CategoriaPOI
import com.example.points.models.PointOfInterest
import com.example.points.network.CachedPOIDto
import com.example.points.network.FavoritePOIDto
import com.example.points.network.SearchHistoryDto
import com.example.points.network.SyncApiService
import com.example.points.network.SyncRequest
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Implementación del repositorio de sincronización
 */
class DefaultSyncRepository(
    private val syncApiService: SyncApiService,
    private val localPOIRepository: LocalPOIRepository,
    private val context: Context
) : SyncRepository {
    
    companion object {
        private const val TAG = "DefaultSyncRepository"
        private const val PREFS_NAME = "sync_preferences"
        private const val KEY_LAST_SYNC_TIMESTAMP = "last_sync_timestamp"
        private val ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT
    }
    
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val auth = FirebaseAuth.getInstance()
    
    /**
     * Obtiene el ID único del dispositivo
     */
    private fun getDeviceId(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown_device"
    }
    
    /**
     * Obtiene el ID del usuario actual desde Firebase Auth
     */
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    override suspend fun pushChanges(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔄 [PUSH] Iniciando push de cambios para usuario: $userId")
            
            // Verificar que el usuario esté autenticado
            val currentUser = auth.currentUser
            if (currentUser == null) {
                val errorMsg = "❌ [PUSH] Usuario no autenticado"
                Log.e(TAG, errorMsg)
                return@withContext Result.failure(Exception(errorMsg))
            }
            
            Log.d(TAG, "✅ [PUSH] Usuario autenticado: ${currentUser.uid}")
            
            // Obtener favoritos locales como lista
            val favoritesList = localPOIRepository.getAllFavoritesList()
            
            Log.d(TAG, "📦 [PUSH] Obtenidos ${favoritesList.size} favoritos locales")
            
            if (favoritesList.isEmpty()) {
                Log.w(TAG, "⚠️ [PUSH] No hay favoritos locales para sincronizar")
                // Aún así enviar request vacío para mantener sincronización
            } else {
                favoritesList.forEach { poi ->
                    Log.d(TAG, "   📍 Favorito local: ${poi.nombre} (ID: ${poi.id})")
                }
            }
            
            // Convertir a DTOs
            val favoriteDtos = favoritesList.map { poi ->
                val dto = poi.toFavoritePOIDto()
                Log.d(TAG, "   🔄 Convertido a DTO: ${dto.nombre} (userId: ${dto.userId}, poiId: ${dto.poiId})")
                dto
            }
            
            // Crear request según la estructura del backend
            // El backend espera solo una lista de favoritos
            val request = SyncRequest(
                favorites = favoriteDtos
            )
            
            Log.d(TAG, "📋 [PUSH] Request creado con ${favoriteDtos.size} favoritos")
            
            Log.d(TAG, "📤 [PUSH] Enviando request con ${favoriteDtos.size} favoritos al servidor...")
            
            // Enviar al servidor
            val response = syncApiService.pushChanges(request)
            
            if (response.isSuccessful) {
                Log.d(TAG, "✅ [PUSH] Push completado exitosamente (código: ${response.code()})")
                Log.d(TAG, "✅ [PUSH] Se enviaron ${favoriteDtos.size} favoritos al servidor")
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Sin detalles"
                val errorMsg = when (response.code()) {
                    403 -> "HTTP 403"
                    401 -> "HTTP 401"
                    else -> "HTTP ${response.code()}"
                }
                Log.e(TAG, "❌ [PUSH] Error HTTP ${response.code()}: ${response.message()}\nBody: $errorBody")
                Log.e(TAG, "❌ [PUSH] URL del request: ${response.raw().request.url}")
                Result.failure(Exception(errorMsg, HttpException(response)))
            }
            
        } catch (e: java.net.UnknownHostException) {
            val errorMsg = "❌ [PUSH] Error de conexión: No se pudo resolver el host del servidor"
            Log.e(TAG, errorMsg, e)
            Result.failure(Exception(errorMsg, e))
        } catch (e: java.net.ConnectException) {
            val errorMsg = "❌ [PUSH] Error de conexión: No se pudo conectar al servidor"
            Log.e(TAG, errorMsg, e)
            Result.failure(Exception(errorMsg, e))
        } catch (e: java.io.IOException) {
            val errorMsg = "❌ [PUSH] Error de red: ${e.message}"
            Log.e(TAG, errorMsg, e)
            Result.failure(Exception(errorMsg, e))
        } catch (e: retrofit2.HttpException) {
            val errorMsg = when (e.code()) {
                403 -> "HTTP 403"
                401 -> "HTTP 401"
                else -> "HTTP ${e.code()}"
            }
            Log.e(TAG, "❌ [PUSH] Error HTTP ${e.code()}: ${e.message()}", e)
            Log.e(TAG, "❌ [PUSH] Response body: ${e.response()?.errorBody()?.string()}")
            Result.failure(Exception(errorMsg, e))
        } catch (e: Exception) {
            val errorMsg = "❌ [PUSH] Error inesperado: ${e.javaClass.simpleName} - ${e.message}"
            Log.e(TAG, errorMsg, e)
            e.printStackTrace()
            Result.failure(Exception(errorMsg, e))
        }
    }
    
    override suspend fun pullChanges(userId: String): Result<SyncResult> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔄 [PULL] Iniciando pull de cambios para usuario: $userId")
            
            // Verificar que el usuario esté autenticado
            val currentUser = auth.currentUser
            if (currentUser == null) {
                val errorMsg = "❌ [PULL] Usuario no autenticado"
                Log.e(TAG, errorMsg)
                return@withContext Result.failure(Exception(errorMsg))
            }
            
            Log.d(TAG, "✅ [PULL] Usuario autenticado: ${currentUser.uid}")
            
            val lastSyncAt = getLastSyncTimestamp()
            Log.d(TAG, "⏰ [PULL] Última sincronización: ${lastSyncAt ?: "Nunca"}")
            
            Log.d(TAG, "📥 [PULL] Solicitando cambios del servidor...")
            val response = syncApiService.pullChanges(
                userId = userId,
                lastSyncAt = lastSyncAt ?: ""
            )
            
            if (response.isSuccessful) {
                val syncResponse = response.body()
                if (syncResponse != null) {
                    Log.d(TAG, "✅ [PULL] Pull completado exitosamente (código: ${response.code()})")
                    Log.d(TAG, "📦 [PULL] Recibidos ${syncResponse.favorites.size} favoritos del servidor")
                    
                    // Procesar favoritos recibidos
                    var added = 0
                    var updated = 0
                    var removed = 0
                    
                    for (favoriteDto in syncResponse.favorites) {
                        val poi = favoriteDto.toPointOfInterest()
                        
                        // Agregar o actualizar según isFavorite
                        if (favoriteDto.isFavorite) {
                            val existing = localPOIRepository.isFavorite(poi.id)
                            if (existing) {
                                // Actualizar: eliminar y volver a agregar
                                localPOIRepository.removeFromFavorites(poi.id)
                                localPOIRepository.addToFavorites(poi)
                                updated++
                            } else {
                                localPOIRepository.addToFavorites(poi)
                                added++
                            }
                        } else {
                            // Si isFavorite es false, eliminar
                            localPOIRepository.removeFromFavorites(poi.id)
                            removed++
                        }
                    }
                    
                    // Guardar timestamp de sincronización
                    val serverTimestamp = syncResponse.serverTimestamp ?: Instant.now().toString()
                    saveLastSyncTimestamp(serverTimestamp)
                    
                    val result = SyncResult(
                        serverTimestamp = serverTimestamp,
                        favoritesAdded = added,
                        favoritesUpdated = updated,
                        favoritesRemoved = removed,
                        message = "Pull: $added nuevos, $updated actualizados, $removed eliminados"
                    )
                    
                    Log.d(TAG, "📊 [PULL] Resumen: $added nuevos, $updated actualizados, $removed eliminados")
                    Log.d(TAG, result.message)
                    Result.success(result)
                } else {
                    val errorMsg = "❌ [PULL] Respuesta vacía del servidor"
                    Log.e(TAG, errorMsg)
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Sin detalles"
                val errorMsg = when (response.code()) {
                    403 -> "HTTP 403"
                    401 -> "HTTP 401"
                    else -> "HTTP ${response.code()}"
                }
                Log.e(TAG, "❌ [PULL] Error HTTP ${response.code()}: ${response.message()}\nBody: $errorBody")
                Log.e(TAG, "❌ [PULL] URL del request: ${response.raw().request.url}")
                Result.failure(Exception(errorMsg, retrofit2.HttpException(response)))
            }
            
        } catch (e: java.net.UnknownHostException) {
            val errorMsg = "❌ [PULL] Error de conexión: No se pudo resolver el host del servidor"
            Log.e(TAG, errorMsg, e)
            Result.failure(Exception(errorMsg, e))
        } catch (e: java.net.ConnectException) {
            val errorMsg = "❌ [PULL] Error de conexión: No se pudo conectar al servidor"
            Log.e(TAG, errorMsg, e)
            Result.failure(Exception(errorMsg, e))
        } catch (e: java.io.IOException) {
            val errorMsg = "❌ [PULL] Error de red: ${e.message}"
            Log.e(TAG, errorMsg, e)
            Result.failure(Exception(errorMsg, e))
        } catch (e: retrofit2.HttpException) {
            val errorMsg = when (e.code()) {
                403 -> "HTTP 403"
                401 -> "HTTP 401"
                else -> "HTTP ${e.code()}"
            }
            Log.e(TAG, "❌ [PULL] Error HTTP ${e.code()}: ${e.message()}", e)
            Log.e(TAG, "❌ [PULL] Response body: ${e.response()?.errorBody()?.string()}")
            Result.failure(Exception(errorMsg, e))
        } catch (e: Exception) {
            val errorMsg = "❌ [PULL] Error inesperado: ${e.javaClass.simpleName} - ${e.message}"
            Log.e(TAG, errorMsg, e)
            e.printStackTrace()
            Result.failure(Exception(errorMsg, e))
        }
    }
    
    override suspend fun sync(userId: String): Result<SyncResult> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "═══════════════════════════════════════")
            Log.d(TAG, "🔄 [SYNC] Iniciando sincronización completa para usuario: $userId")
            Log.d(TAG, "═══════════════════════════════════════")
            
            // 1. Primero hacer pull para obtener cambios del servidor
            Log.d(TAG, "📥 [SYNC] Paso 1: Obteniendo cambios del servidor (PULL)...")
            val pullResult = pullChanges(userId)
            
            if (pullResult.isFailure) {
                val pullError = pullResult.exceptionOrNull()?.message ?: "Error desconocido"
                Log.w(TAG, "⚠️ [SYNC] Pull falló: $pullError")
                Log.w(TAG, "⚠️ [SYNC] Continuando con push...")
            } else {
                Log.d(TAG, "✅ [SYNC] Pull completado exitosamente")
            }
            
            // 2. Luego hacer push para enviar cambios locales
            Log.d(TAG, "📤 [SYNC] Paso 2: Enviando cambios locales al servidor (PUSH)...")
            val pushResult = pushChanges(userId)
            
            if (pushResult.isFailure) {
                val pushError = pushResult.exceptionOrNull()?.message ?: "Error desconocido"
                Log.w(TAG, "⚠️ [SYNC] Push falló: $pushError")
            } else {
                Log.d(TAG, "✅ [SYNC] Push completado exitosamente")
            }
            
            // Obtener información del push para el mensaje
            val pushCount = try {
                localPOIRepository.getAllFavoritesList().size
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ [SYNC] No se pudo obtener conteo de favoritos para push: ${e.message}")
                0
            }
            
            // Retornar el resultado del pull (o éxito si ambos funcionaron)
            when {
                pullResult.isSuccess -> {
                    val pullResultData = pullResult.getOrNull()
                    val pullAdded = pullResultData?.favoritesAdded ?: 0
                    val pullUpdated = pullResultData?.favoritesUpdated ?: 0
                    val pullRemoved = pullResultData?.favoritesRemoved ?: 0
                    
                    Log.d(TAG, "✅ [SYNC] Sincronización completada: Pull exitoso")
                    Log.d(TAG, "   📤 Push: $pushCount favoritos enviados")
                    Log.d(TAG, "   📥 Pull: $pullAdded nuevos, $pullUpdated actualizados, $pullRemoved eliminados")
                    
                    // Actualizar mensaje para incluir información del push
                    val updatedMessage = buildString {
                        append("Push: $pushCount favoritos enviados. ")
                        append("Pull: $pullAdded nuevos, $pullUpdated actualizados, $pullRemoved eliminados")
                    }
                    
                    Result.success(
                        SyncResult(
                            serverTimestamp = pullResultData?.serverTimestamp ?: Instant.now().toString(),
                            favoritesAdded = pullAdded,
                            favoritesUpdated = pullUpdated,
                            favoritesRemoved = pullRemoved,
                            message = updatedMessage
                        )
                    )
                }
                pushResult.isSuccess -> {
                    Log.d(TAG, "⚠️ [SYNC] Sincronización parcial: Push exitoso, Pull falló")
                    Result.success(
                        SyncResult(
                            serverTimestamp = Instant.now().toString(),
                            message = "Sincronización parcial: push exitoso, pull falló"
                        )
                    )
                }
                else -> {
                    val pullError = pullResult.exceptionOrNull()?.message ?: "Error desconocido en pull"
                    val pushError = pushResult.exceptionOrNull()?.message ?: "Error desconocido en push"
                    val errorMsg = "Tanto pull como push fallaron.\nPull: $pullError\nPush: $pushError"
                    Log.e(TAG, "❌ [SYNC] $errorMsg")
                    Log.d(TAG, "═══════════════════════════════════════")
                    Result.failure(Exception(errorMsg))
                }
            }
            
        } catch (e: Exception) {
            val errorMsg = "❌ [SYNC] Error inesperado en sincronización completa: ${e.message}"
            Log.e(TAG, errorMsg, e)
            e.printStackTrace()
            Result.failure(Exception(errorMsg, e))
        }
    }
    
    override suspend fun getLastSyncTimestamp(): String? = withContext(Dispatchers.IO) {
        prefs.getString(KEY_LAST_SYNC_TIMESTAMP, null)
    }
    
    override suspend fun saveLastSyncTimestamp(timestamp: String): Unit = withContext(Dispatchers.IO) {
        prefs.edit().putString(KEY_LAST_SYNC_TIMESTAMP, timestamp).apply()
        Log.d(TAG, "Timestamp de sincronización guardado: $timestamp")
    }
    
    // ========== Conversiones ==========
    
    /**
     * Convierte PointOfInterest local a FavoritePOIDto para el backend
     * Estructura basada en la entidad FavoritePOI del backend
     * El backend requiere todos los campos, especialmente 'nombre' que es not null
     */
    private fun PointOfInterest.toFavoritePOIDto(): FavoritePOIDto {
        val userId = getCurrentUserId() ?: "unknown"
        val timestamp = Instant.now().toString() // ISO-8601 format
        
        return FavoritePOIDto(
            userId = userId,
            poiId = id,
            nombre = nombre.ifEmpty { "Sin nombre" }, // Asegurar que no sea null o vacío
            descripcion = descripcion.ifEmpty { null },
            categoria = categoria.name,
            direccion = direccion.ifEmpty { null },
            lat = ubicacion.lat,
            lon = ubicacion.lon,
            calificacion = calificacion.takeIf { it > 0 },
            imagenUrl = imagenes.firstOrNull(),
            isFavorite = true,
            timestamp = timestamp
        )
    }
    
    /**
     * Convierte FavoritePOIDto del backend a PointOfInterest local
     */
    private fun FavoritePOIDto.toPointOfInterest(): PointOfInterest {
        return PointOfInterest(
            id = poiId,
            nombre = nombre.ifEmpty { "Sin nombre" },
            descripcion = descripcion ?: "",
            categoria = categoria?.let { 
                CategoriaPOI.values().find { cat -> cat.name == it } ?: CategoriaPOI.OTRO
            } ?: CategoriaPOI.OTRO,
            ubicacion = com.example.points.models.Ubicacion(
                lat = lat ?: 0.0,
                lon = lon ?: 0.0,
                direccion = direccion ?: ""
            ),
            direccion = direccion ?: "",
            imagenes = imagenUrl?.let { listOf(it) } ?: emptyList(),
            calificacion = calificacion ?: 0.0
        )
    }
}

