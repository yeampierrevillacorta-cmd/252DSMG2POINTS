package com.example.points.utils

import android.content.Context
import android.util.Log

/**
 * Helper para manejar configuraciones de la aplicación
 * Demuestra cómo usar las variables de entorno
 */
object ConfigHelper {
    
    private const val TAG = "ConfigHelper"
    
    /**
     * Inicializa la configuración de la aplicación
     */
    fun initializeConfig(context: Context) {
        // Inicializar variables de entorno
        EnvironmentConfig.initialize(context)
        
        // Verificar configuración
        if (!EnvironmentConfig.isConfigurationValid()) {
            Log.w(TAG, "⚠️ Configuración incompleta - algunas variables de entorno faltan")
        } else {
            Log.i(TAG, "✅ Configuración válida - todas las variables de entorno están configuradas")
        }
        
        // Log de información de configuración (sin claves sensibles)
        if (EnvironmentConfig.DEBUG_MODE) {
            val configInfo = EnvironmentConfig.getConfigurationInfo()
            Log.d(TAG, "📋 Información de configuración:")
            configInfo.forEach { (key, value) ->
                Log.d(TAG, "   $key: $value")
            }
        }
    }
    
    /**
     * Obtiene la clave de Google Maps para usar en la aplicación
     */
    fun getGoogleMapsApiKey(): String {
        val key = EnvironmentConfig.GOOGLE_MAPS_API_KEY
        if (key.isEmpty()) {
            Log.e(TAG, "❌ Google Maps API Key no configurada")
        }
        return key
    }
    
    /**
     * Obtiene información de Firebase para debugging
     */
    fun getFirebaseInfo(): Map<String, String> {
        return mapOf(
            "Project ID" to EnvironmentConfig.FIREBASE_PROJECT_ID,
            "Project Number" to EnvironmentConfig.FIREBASE_PROJECT_NUMBER,
            "Storage Bucket" to EnvironmentConfig.FIREBASE_STORAGE_BUCKET,
            "API Key" to if (EnvironmentConfig.FIREBASE_API_KEY.isNotEmpty()) "***configured***" else "***missing***",
            "App ID" to EnvironmentConfig.FIREBASE_APP_ID
        )
    }
    
    /**
     * Verifica si la aplicación está en modo debug
     */
    fun isDebugMode(): Boolean = EnvironmentConfig.DEBUG_MODE
    
    /**
     * Obtiene el entorno actual
     */
    fun getEnvironment(): String = EnvironmentConfig.ENVIRONMENT
}
