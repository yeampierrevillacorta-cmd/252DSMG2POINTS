package com.example.points.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.points.database.dao.CachedPOIDao
import com.example.points.database.dao.FavoritePOIDao
import com.example.points.database.dao.SearchHistoryDao
import com.example.points.database.entity.CachedPOI
import com.example.points.database.entity.FavoritePOI
import com.example.points.database.entity.SearchHistory

/**
 * Base de datos Room para almacenamiento local
 * Implementa SQLite para la Unidad 5 de Android Basics
 */
@Database(
    entities = [
        FavoritePOI::class,
        SearchHistory::class,
        CachedPOI::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PointsDatabase : RoomDatabase() {
    
    abstract fun favoritePOIDao(): FavoritePOIDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun cachedPOIDao(): CachedPOIDao
    
    companion object {
        @Volatile
        private var INSTANCE: PointsDatabase? = null
        
        fun getDatabase(context: Context): PointsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PointsDatabase::class.java,
                    "points_database"
                )
                    .fallbackToDestructiveMigration() // En desarrollo, recrea la BD si hay cambios
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

