package com.example.points

import android.app.Application
import com.example.points.data.AppContainer
import com.example.points.data.DefaultAppContainer
import com.example.points.utils.EnvironmentConfig
<<<<<<< HEAD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
=======
import com.example.points.worker.SyncWorkManager
import com.google.firebase.auth.FirebaseAuth
>>>>>>> 3616147010ca71a00c51183b96f2dd12eda121ab

/**
 * Clase Application personalizada para inicializar configuraciones globales
 */
class PointsApplication : Application() {
    
    lateinit var container: AppContainer
    private var syncWorkManager: SyncWorkManager? = null
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar configuración de variables de entorno
        EnvironmentConfig.initialize(this)
        
        // Inicializar contenedor de dependencias (pasar contexto para Room y SharedPreferences)
        try {
            container = DefaultAppContainer(this)
            android.util.Log.d("PointsApp", "PointsApplication.onCreate() - Container inicializado correctamente")
        } catch (e: Exception) {
            android.util.Log.e("PointsApp", "Error al inicializar container", e)
            throw e
        }
        
        // Inicializar SyncWorkManager
        syncWorkManager = SyncWorkManager(this, container.preferencesManager)
        
        // Inicializar sincronización automática si está habilitada
        initializeAutoSync()
        
        // Log de configuración (solo en modo debug)
        if (EnvironmentConfig.DEBUG_MODE) {
            val configInfo = EnvironmentConfig.getConfigurationInfo()
            android.util.Log.d("PointsApp", "Configuración inicializada:")
            configInfo.forEach { (key, value) ->
                android.util.Log.d("PointsApp", "$key: $value")
            }
        }
        
        // Inicializar sincronización automática si está habilitada
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val syncEnabled = container.syncPreferences.getAutoSyncEnabled()
                if (syncEnabled) {
                    android.util.Log.d("PointsApp", "Programando sincronización automática...")
                    container.syncWorkManager.schedulePeriodicSync()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("PointsApp", "Error al inicializar sincronización", e)
        }
    }
    
    /**
     * Inicializa la sincronización automática si está habilitada
     */
    private fun initializeAutoSync() {
        if (container.preferencesManager.autoSyncEnabled) {
            android.util.Log.d("PointsApp", "Iniciando sincronización automática...")
            syncWorkManager?.startPeriodicSync()
        } else {
            android.util.Log.d("PointsApp", "Sincronización automática deshabilitada")
        }
    }
    
    /**
     * Reinicia la sincronización automática (útil cuando el usuario cambia las preferencias)
     */
    fun restartAutoSync() {
        syncWorkManager?.restartPeriodicSync()
    }
    
    /**
     * Ejecuta una sincronización inmediata
     */
    fun syncNow() {
        syncWorkManager?.syncNow()
    }
}