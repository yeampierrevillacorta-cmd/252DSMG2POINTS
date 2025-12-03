package com.example.points.sync.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Gestor de preferencias de sincronización usando DataStore
 * Implementa almacenamiento de preferencias para la Unidad 6 de Android Basics
 */
class SyncPreferences(private val context: Context) {
    
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sync_preferences")
        
        // Claves de preferencias
        private val KEY_SYNC_ENABLED = booleanPreferencesKey("sync_enabled")
        private val KEY_SYNC_FREQUENCY = intPreferencesKey("sync_frequency") // minutos
        private val KEY_SYNC_ON_WIFI_ONLY = booleanPreferencesKey("sync_on_wifi_only")
        private val KEY_LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
        private val KEY_AUTO_SYNC_ENABLED = booleanPreferencesKey("auto_sync_enabled")
    }
    
    /**
     * Sincronización automática habilitada
     */
    val syncEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_SYNC_ENABLED] ?: true
    }
    
    /**
     * Frecuencia de sincronización en minutos
     */
    val syncFrequency: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_SYNC_FREQUENCY] ?: 60 // Por defecto cada hora
    }
    
    /**
     * Sincronizar solo en WiFi
     */
    val syncOnWifiOnly: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_SYNC_ON_WIFI_ONLY] ?: false
    }
    
    /**
     * Última sincronización (timestamp)
     */
    val lastSyncTimestamp: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[KEY_LAST_SYNC_TIMESTAMP] ?: 0L
    }
    
    /**
     * Sincronización automática habilitada
     */
    val autoSyncEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_AUTO_SYNC_ENABLED] ?: true
    }
    
    /**
     * Habilitar/deshabilitar sincronización
     */
    suspend fun setSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SYNC_ENABLED] = enabled
        }
    }
    
    /**
     * Configurar frecuencia de sincronización
     */
    suspend fun setSyncFrequency(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SYNC_FREQUENCY] = minutes
        }
    }
    
    /**
     * Configurar sincronización solo en WiFi
     */
    suspend fun setSyncOnWifiOnly(wifiOnly: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SYNC_ON_WIFI_ONLY] = wifiOnly
        }
    }
    
    /**
     * Actualizar timestamp de última sincronización
     */
    suspend fun updateLastSyncTimestamp(timestamp: Long = System.currentTimeMillis()) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LAST_SYNC_TIMESTAMP] = timestamp
        }
    }
    
    /**
     * Habilitar/deshabilitar sincronización automática
     */
    suspend fun setAutoSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AUTO_SYNC_ENABLED] = enabled
        }
    }
    
    /**
     * Obtener valores actuales (no reactivos)
     */
    suspend fun getSyncEnabled(): Boolean {
        return context.dataStore.data.map { it[KEY_SYNC_ENABLED] ?: true }.first()
    }
    
    suspend fun getSyncFrequency(): Int {
        return context.dataStore.data.map { it[KEY_SYNC_FREQUENCY] ?: 60 }.first()
    }
    
    suspend fun getAutoSyncEnabled(): Boolean {
        return context.dataStore.data.map { it[KEY_AUTO_SYNC_ENABLED] ?: true }.first()
    }
}

