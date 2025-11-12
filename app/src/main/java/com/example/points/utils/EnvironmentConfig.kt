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
        try {
            val envFile = File(context.filesDir, ".env")
            
            // Siempre intentar copiar desde assets (sobrescribe el existente si hay uno)
            // Esto asegura que el .env siempre esté actualizado con la versión de assets
            var copiedFromAssets = false
            try {
                // Intentar cargar desde assets
                context.assets.open(".env").use { input ->
                    envFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                copiedFromAssets = true
                android.util.Log.d("EnvironmentConfig", "Archivo .env copiado desde assets a filesDir")
            } catch (e: Exception) {
                // Si no existe en assets, verificar si hay uno en filesDir
                if (envFile.exists()) {
                    android.util.Log.d("EnvironmentConfig", "No se encontró .env en assets, usando el existente en filesDir")
                } else {
                    // Si no existe en assets ni en filesDir, es normal - el archivo puede no estar configurado
                    android.util.Log.w("EnvironmentConfig", "No se encontró .env en assets ni en filesDir (esto es normal si no está configurado): ${e.message}")
                }
            }
            
            // Reinicializar dotenv si se copió desde assets o si no estaba inicializado
            // Esto asegura que siempre use la versión más reciente del .env
            if (copiedFromAssets || dotenv == null) {
                // Cargar desde filesDir
                dotenv = dotenv {
                    ignoreIfMissing = true
                    directory = context.filesDir.absolutePath
                }
                
                // Log de configuración en modo debug
                android.util.Log.d("EnvironmentConfig", "=== Configuración de Variables de Entorno ===")
                android.util.Log.d("EnvironmentConfig", "GOOGLE_MAPS_API_KEY: ${if (GOOGLE_MAPS_API_KEY.isNotEmpty()) "***configurada***" else "❌ FALTA"}")
                android.util.Log.d("EnvironmentConfig", "FIREBASE_PROJECT_ID: ${if (FIREBASE_PROJECT_ID.isNotEmpty()) "***configurada***" else "❌ FALTA"}")
                android.util.Log.d("EnvironmentConfig", "FIREBASE_API_KEY: ${if (FIREBASE_API_KEY.isNotEmpty()) "***configurada***" else "❌ FALTA"}")
                android.util.Log.d("EnvironmentConfig", "FIREBASE_APP_ID: ${if (FIREBASE_APP_ID.isNotEmpty()) "***configurada***" else "❌ FALTA"}")
                android.util.Log.d("EnvironmentConfig", "OPENWEATHER_API_KEY: ${if (OPENWEATHER_API_KEY.isNotEmpty()) "***configurada***" else "❌ FALTA"}")
                android.util.Log.d("EnvironmentConfig", "GEMINI_API_KEY: ${if (GEMINI_API_KEY.isNotEmpty()) "***configurada***" else "❌ FALTA"}")
                android.util.Log.d("EnvironmentConfig", "Configuración válida: ${isConfigurationValid()}")
                
                // Verificar específicamente la API key de Gemini
                if (GEMINI_API_KEY.isEmpty()) {
                    android.util.Log.w("EnvironmentConfig", "⚠️ GEMINI_API_KEY está vacía después de cargar .env")
                } else {
                    android.util.Log.d("EnvironmentConfig", "✅ GEMINI_API_KEY cargada correctamente (longitud: ${GEMINI_API_KEY.length} caracteres)")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("EnvironmentConfig", "Error al inicializar variables de entorno", e)
            // Si falla y no está inicializado, usar valores por defecto
            if (dotenv == null) {
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
    
    // Google Gemini
    val GEMINI_API_KEY: String
        get() = getEnvValue("GEMINI_API_KEY")
    
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
               FIREBASE_APP_ID.isNotEmpty() &&
               OPENWEATHER_API_KEY.isNotEmpty()
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
            "Firebase API Key" to if (FIREBASE_API_KEY.isNotEmpty()) "***configured***" else "***missing***",
            "OpenWeatherMap API Key" to if (OPENWEATHER_API_KEY.isNotEmpty()) "***configured***" else "***missing***",
            "Gemini API Key" to if (GEMINI_API_KEY.isNotEmpty()) "***configured***" else "***missing***"
        )
    }
}
