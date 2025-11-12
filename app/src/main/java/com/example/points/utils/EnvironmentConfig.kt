package com.example.points.utils

import android.content.Context
import io.github.cdimascio.dotenv.dotenv
import java.io.File

/**
 * Configuración de variables de entorno para la aplicación
 * Maneja la carga segura de claves de API y configuraciones
 */
object EnvironmentConfig {
    
    private var dotenv: io.github.cdimascio.dotenv.Dotenv? = null
    
    fun initialize(context: Context) {
        if (dotenv == null) {
            try {
                // Intentar cargar desde assets
                val envFile = File(context.filesDir, ".env")
                if (!envFile.exists()) {
                    // Copiar desde assets si no existe
                    context.assets.open(".env").use { input ->
                        envFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
                
                dotenv = dotenv {
                    ignoreIfMissing = true
                    directory = context.filesDir.absolutePath
                }
            } catch (e: Exception) {
                // Si falla, usar valores por defecto
                dotenv = dotenv {
                    ignoreIfMissing = true
                }
            }
        }
    }
    
    private fun getEnvValue(key: String): String {
        return dotenv?.get(key) ?: ""
    }
    
    // Google Maps
    val GOOGLE_MAPS_API_KEY: String
        get() = getEnvValue("GOOGLE_MAPS_API_KEY")
    
    // Firebase
    val FIREBASE_PROJECT_ID: String
        get() = getEnvValue("FIREBASE_PROJECT_ID")
    
    val FIREBASE_PROJECT_NUMBER: String
        get() = getEnvValue("FIREBASE_PROJECT_NUMBER")
    
    val FIREBASE_STORAGE_BUCKET: String
        get() = getEnvValue("FIREBASE_STORAGE_BUCKET")
    
    val FIREBASE_API_KEY: String
        get() = getEnvValue("FIREBASE_API_KEY")
    
    val FIREBASE_APP_ID: String
        get() = getEnvValue("FIREBASE_APP_ID")
    
    // OpenWeatherMap
    val OPENWEATHER_API_KEY: String
        get() = getEnvValue("OPENWEATHER_API_KEY")
    
    // Environment
    val ENVIRONMENT: String
        get() = getEnvValue("ENVIRONMENT").ifEmpty { "development" }
    
    val DEBUG_MODE: Boolean
        get() = getEnvValue("DEBUG_MODE").toBooleanStrictOrNull() ?: true
    
    /**
     * Verifica si todas las variables de entorno requeridas están configuradas
     */
    fun isConfigurationValid(): Boolean {
        return GOOGLE_MAPS_API_KEY.isNotEmpty() &&
               FIREBASE_PROJECT_ID.isNotEmpty() &&
               FIREBASE_API_KEY.isNotEmpty() &&
               FIREBASE_APP_ID.isNotEmpty()
    }
    
    /**
     * Obtiene información de configuración para debugging (sin claves sensibles)
     */
    fun getConfigurationInfo(): Map<String, String> {
        return mapOf(
            "Environment" to ENVIRONMENT,
            "Debug Mode" to DEBUG_MODE.toString(),
            "Firebase Project ID" to FIREBASE_PROJECT_ID,
            "Google Maps API Key" to if (GOOGLE_MAPS_API_KEY.isNotEmpty()) "***configured***" else "***missing***",
            "Firebase API Key" to if (FIREBASE_API_KEY.isNotEmpty()) "***configured***" else "***missing***"
        )
    }
}
