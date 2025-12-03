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
                android.util.Log.d("EnvironmentConfig", "✅ Archivo .env copiado desde assets a filesDir")
            } catch (e: Exception) {
                // Si no existe en assets, verificar si hay uno en filesDir
                if (envFile.exists()) {
                    android.util.Log.w("EnvironmentConfig", "⚠️ No se encontró .env en assets, usando el existente en filesDir")
                } else {
                    // Si no existe en assets ni en filesDir, es normal - el archivo puede no estar configurado
                    android.util.Log.w("EnvironmentConfig", "⚠️ No se encontró .env en assets ni en filesDir (esto es normal si no está configurado): ${e.message}")
                }
            }
            
            // SIEMPRE reinicializar dotenv para asegurar que use la versión más reciente
            // Incluso si no se copió desde assets, necesitamos cargar desde filesDir
            android.util.Log.d("EnvironmentConfig", "Reinicializando dotenv...")
            dotenv = null // Forzar reinicialización
            
            // Cargar desde filesDir
            dotenv = dotenv {
                ignoreIfMissing = true
                directory = context.filesDir.absolutePath
            }
            
            android.util.Log.d("EnvironmentConfig", "dotenv inicializado: ${dotenv != null}")
            
            // Log de configuración en modo debug (SIEMPRE mostrar, no solo en DEBUG_MODE)
            android.util.Log.d("EnvironmentConfig", "=== Configuración de Variables de Entorno ===")
            android.util.Log.d("EnvironmentConfig", "Archivo .env ubicación: ${envFile.absolutePath}")
            android.util.Log.d("EnvironmentConfig", "Archivo .env existe: ${envFile.exists()}")
            if (envFile.exists()) {
                android.util.Log.d("EnvironmentConfig", "Archivo .env tamaño: ${envFile.length()} bytes")
                // Leer y mostrar las primeras líneas del archivo para debugging
                try {
                    val envContent = envFile.readText()
                    val lines = envContent.lines().take(20) // Primeras 20 líneas
                    android.util.Log.d("EnvironmentConfig", "Primeras líneas del .env:")
                    lines.forEachIndexed { index, line ->
                        val trimmedLine = line.trim()
                        if (trimmedLine.isNotEmpty() && !trimmedLine.startsWith("#")) {
                            val displayLine = if (trimmedLine.contains("=")) {
                                val parts = trimmedLine.split("=", limit = 2)
                                if (parts.size == 2 && parts[1].isNotEmpty()) {
                                    "${parts[0]}=${parts[1].take(10)}... (${parts[1].length} chars)"
                                } else {
                                    trimmedLine
                                }
                            } else {
                                trimmedLine
                            }
                            android.util.Log.d("EnvironmentConfig", "   [$index] $displayLine")
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("EnvironmentConfig", "Error al leer archivo .env: ${e.message}")
                }
            }
            
            android.util.Log.d("EnvironmentConfig", "GOOGLE_MAPS_API_KEY: ${if (GOOGLE_MAPS_API_KEY.isNotEmpty()) "***configurada*** (${GOOGLE_MAPS_API_KEY.length} chars)" else "❌ FALTA"}")
            android.util.Log.d("EnvironmentConfig", "FIREBASE_PROJECT_ID: ${if (FIREBASE_PROJECT_ID.isNotEmpty()) "***configurada***" else "❌ FALTA"}")
            android.util.Log.d("EnvironmentConfig", "FIREBASE_API_KEY: ${if (FIREBASE_API_KEY.isNotEmpty()) "***configurada***" else "❌ FALTA"}")
            android.util.Log.d("EnvironmentConfig", "FIREBASE_APP_ID: ${if (FIREBASE_APP_ID.isNotEmpty()) "***configurada***" else "❌ FALTA"}")
            android.util.Log.d("EnvironmentConfig", "OPENWEATHER_API_KEY: ${if (OPENWEATHER_API_KEY.isNotEmpty()) "***configurada*** (${OPENWEATHER_API_KEY.length} chars)" else "❌ FALTA"}")
            android.util.Log.d("EnvironmentConfig", "GEMINI_API_KEY: ${if (GEMINI_API_KEY.isNotEmpty()) "***configurada*** (${GEMINI_API_KEY.length} chars)" else "❌ FALTA"}")
            android.util.Log.d("EnvironmentConfig", "BACKEND_BASE_URL: ${if (BACKEND_BASE_URL.isNotEmpty()) "***configurada***" else "❌ FALTA"}")
            
            // Verificar específicamente la API key de Gemini (CRÍTICO)
            val geminiKey = GEMINI_API_KEY
            android.util.Log.d("EnvironmentConfig", "GEMINI_API_KEY después de cargar: longitud=${geminiKey.length}, vacía=${geminiKey.isEmpty()}")
            if (geminiKey.isEmpty()) {
                android.util.Log.e("EnvironmentConfig", "❌ GEMINI_API_KEY está VACÍA después de cargar .env")
                android.util.Log.e("EnvironmentConfig", "   Esto significa que la API key NO se está leyendo correctamente")
                android.util.Log.e("EnvironmentConfig", "   Verifica que el .env en assets tenga GEMINI_API_KEY configurada")
                // Intentar leer el archivo directamente para debug
                if (envFile.exists()) {
                    try {
                        val envContent = envFile.readText()
                        android.util.Log.d("EnvironmentConfig", "   Contenido completo del .env (primeros 500 chars):")
                        android.util.Log.d("EnvironmentConfig", "   ${envContent.take(500)}")
                        if (envContent.contains("GEMINI_API_KEY")) {
                            android.util.Log.d("EnvironmentConfig", "   ✅ GEMINI_API_KEY encontrada en el archivo .env")
                            val lines = envContent.lines()
                            lines.forEach { line ->
                                val trimmedLine = line.trim()
                                if (trimmedLine.startsWith("GEMINI_API_KEY")) {
                                    val parts = trimmedLine.split("=", limit = 2)
                                    if (parts.size == 2) {
                                        val keyValue = parts[1].trim()
                                        android.util.Log.d("EnvironmentConfig", "   Línea encontrada: GEMINI_API_KEY=${keyValue.take(10)}... (longitud: ${keyValue.length})")
                                        if (keyValue.isEmpty()) {
                                            android.util.Log.e("EnvironmentConfig", "   ❌ El valor de GEMINI_API_KEY está VACÍO en el archivo")
                                        }
                                    } else {
                                        android.util.Log.e("EnvironmentConfig", "   ⚠️ Formato incorrecto de GEMINI_API_KEY: $trimmedLine")
                                    }
                                }
                            }
                        } else {
                            android.util.Log.e("EnvironmentConfig", "   ❌ GEMINI_API_KEY NO encontrada en el contenido del archivo .env")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("EnvironmentConfig", "   Error al leer archivo .env: ${e.message}")
                        e.printStackTrace()
                    }
                } else {
                    android.util.Log.e("EnvironmentConfig", "   ❌ El archivo .env no existe en filesDir")
                }
            } else {
                android.util.Log.d("EnvironmentConfig", "✅ GEMINI_API_KEY cargada correctamente (longitud: ${geminiKey.length} caracteres)")
                android.util.Log.d("EnvironmentConfig", "   Primeros 10 caracteres: ${geminiKey.take(10)}...")
            }
            android.util.Log.d("EnvironmentConfig", "Configuración válida: ${isConfigurationValid()}")
            
        } catch (e: Exception) {
            android.util.Log.e("EnvironmentConfig", "❌ Error al inicializar variables de entorno", e)
            e.printStackTrace()
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
        get() {
            val envValue = getEnvValue("GEMINI_API_KEY")
            // Fallback a la API key local si no está en .env
            return if (envValue.isEmpty()) {
                "AIzaSyAA0Bd-Ppbk6GvTwbOPen4q_VuQs35AC5c"
            } else {
                envValue
            }
        }
    
    // Backend URL para sincronización
    val BACKEND_BASE_URL: String
        get() = getEnvValue("BACKEND_BASE_URL")
    
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
