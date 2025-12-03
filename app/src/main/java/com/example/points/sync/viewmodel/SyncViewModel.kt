package com.example.points.sync.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.points.sync.data.SyncPreferences
import com.example.points.sync.worker.SyncWorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Context
import android.util.Log

/**
 * ViewModel para gestión de sincronización
 * Gestiona el estado de sincronización y configuración
 */
data class SyncUiState(
    val isSyncEnabled: Boolean = true,
    val autoSyncEnabled: Boolean = true,
    val syncFrequency: Int = 60, // minutos
    val syncOnWifiOnly: Boolean = false,
    val lastSyncTimestamp: Long = 0L,
    val isSyncing: Boolean = false,
    val syncError: String? = null,
    val syncSuccess: Boolean = false
)

class SyncViewModel(
    private val context: Context,
    private val syncPreferences: SyncPreferences,
    private val syncWorkManager: SyncWorkManager
) : ViewModel() {
    
    companion object {
        fun Factory(context: Context): androidx.lifecycle.ViewModelProvider.Factory {
            return object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    val application = context.applicationContext as com.example.points.PointsApplication
                    val container = application.container
                    return SyncViewModel(
                        context,
                        container.syncPreferences,
                        container.syncWorkManager
                    ) as T
                }
            }
        }
    }
    
    private val _uiState = MutableStateFlow(SyncUiState())
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()
    
    init {
        loadSyncSettings()
    }
    
    /**
     * Cargar configuración de sincronización
     */
    private fun loadSyncSettings() {
        viewModelScope.launch {
            syncPreferences.syncEnabled.collect { enabled ->
                _uiState.value = _uiState.value.copy(isSyncEnabled = enabled)
            }
        }
        
        viewModelScope.launch {
            syncPreferences.autoSyncEnabled.collect { autoEnabled ->
                _uiState.value = _uiState.value.copy(autoSyncEnabled = autoEnabled)
            }
        }
        
        viewModelScope.launch {
            syncPreferences.syncFrequency.collect { frequency ->
                _uiState.value = _uiState.value.copy(syncFrequency = frequency)
            }
        }
        
        viewModelScope.launch {
            syncPreferences.syncOnWifiOnly.collect { wifiOnly ->
                _uiState.value = _uiState.value.copy(syncOnWifiOnly = wifiOnly)
            }
        }
        
        viewModelScope.launch {
            syncPreferences.lastSyncTimestamp.collect { timestamp ->
                _uiState.value = _uiState.value.copy(lastSyncTimestamp = timestamp)
            }
        }
    }
    
    /**
     * Habilitar/deshabilitar sincronización
     */
    fun setSyncEnabled(enabled: Boolean) {
        viewModelScope.launch {
            syncPreferences.setSyncEnabled(enabled)
            if (enabled) {
                syncWorkManager.schedulePeriodicSync()
            } else {
                syncWorkManager.cancelPeriodicSync()
            }
        }
    }
    
    /**
     * Habilitar/deshabilitar sincronización automática
     */
    fun setAutoSyncEnabled(enabled: Boolean) {
        viewModelScope.launch {
            syncPreferences.setAutoSyncEnabled(enabled)
            if (enabled) {
                syncWorkManager.schedulePeriodicSync()
            } else {
                syncWorkManager.cancelPeriodicSync()
            }
        }
    }
    
    /**
     * Configurar frecuencia de sincronización
     */
    fun setSyncFrequency(minutes: Int) {
        viewModelScope.launch {
            syncPreferences.setSyncFrequency(minutes)
            // Re-programar con nueva frecuencia
            if (_uiState.value.autoSyncEnabled) {
                syncWorkManager.schedulePeriodicSync()
            }
        }
    }
    
    /**
     * Configurar sincronización solo en WiFi
     */
    fun setSyncOnWifiOnly(wifiOnly: Boolean) {
        viewModelScope.launch {
            syncPreferences.setSyncOnWifiOnly(wifiOnly)
            // Re-programar con nuevas restricciones
            if (_uiState.value.autoSyncEnabled) {
                syncWorkManager.schedulePeriodicSync()
            }
        }
    }
    
    /**
     * Sincronizar manualmente ahora
     */
    fun syncNow() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSyncing = true,
                syncError = null,
                syncSuccess = false
            )
            
            try {
                syncWorkManager.syncNow()
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    syncSuccess = true
                )
                
                // Limpiar éxito después de 3 segundos
                kotlinx.coroutines.delay(3000)
                _uiState.value = _uiState.value.copy(syncSuccess = false)
            } catch (e: Exception) {
                Log.e("SyncViewModel", "Error en sincronización manual", e)
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    syncError = "Error: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Limpiar error
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(syncError = null)
    }
}

