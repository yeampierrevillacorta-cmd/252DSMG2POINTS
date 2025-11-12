package com.example.points.repository

import com.example.points.database.PointsDatabase
import com.example.points.database.dao.CachedPOIDao
import com.example.points.database.dao.FavoritePOIDao
import com.example.points.database.entity.CachedPOI
import com.example.points.database.entity.FavoritePOI
import com.example.points.models.PointOfInterest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.util.Date

/**
 * Repositorio local para operaciones con Room
 * Implementa almacenamiento local para la Unidad 5 de Android Basics
 */
class LocalPOIRepository(private val database: PointsDatabase) {
    
    private val favoriteDao: FavoritePOIDao = database.favoritePOIDao()
    private val cachedDao: CachedPOIDao = database.cachedPOIDao()
    
    // Gson con adaptadores personalizados para Timestamp
    private val gson: Gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(Timestamp::class.java, TimestampAdapter())
            .create()
    }
    
    companion object {
        private const val TAG = "LocalPOIRepository"
    }
    
    // ========== POIs Favoritos ==========
    
    /**
     * Obtener todos los POIs favoritos
     */
    fun getAllFavorites(): Flow<List<PointOfInterest>> {
        return favoriteDao.getAllFavorites()
            .map { favorites ->
                favorites.map { it.toPointOfInterest() }
            }
            .flowOn(Dispatchers.IO)
    }
    
    /**
     * Verificar si un POI es favorito
     */
    suspend fun isFavorite(poiId: String): Boolean {
        return withContext(Dispatchers.IO) {
            favoriteDao.isFavorite(poiId)
        }
    }
    
    /**
     * Agregar POI a favoritos
     */
    suspend fun addToFavorites(poi: PointOfInterest): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val favorite = poi.toFavoritePOI()
                favoriteDao.insertFavorite(favorite)
                Log.d(TAG, "POI agregado a favoritos: ${poi.id}")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error al agregar POI a favoritos", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Eliminar POI de favoritos
     */
    suspend fun removeFromFavorites(poiId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                favoriteDao.deleteFavoriteById(poiId)
                Log.d(TAG, "POI eliminado de favoritos: $poiId")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error al eliminar POI de favoritos", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Obtener contador de favoritos
     */
    suspend fun getFavoriteCount(): Int {
        return withContext(Dispatchers.IO) {
            favoriteDao.getFavoriteCount()
        }
    }
    
    // ========== Caché de POIs ==========
    
    /**
     * Guardar POI en caché local
     */
    suspend fun cachePOI(poi: PointOfInterest): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val cachedPOI = poi.toCachedPOI()
                cachedDao.insertCachedPOI(cachedPOI)
                Log.d(TAG, "POI guardado en caché: ${poi.id}")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error al guardar POI en caché", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Guardar múltiples POIs en caché
     */
    suspend fun cachePOIs(pois: List<PointOfInterest>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val cachedPOIs = pois.map { it.toCachedPOI() }
                cachedDao.insertCachedPOIs(cachedPOIs)
                Log.d(TAG, "${pois.size} POIs guardados en caché")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error al guardar POIs en caché", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Obtener POIs desde caché local
     */
    fun getCachedPOIs(limit: Int = 50): Flow<List<PointOfInterest>> {
        return cachedDao.getCachedPOIs(limit)
            .map { cached ->
                cached.mapNotNull { it.toPointOfInterest() }
            }
            .flowOn(Dispatchers.IO)
    }
    
    /**
     * Obtener POI desde caché por ID
     */
    suspend fun getCachedPOIById(poiId: String): PointOfInterest? {
        return withContext(Dispatchers.IO) {
            try {
                cachedDao.getCachedPOIById(poiId)?.toPointOfInterest()
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener POI desde caché", e)
                null
            }
        }
    }
    
    /**
     * Limpiar caché antiguo (más de 7 días)
     */
    suspend fun cleanOldCache(maxAgeDays: Int = 7): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val cutoffTime = System.currentTimeMillis() - (maxAgeDays * 24 * 60 * 60 * 1000L)
                cachedDao.deleteOldCachedPOIs(cutoffTime)
                Log.d(TAG, "Caché antiguo limpiado")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error al limpiar caché", e)
                Result.failure(e)
            }
        }
    }
    
    // ========== Conversiones ==========
    
    private fun FavoritePOI.toPointOfInterest(): PointOfInterest {
        return PointOfInterest(
            id = poiId,
            nombre = nombre,
            descripcion = descripcion,
            categoria = com.example.points.models.CategoriaPOI.values()
                .find { it.name == categoria } ?: com.example.points.models.CategoriaPOI.OTRO,
            ubicacion = com.example.points.models.Ubicacion(
                lat = lat,
                lon = lon,
                direccion = direccion
            ),
            direccion = direccion,
            imagenes = imagenUrl?.let { listOf(it) } ?: emptyList(),
            calificacion = calificacion
        )
    }
    
    private fun PointOfInterest.toFavoritePOI(): FavoritePOI {
        return FavoritePOI(
            poiId = id,
            nombre = nombre,
            descripcion = descripcion,
            categoria = categoria.name,
            direccion = direccion,
            lat = ubicacion.lat,
            lon = ubicacion.lon,
            calificacion = calificacion,
            imagenUrl = imagenes.firstOrNull()
        )
    }
    
    private fun PointOfInterest.toCachedPOI(): CachedPOI {
        return CachedPOI(
            poiId = id,
            nombre = nombre,
            descripcion = descripcion,
            categoria = categoria.name,
            direccion = direccion,
            lat = ubicacion.lat,
            lon = ubicacion.lon,
            calificacion = calificacion,
            totalCalificaciones = totalCalificaciones,
            imagenUrl = imagenes.firstOrNull(),
            estado = estado.name,
            fechaCreacionMillis = fechaCreacion.seconds * 1000 + fechaCreacion.nanoseconds / 1000000,
            fechaActualizacionMillis = fechaActualizacion.seconds * 1000 + fechaActualizacion.nanoseconds / 1000000,
            jsonData = gson.toJson(this)
        )
    }
    
    private fun CachedPOI.toPointOfInterest(): PointOfInterest? {
        return try {
            gson.fromJson(jsonData, PointOfInterest::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error al deserializar POI desde caché", e)
            null
        }
    }
    
    // ========== Adaptador para Timestamp de Firebase ==========
    
    private class TimestampAdapter : TypeAdapter<Timestamp>() {
        override fun write(out: JsonWriter, value: Timestamp?) {
            if (value == null) {
                out.nullValue()
            } else {
                out.value(value.seconds * 1000 + value.nanoseconds / 1000000)
            }
        }
        
        override fun read(`in`: JsonReader): Timestamp? {
            val timestamp = `in`.nextLong()
            val seconds = timestamp / 1000
            val nanoseconds = ((timestamp % 1000) * 1000000).toInt()
            return Timestamp(seconds, nanoseconds)
        }
    }
}

