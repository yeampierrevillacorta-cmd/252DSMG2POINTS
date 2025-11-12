package com.example.points

import android.app.Application
import com.example.points.data.AppContainer
import com.example.points.data.DefaultAppContainer
import com.example.points.utils.EnvironmentConfig

/**
 * Clase Application personalizada para inicializar configuraciones globales
 */
class PointsApplication : Application() {
    
    lateinit var container: AppContainer
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar configuración de variables de entorno
        EnvironmentConfig.initialize(this)
        
        // Inicializar contenedor de dependencias (pasar contexto para Room y SharedPreferences)
        container = DefaultAppContainer(this)
        
        // Log de configuración (solo en modo debug)
        if (EnvironmentConfig.DEBUG_MODE) {
            val configInfo = EnvironmentConfig.getConfigurationInfo()
            android.util.Log.d("PointsApp", "Configuración inicializada:")
            configInfo.forEach { (key, value) ->
                android.util.Log.d("PointsApp", "$key: $value")
            }
        }
    }
}