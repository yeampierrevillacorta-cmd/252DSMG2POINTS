package com.example.points.worker

import android.content.Context
import android.util.Log
import com.example.points.PointsApplication
import com.example.points.data.PreferencesManager

/**
 * Ejemplo de uso del SyncWorkManager
 * 
 * Este archivo muestra cómo usar el manager de sincronización automática
 * para configurar y gestionar la sincronización periódica.
 * 
 * NOTA: Este es un archivo de ejemplo/documentación.
 */
object SyncWorkManagerExample {
    
    private const val TAG = "SyncWorkManagerExample"
    
    /**
     * Ejemplo 1: Iniciar sincronización automática
     * 
     * Inicia la sincronización automática con la configuración por defecto
     * (cada 6 horas, cualquier conexión de red)
     */
    fun ejemploIniciarSincronizacion(context: Context) {
        val app = context.applicationContext as? PointsApplication
        val syncWorkManager = SyncWorkManager(
            context,
            app?.container?.preferencesManager ?: return
        )
        
        syncWorkManager.startPeriodicSync()
        Log.d(TAG, "✅ Sincronización automática iniciada")
    }
    
    /**
     * Ejemplo 2: Configurar sincronización personalizada
     * 
     * Inicia la sincronización con intervalo y restricciones personalizadas
     */
    fun ejemploSincronizacionPersonalizada(context: Context) {
        val app = context.applicationContext as? PointsApplication
        val syncWorkManager = SyncWorkManager(
            context,
            app?.container?.preferencesManager ?: return
        )
        
        // Sincronizar cada 3 horas, solo con WiFi
        syncWorkManager.startPeriodicSync(
            intervalHours = 3,
            onlyWifi = true
        )
        
        Log.d(TAG, "✅ Sincronización personalizada iniciada (cada 3 horas, solo WiFi)")
    }
    
    /**
     * Ejemplo 3: Detener sincronización automática
     */
    fun ejemploDetenerSincronizacion(context: Context) {
        val app = context.applicationContext as? PointsApplication
        val syncWorkManager = SyncWorkManager(
            context,
            app?.container?.preferencesManager ?: return
        )
        
        syncWorkManager.stopPeriodicSync()
        Log.d(TAG, "🛑 Sincronización automática detenida")
    }
    
    /**
     * Ejemplo 4: Sincronización inmediata
     * 
     * Ejecuta una sincronización única inmediata sin esperar al intervalo programado
     */
    fun ejemploSincronizacionInmediata(context: Context) {
        val app = context.applicationContext as? PointsApplication
        val syncWorkManager = SyncWorkManager(
            context,
            app?.container?.preferencesManager ?: return
        )
        
        syncWorkManager.syncNow()
        Log.d(TAG, "🔄 Sincronización inmediata programada")
    }
    
    /**
     * Ejemplo 5: Cambiar configuración y reiniciar
     * 
     * Útil cuando el usuario cambia las preferencias de sincronización
     */
    fun ejemploCambiarConfiguracion(context: Context) {
        val app = context.applicationContext as? PointsApplication
        val preferencesManager = app?.container?.preferencesManager ?: return
        val syncWorkManager = SyncWorkManager(context, preferencesManager)
        
        // Cambiar preferencias
        preferencesManager.autoSyncEnabled = true
        preferencesManager.autoSyncIntervalHours = 4 // Cada 4 horas
        preferencesManager.syncOnlyWifi = true // Solo con WiFi
        
        // Reiniciar sincronización con nueva configuración
        syncWorkManager.restartPeriodicSync()
        
        Log.d(TAG, "✅ Configuración actualizada y sincronización reiniciada")
    }
    
    /**
     * Ejemplo 6: Usar desde un ViewModel
     * 
     * Ejemplo de cómo usar el SyncWorkManager desde un ViewModel
     */
    /*
    class SettingsViewModel(
        private val context: Context
    ) : ViewModel() {
        
        private val app = context.applicationContext as PointsApplication
        private val syncWorkManager = SyncWorkManager(
            context,
            app.container.preferencesManager
        )
        
        private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
        val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
        
        fun enableAutoSync(intervalHours: Int, onlyWifi: Boolean) {
            viewModelScope.launch {
                _syncState.value = SyncState.Configuring
                
                app.container.preferencesManager.autoSyncEnabled = true
                app.container.preferencesManager.autoSyncIntervalHours = intervalHours
                app.container.preferencesManager.syncOnlyWifi = onlyWifi
                
                syncWorkManager.startPeriodicSync(
                    intervalHours = intervalHours.toLong(),
                    onlyWifi = onlyWifi
                )
                
                _syncState.value = SyncState.Enabled
            }
        }
        
        fun disableAutoSync() {
            viewModelScope.launch {
                app.container.preferencesManager.autoSyncEnabled = false
                syncWorkManager.stopPeriodicSync()
                _syncState.value = SyncState.Disabled
            }
        }
        
        fun syncNow() {
            viewModelScope.launch {
                _syncState.value = SyncState.Syncing
                syncWorkManager.syncNow()
                // El estado se actualizará cuando el Worker complete
            }
        }
    }
    
    sealed class SyncState {
        object Idle : SyncState()
        object Configuring : SyncState()
        object Enabled : SyncState()
        object Disabled : SyncState()
        object Syncing : SyncState()
    }
    */
}

