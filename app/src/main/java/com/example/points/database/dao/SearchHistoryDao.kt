package com.example.points.database.dao

import androidx.room.*
import com.example.points.database.entity.SearchHistory
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones con historial de búsquedas
 * Implementa operaciones CRUD para la Unidad 5 de Android Basics
 */
@Dao
interface SearchHistoryDao {
    
    @Query("SELECT * FROM search_history ORDER BY fechaBusqueda DESC LIMIT :limit")
    fun getRecentSearches(limit: Int = 10): Flow<List<SearchHistory>>
    
    @Query("SELECT DISTINCT query FROM search_history ORDER BY fechaBusqueda DESC LIMIT :limit")
    fun getRecentSearchQueries(limit: Int = 10): Flow<List<String>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: SearchHistory)
    
    @Delete
    suspend fun deleteSearch(search: SearchHistory)
    
    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteSearchById(id: Long)
    
    @Query("DELETE FROM search_history")
    suspend fun deleteAllSearches()
    
    @Query("DELETE FROM search_history WHERE fechaBusqueda < :timestamp")
    suspend fun deleteOldSearches(timestamp: Long)
    
    @Query("SELECT COUNT(*) FROM search_history")
    suspend fun getSearchCount(): Int
}

