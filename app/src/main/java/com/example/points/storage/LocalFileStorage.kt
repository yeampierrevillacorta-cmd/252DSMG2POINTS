package com.example.points.storage

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Gestor de almacenamiento en archivos internos
 * Implementa almacenamiento en sandbox para la Unidad 5 de Android Basics
 */
class LocalFileStorage(private val context: Context) {
    
    private val filesDir: File = context.filesDir
    private val cacheDir: File = context.cacheDir
    
    companion object {
        private const val TAG = "LocalFileStorage"
        
        // Directorios
        private const val IMAGES_CACHE_DIR = "images_cache"
        private const val EXPORTS_DIR = "exports"
        private const val LOGS_DIR = "logs"
        private const val TEMP_DIR = "temp"
        
        // Tamaño máximo de caché (50 MB)
        private const val MAX_CACHE_SIZE = 50 * 1024 * 1024L
    }
    
    /**
     * Guardar imagen en caché local
     */
    suspend fun saveImageToCache(imageUrl: String, imageData: ByteArray): File? {
        return try {
            val imagesDir = File(cacheDir, IMAGES_CACHE_DIR)
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }
            
            val fileName = imageUrl.hashCode().toString() + ".jpg"
            val imageFile = File(imagesDir, fileName)
            
            FileOutputStream(imageFile).use { output ->
                output.write(imageData)
            }
            
            Log.d(TAG, "Imagen guardada en caché: ${imageFile.absolutePath}")
            imageFile
        } catch (e: IOException) {
            Log.e(TAG, "Error al guardar imagen en caché", e)
            null
        }
    }
    
    /**
     * Leer imagen de caché local
     */
    fun getImageFromCache(imageUrl: String): File? {
        val imagesDir = File(cacheDir, IMAGES_CACHE_DIR)
        val fileName = imageUrl.hashCode().toString() + ".jpg"
        val imageFile = File(imagesDir, fileName)
        
        return if (imageFile.exists()) {
            imageFile
        } else {
            null
        }
    }
    
    /**
     * Guardar datos exportados (JSON, CSV, etc.)
     */
    suspend fun saveExportData(data: String, fileName: String): File? {
        return try {
            val exportsDir = File(filesDir, EXPORTS_DIR)
            if (!exportsDir.exists()) {
                exportsDir.mkdirs()
            }
            
            val exportFile = File(exportsDir, fileName)
            FileOutputStream(exportFile).use { output ->
                output.write(data.toByteArray())
            }
            
            Log.d(TAG, "Datos exportados guardados: ${exportFile.absolutePath}")
            exportFile
        } catch (e: IOException) {
            Log.e(TAG, "Error al guardar datos exportados", e)
            null
        }
    }
    
    /**
     * Leer datos exportados
     */
    fun readExportData(fileName: String): String? {
        return try {
            val exportsDir = File(filesDir, EXPORTS_DIR)
            val exportFile = File(exportsDir, fileName)
            
            if (exportFile.exists()) {
                exportFile.readText()
            } else {
                null
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error al leer datos exportados", e)
            null
        }
    }
    
    /**
     * Guardar log de la aplicación
     */
    suspend fun saveLog(logMessage: String) {
        try {
            val logsDir = File(filesDir, LOGS_DIR)
            if (!logsDir.exists()) {
                logsDir.mkdirs()
            }
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val logFileName = "log_${dateFormat.format(Date())}.txt"
            val logFile = File(logsDir, logFileName)
            
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date())
            val logEntry = "[$timestamp] $logMessage\n"
            
            logFile.appendText(logEntry)
        } catch (e: IOException) {
            Log.e(TAG, "Error al guardar log", e)
        }
    }
    
    /**
     * Leer logs de un día específico
     */
    fun readLogs(date: Date): String? {
        return try {
            val logsDir = File(filesDir, LOGS_DIR)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val logFileName = "log_${dateFormat.format(date)}.txt"
            val logFile = File(logsDir, logFileName)
            
            if (logFile.exists()) {
                logFile.readText()
            } else {
                null
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error al leer logs", e)
            null
        }
    }
    
    /**
     * Limpiar caché antiguo
     */
    suspend fun cleanOldCache(maxAgeDays: Int = 7) {
        try {
            val imagesDir = File(cacheDir, IMAGES_CACHE_DIR)
            if (!imagesDir.exists()) return
            
            val cutoffTime = System.currentTimeMillis() - (maxAgeDays * 24 * 60 * 60 * 1000L)
            var deletedCount = 0
            var freedSpace = 0L
            
            imagesDir.listFiles()?.forEach { file ->
                if (file.lastModified() < cutoffTime) {
                    val fileSize = file.length()
                    if (file.delete()) {
                        deletedCount++
                        freedSpace += fileSize
                    }
                }
            }
            
            Log.d(TAG, "Caché limpiado: $deletedCount archivos, ${freedSpace / 1024} KB liberados")
        } catch (e: Exception) {
            Log.e(TAG, "Error al limpiar caché", e)
        }
    }
    
    /**
     * Obtener tamaño total del caché
     */
    fun getCacheSize(): Long {
        return try {
            val imagesDir = File(cacheDir, IMAGES_CACHE_DIR)
            if (!imagesDir.exists()) return 0L
            
            var totalSize = 0L
            imagesDir.listFiles()?.forEach { file ->
                totalSize += file.length()
            }
            totalSize
        } catch (e: Exception) {
            Log.e(TAG, "Error al calcular tamaño del caché", e)
            0L
        }
    }
    
    /**
     * Limpiar todo el caché si excede el tamaño máximo
     */
    suspend fun cleanCacheIfNeeded() {
        val cacheSize = getCacheSize()
        if (cacheSize > MAX_CACHE_SIZE) {
            Log.d(TAG, "Caché excede tamaño máximo, limpiando...")
            cleanOldCache(1) // Limpiar archivos de más de 1 día
        }
    }
    
    /**
     * Guardar archivo temporal
     */
    suspend fun saveTempFile(inputStream: InputStream, fileName: String): File? {
        return try {
            val tempDir = File(cacheDir, TEMP_DIR)
            if (!tempDir.exists()) {
                tempDir.mkdirs()
            }
            
            val tempFile = File(tempDir, fileName)
            FileOutputStream(tempFile).use { output ->
                inputStream.copyTo(output)
            }
            
            tempFile
        } catch (e: IOException) {
            Log.e(TAG, "Error al guardar archivo temporal", e)
            null
        }
    }
    
    /**
     * Eliminar archivo temporal
     */
    fun deleteTempFile(fileName: String): Boolean {
        return try {
            val tempDir = File(cacheDir, TEMP_DIR)
            val tempFile = File(tempDir, fileName)
            tempFile.delete()
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar archivo temporal", e)
            false
        }
    }
    
    /**
     * Limpiar archivos temporales antiguos
     */
    suspend fun cleanTempFiles(maxAgeHours: Int = 24) {
        try {
            val tempDir = File(cacheDir, TEMP_DIR)
            if (!tempDir.exists()) return
            
            val cutoffTime = System.currentTimeMillis() - (maxAgeHours * 60 * 60 * 1000L)
            var deletedCount = 0
            
            tempDir.listFiles()?.forEach { file ->
                if (file.lastModified() < cutoffTime) {
                    if (file.delete()) {
                        deletedCount++
                    }
                }
            }
            
            Log.d(TAG, "Archivos temporales limpiados: $deletedCount archivos")
        } catch (e: Exception) {
            Log.e(TAG, "Error al limpiar archivos temporales", e)
        }
    }
}

