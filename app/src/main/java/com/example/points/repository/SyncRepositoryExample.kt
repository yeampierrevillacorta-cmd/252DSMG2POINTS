package com.example.points.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Ejemplo de uso del SyncRepository
 * 
 * Este archivo muestra cómo usar el repositorio de sincronización
 * para sincronizar datos con el backend Spring Boot.
 * 
 * NOTA: Este es un archivo de ejemplo/documentación.
 * Puedes eliminar este archivo o usarlo como referencia.
 */
object SyncRepositoryExample {
    
    private const val TAG = "SyncRepositoryExample"
    private val auth = FirebaseAuth.getInstance()
    
    /**
     * Ejemplo 1: Sincronización completa (pull + push)
     * 
     * Este método realiza una sincronización bidireccional:
     * 1. Obtiene cambios del servidor (pull)
     * 2. Fusiona con datos locales
     * 3. Envía cambios locales al servidor (push)
     */
    fun ejemploSincronizacionCompleta(
        syncRepository: SyncRepository,
        scope: CoroutineScope
    ) {
        scope.launch(Dispatchers.IO) {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.w(TAG, "Usuario no autenticado, no se puede sincronizar")
                return@launch
            }
            
            val userId = currentUser.uid
            
            // Realizar sincronización completa
            val result = syncRepository.sync(userId)
            
            result.onSuccess { syncResult ->
                Log.d(TAG, "✅ Sincronización exitosa:")
                Log.d(TAG, "   - Favoritos agregados: ${syncResult.favoritesAdded}")
                Log.d(TAG, "   - Favoritos actualizados: ${syncResult.favoritesUpdated}")
                Log.d(TAG, "   - Favoritos eliminados: ${syncResult.favoritesRemoved}")
                Log.d(TAG, "   - Mensaje: ${syncResult.message}")
                Log.d(TAG, "   - Timestamp del servidor: ${syncResult.serverTimestamp}")
            }.onFailure { error ->
                Log.e(TAG, "❌ Error en sincronización: ${error.message}", error)
            }
        }
    }
    
    /**
     * Ejemplo 2: Solo obtener cambios del servidor (pull)
     * 
     * Útil cuando solo quieres actualizar datos locales
     * sin enviar cambios al servidor.
     */
    fun ejemploSoloPull(
        syncRepository: SyncRepository,
        scope: CoroutineScope
    ) {
        scope.launch(Dispatchers.IO) {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.w(TAG, "Usuario no autenticado")
                return@launch
            }
            
            val userId = currentUser.uid
            
            // Solo obtener cambios del servidor
            val result = syncRepository.pullChanges(userId)
            
            result.onSuccess { syncResult ->
                Log.d(TAG, "✅ Pull exitoso:")
                Log.d(TAG, "   - Favoritos recibidos: ${syncResult.favoritesAdded + syncResult.favoritesUpdated}")
                Log.d(TAG, "   - Timestamp: ${syncResult.serverTimestamp}")
            }.onFailure { error ->
                Log.e(TAG, "❌ Error en pull: ${error.message}", error)
            }
        }
    }
    
    /**
     * Ejemplo 3: Solo enviar cambios al servidor (push)
     * 
     * Útil cuando solo quieres subir cambios locales
     * sin obtener cambios del servidor.
     */
    fun ejemploSoloPush(
        syncRepository: SyncRepository,
        scope: CoroutineScope
    ) {
        scope.launch(Dispatchers.IO) {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.w(TAG, "Usuario no autenticado")
                return@launch
            }
            
            val userId = currentUser.uid
            
            // Solo enviar cambios al servidor
            val result = syncRepository.pushChanges(userId)
            
            result.onSuccess {
                Log.d(TAG, "✅ Push exitoso: Cambios enviados al servidor")
            }.onFailure { error ->
                Log.e(TAG, "❌ Error en push: ${error.message}", error)
            }
        }
    }
    
    /**
     * Ejemplo 4: Verificar última sincronización
     * 
     * Útil para mostrar al usuario cuándo fue la última sincronización
     * o para decidir si es necesario sincronizar.
     */
    fun ejemploVerificarUltimaSincronizacion(
        syncRepository: SyncRepository,
        scope: CoroutineScope
    ) {
        scope.launch(Dispatchers.IO) {
            val lastSync = syncRepository.getLastSyncTimestamp()
            
            if (lastSync != null) {
                Log.d(TAG, "Última sincronización: $lastSync")
                // Aquí podrías parsear la fecha y mostrar al usuario
            } else {
                Log.d(TAG, "Nunca se ha sincronizado")
                // Podrías mostrar un mensaje al usuario indicando que necesita sincronizar
            }
        }
    }
    
    /**
     * Ejemplo 5: Uso en un ViewModel
     * 
     * Ejemplo de cómo usar el repositorio desde un ViewModel
     */
    /*
    class SyncViewModel(
        private val syncRepository: SyncRepository?
    ) : ViewModel() {
        
        private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
        val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
        
        fun sync() {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null || syncRepository == null) {
                _syncState.value = SyncState.Error("Usuario no autenticado o repositorio no disponible")
                return
            }
            
            viewModelScope.launch {
                _syncState.value = SyncState.Syncing
                
                val result = syncRepository.sync(currentUser.uid)
                
                result.onSuccess { syncResult ->
                    _syncState.value = SyncState.Success(syncResult)
                }.onFailure { error ->
                    _syncState.value = SyncState.Error(error.message ?: "Error desconocido")
                }
            }
        }
    }
    
    sealed class SyncState {
        object Idle : SyncState()
        object Syncing : SyncState()
        data class Success(val result: SyncResult) : SyncState()
        data class Error(val message: String) : SyncState()
    }
    */
}

