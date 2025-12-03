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
        private const val TAG = "PointsDatabase"
        
        fun getDatabase(context: Context): PointsDatabase {
            return INSTANCE ?: synchronized(this) {
                android.util.Log.d(TAG, "🔨 Creando instancia de Room Database...")
                
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PointsDatabase::class.java,
                    "points_database"
                )
                    .fallbackToDestructiveMigration() // En desarrollo, recrea la BD si hay cambios
                    .build()
                
                INSTANCE = instance
                android.util.Log.d(TAG, "✅ Room Database instancia creada correctamente")
                
                // Log de la ruta del archivo de base de datos
                val dbPath = context.applicationContext.getDatabasePath("points_database")
                android.util.Log.d(TAG, "📍 Ruta de la base de datos: ${dbPath.absolutePath}")
                
                instance
            }
        }
        
        /**
         * Método para inicializar y verificar la base de datos.
         * Ejecuta operaciones simples para forzar la creación del archivo físico.
         */
        suspend fun initializeAndVerifyDatabase(context: Context) {
            val database = getDatabase(context)
            
            try {
                android.util.Log.d(TAG, "🔍 Verificando base de datos...")
                
                // Forzar la creación del archivo físico ejecutando consultas COUNT
                val favoriteCount = database.favoritePOIDao().getFavoriteCount()
                val cachedCount = database.cachedPOIDao().getCachedCount()
                val searchCount = database.searchHistoryDao().getSearchCount()
                
                android.util.Log.d(TAG, "📊 Estadísticas de la base de datos:")
                android.util.Log.d(TAG, "   - Favoritos: $favoriteCount")
                android.util.Log.d(TAG, "   - POIs en caché: $cachedCount")
                android.util.Log.d(TAG, "   - Búsquedas en historial: $searchCount")
                
                // Verificar que el archivo físico existe
                val dbPath = context.getDatabasePath("points_database")
                if (dbPath.exists()) {
                    val sizeKb = dbPath.length() / 1024
                    android.util.Log.d(TAG, "✅ Base de datos SQLite creada exitosamente")
                    android.util.Log.d(TAG, "📦 Tamaño: $sizeKb KB")
                    android.util.Log.d(TAG, "📍 Ubicación: ${dbPath.absolutePath}")
                } else {
                    android.util.Log.w(TAG, "⚠️ El archivo de base de datos no existe todavía")
                }
                
            } catch (e: Exception) {
                android.util.Log.e(TAG, "❌ Error al inicializar la base de datos", e)
            }
        }
    }
}

