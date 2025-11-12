package com.example.points.database.dao

import androidx.room.*
import com.example.points.database.entity.FavoritePOI
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones con POIs favoritos
 * Implementa operaciones CRUD para la Unidad 5 de Android Basics
 */
@Dao
interface FavoritePOIDao {
    
    @Query("SELECT * FROM favorite_pois ORDER BY fechaAgregado DESC")
    fun getAllFavorites(): Flow<List<FavoritePOI>>
    
    @Query("SELECT * FROM favorite_pois WHERE poiId = :poiId")
    suspend fun getFavoriteById(poiId: String): FavoritePOI?
    
    @Query("SELECT * FROM favorite_pois WHERE poiId = :poiId")
    fun getFavoriteByIdFlow(poiId: String): Flow<FavoritePOI?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoritePOI)
    
    @Delete
    suspend fun deleteFavorite(favorite: FavoritePOI)
    
    @Query("DELETE FROM favorite_pois WHERE poiId = :poiId")
    suspend fun deleteFavoriteById(poiId: String)
    
    @Query("DELETE FROM favorite_pois")
    suspend fun deleteAllFavorites()
    
    @Query("SELECT COUNT(*) FROM favorite_pois")
    suspend fun getFavoriteCount(): Int
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_pois WHERE poiId = :poiId)")
    suspend fun isFavorite(poiId: String): Boolean
}

