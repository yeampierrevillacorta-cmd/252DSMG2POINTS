package com.example.points.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.points.PointsApplication
import com.example.points.data.PreferencesManager
import com.example.points.repository.SyncRepository
import com.example.points.worker.SyncWorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

data class SyncSettingsUiState(
    val autoSyncEnabled: Boolean = true,
    val autoSyncIntervalHours: Int = 6,
    val syncOnlyWifi: Boolean = false,
    val isSyncing: Boolean = false,
    val lastSyncTimestamp: String? = null,
    val syncStatus: SyncStatus = SyncStatus.Idle,
    val errorMessage: String? = null,
    val isLoading: Boolean = false
)

enum class SyncStatus {
    Idle,
    Syncing,
    Success,
    Error
}

class SyncSettingsViewModel(
    private val application: Application,
    private val preferencesManager: PreferencesManager,
    private val syncRepository: SyncRepository?
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SyncSettingsUiState())
    val uiState: StateFlow<SyncSettingsUiState> = _uiState.asStateFlow()
    
    private val syncWorkManager = SyncWorkManager(application, preferencesManager)
    
    init {
        loadSettings()
        loadLastSyncTimestamp()
    }
    
    /**
     * Carga la configuración actual desde PreferencesManager
     */
    private fun loadSettings() {
        _uiState.value = _uiState.value.copy(
            autoSyncEnabled = preferencesManager.autoSyncEnabled,
            autoSyncIntervalHours = preferencesManager.autoSyncIntervalHours,
            syncOnlyWifi = preferencesManager.syncOnlyWifi
        )
    }
    
    /**
     * Carga el timestamp de la última sincronización
     */
    private fun loadLastSyncTimestamp() {
        viewModelScope.launch {
            val timestamp = syncRepository?.getLastSyncTimestamp()
            _uiState.value = _uiState.value.copy(
                lastSyncTimestamp = timestamp
            )
        }
    }
    
    /**
     * Habilita o deshabilita la sincronización automática
     */
    fun setAutoSyncEnabled(enabled: Boolean) {
        preferencesManager.autoSyncEnabled = enabled
        _uiState.value = _uiState.value.copy(autoSyncEnabled = enabled)
        
        if (enabled) {
            // Iniciar sincronización automática
            syncWorkManager.startPeriodicSync(
                intervalHours = _uiState.value.autoSyncIntervalHours.toLong(),
                onlyWifi = _uiState.value.syncOnlyWifi
            )
            Log.d("SyncSettingsViewModel", "Sincronización automática habilitada")
        } else {
            // Detener sincronización automática
            syncWorkManager.stopPeriodicSync()
            Log.d("SyncSettingsViewModel", "Sincronización automática deshabilitada")
        }
    }
    
    /**
     * Cambia el intervalo de sincronización
     */
    fun setSyncInterval(hours: Int) {
        val validHours = hours.coerceIn(1, 24)
        preferencesManager.autoSyncIntervalHours = validHours
        _uiState.value = _uiState.value.copy(autoSyncIntervalHours = validHours)
        
        // Reiniciar sincronización con nuevo intervalo si está habilitada
        if (_uiState.value.autoSyncEnabled) {
            syncWorkManager.restartPeriodicSync()
        }
        
        Log.d("SyncSettingsViewModel", "Intervalo de sincronización actualizado: $validHours horas")
    }
    
    /**
     * Cambia la preferencia de sincronizar solo con WiFi
     */
    fun setSyncOnlyWifi(onlyWifi: Boolean) {
        preferencesManager.syncOnlyWifi = onlyWifi
        _uiState.value = _uiState.value.copy(syncOnlyWifi = onlyWifi)
        
        // Reiniciar sincronización con nueva configuración si está habilitada
        if (_uiState.value.autoSyncEnabled) {
            syncWorkManager.restartPeriodicSync()
        }
        
        Log.d("SyncSettingsViewModel", "Sincronizar solo WiFi: $onlyWifi")
    }
    
    /**
     * Ejecuta una sincronización inmediata
     */
    fun syncNow() {
        if (syncRepository == null) {
            _uiState.value = _uiState.value.copy(
                syncStatus = SyncStatus.Error,
                errorMessage = "Repositorio de sincronización no disponible"
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSyncing = true,
                syncStatus = SyncStatus.Syncing,
                errorMessage = null
            )
            
            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    syncStatus = SyncStatus.Error,
                    errorMessage = "Usuario no autenticado"
                )
                return@launch
            }
            
            val result = syncRepository.sync(currentUser.uid)
            
            result.onSuccess { syncResult ->
                // Actualizar timestamp
                loadLastSyncTimestamp()
                
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    syncStatus = SyncStatus.Success,
                    errorMessage = null
                )
                
                Log.d("SyncSettingsViewModel", "Sincronización exitosa: ${syncResult.message}")
                
                // Limpiar estado de éxito después de 3 segundos
                viewModelScope.launch {
                    kotlinx.coroutines.delay(3000)
                    if (_uiState.value.syncStatus == SyncStatus.Success) {
                        _uiState.value = _uiState.value.copy(syncStatus = SyncStatus.Idle)
                    }
                }
            }.onFailure { error ->
                // Mensaje de error más descriptivo
                val errorMessage = when {
                    error.message?.contains("403") == true -> {
                        "Error 403: El backend rechazó la petición. " +
                        "Verifica que el backend permita acceso a /api/v1/sync/** " +
                        "(ver PROMPT_MODIFICAR_BACKEND.md)"
                    }
                    error.message?.contains("401") == true -> {
                        "Error 401: No autorizado. Verifica que estés autenticado."
                    }
                    error.message?.contains("Unable to resolve host") == true -> {
                        "Error de conexión: No se pudo conectar al servidor. Verifica tu conexión a internet."
                    }
                    else -> {
                        error.message ?: "Error desconocido al sincronizar"
                    }
                }
                
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    syncStatus = SyncStatus.Error,
                    errorMessage = errorMessage
                )
                
                Log.e("SyncSettingsViewModel", "Error en sincronización: ${error.message}", error)
            }
        }
    }
    
    /**
     * Formatea el timestamp de última sincronización para mostrar
     */
    fun formatLastSyncTimestamp(): String {
        val timestamp = _uiState.value.lastSyncTimestamp ?: return "Nunca"
        
        return try {
            // Parsear ISO 8601 timestamp
            val instant = java.time.Instant.parse(timestamp)
            val dateTime = java.time.LocalDateTime.ofInstant(
                instant,
                java.time.ZoneId.systemDefault()
            )
            val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            "Última sincronización: ${dateTime.format(formatter)}"
        } catch (e: Exception) {
            "Última sincronización: $timestamp"
        }
    }
}

