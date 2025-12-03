package com.example.points.repository

import com.example.points.database.PointsDatabase
import com.example.points.database.dao.SearchHistoryDao
import com.example.points.database.entity.SearchHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import android.util.Log

/**
 * Repositorio local para historial de búsquedas
 * Implementa almacenamiento local
 */
class LocalSearchRepository(private val database: PointsDatabase) {
    
    private val searchDao: SearchHistoryDao = database.searchHistoryDao()
    
    companion object {
        private const val TAG = "LocalSearchRepository"
    }

    /**
     * Obtener historial de búsquedas recientes
     */
    fun getRecentSearches(limit: Int = 10): Flow<List<SearchHistory>> {
        return searchDao.getRecentSearches(limit)
            .flowOn(Dispatchers.IO)
    }

    /**
     * Obtener queries de búsqueda recientes
     */
    fun getRecentSearchQueries(limit: Int = 10): Flow<List<String>> {
        return searchDao.getRecentSearchQueries(limit)
            .flowOn(Dispatchers.IO)
    }

    /**
     * Guardar búsqueda en historial
     */
    suspend fun saveSearch(query: String, category: String?, resultados: Int = 0): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val search = SearchHistory(
                    query = query,
                    category = category,
                    resultados = resultados
                )
                searchDao.insertSearch(search)
                Log.d(TAG, "Búsqueda guardada en historial: $query")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error al guardar búsqueda en historial", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Eliminar búsqueda del historial
     */
    suspend fun deleteSearch(id: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                searchDao.deleteSearchById(id)
                Log.d(TAG, "Búsqueda eliminada del historial: $id")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error al eliminar búsqueda del historial", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Limpiar todo el historial
     */
    suspend fun clearHistory(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                searchDao.deleteAllSearches()
                Log.d(TAG, "Historial limpiado")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error al limpiar historial", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Limpiar búsquedas antiguas (más de 30 días)
     */
    suspend fun cleanOldSearches(maxAgeDays: Int = 30): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val cutoffTime = System.currentTimeMillis() - (maxAgeDays * 24 * 60 * 60 * 1000L)
                searchDao.deleteOldSearches(cutoffTime)
                Log.d(TAG, "Búsquedas antiguas eliminadas")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error al limpiar búsquedas antiguas", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Obtener contador de búsquedas
     */
    suspend fun getSearchCount(): Int {
        return withContext(Dispatchers.IO) {
            searchDao.getSearchCount()
        }
    }
}

