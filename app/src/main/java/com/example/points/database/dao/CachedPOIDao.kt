package com.example.points.database.dao

import androidx.room.*
import com.example.points.database.entity.CachedPOI
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones con caché de POIs
 * Permite acceso offline a POIs vistos recientemente
 */
@Dao
interface CachedPOIDao {
    
    @Query("SELECT * FROM cached_pois ORDER BY fechaCache DESC LIMIT :limit")
    fun getCachedPOIs(limit: Int = 50): Flow<List<CachedPOI>>
    
    @Query("SELECT * FROM cached_pois WHERE poiId = :poiId")
    suspend fun getCachedPOIById(poiId: String): CachedPOI?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedPOI(poi: CachedPOI)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedPOIs(pois: List<CachedPOI>)
    
    @Delete
    suspend fun deleteCachedPOI(poi: CachedPOI)
    
    @Query("DELETE FROM cached_pois WHERE poiId = :poiId")
    suspend fun deleteCachedPOIById(poiId: String)
    
    @Query("DELETE FROM cached_pois")
    suspend fun deleteAllCachedPOIs()
    
    @Query("DELETE FROM cached_pois WHERE fechaCache < :timestamp")
    suspend fun deleteOldCachedPOIs(timestamp: Long)
    
    @Query("SELECT COUNT(*) FROM cached_pois")
    suspend fun getCachedCount(): Int
}

