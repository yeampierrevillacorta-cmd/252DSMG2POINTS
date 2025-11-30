package com.example.points.worker

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.points.data.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Manager para configurar y gestionar la sincronización automática con WorkManager
 */
class SyncWorkManager(
    private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    
    companion object {
        private const val TAG = "SyncWorkManager"
        const val WORK_NAME = "sync_work"
        
        // Intervalos mínimos y máximos permitidos
        private const val MIN_INTERVAL_HOURS = 1L
        private const val MAX_INTERVAL_HOURS = 24L
        private const val DEFAULT_INTERVAL_HOURS = 6L
    }
    
    private val workManager = WorkManager.getInstance(context)
    
    /**
     * Inicia la sincronización automática periódica
     * 
     * @param intervalHours Intervalo entre sincronizaciones (en horas)
     * @param onlyWifi Si es true, solo sincroniza cuando hay WiFi
     */
    fun startPeriodicSync(
        intervalHours: Long = preferencesManager.autoSyncIntervalHours.toLong(),
        onlyWifi: Boolean = preferencesManager.syncOnlyWifi
    ) {
        // Validar intervalo
        val validInterval = intervalHours.coerceIn(MIN_INTERVAL_HOURS, MAX_INTERVAL_HOURS)
        
        if (validInterval != intervalHours) {
            Log.w(TAG, "Intervalo ajustado de $intervalHours a $validInterval horas")
        }
        
        // Crear constraints
        val constraints = Constraints.Builder().apply {
            // Requerir conexión a internet
            setRequiredNetworkType(
                if (onlyWifi) NetworkType.UNMETERED // WiFi o Ethernet
                else NetworkType.CONNECTED // Cualquier conexión
            )
            
            // Opcional: solo cuando el dispositivo está cargando (para ahorrar batería)
            // setRequiresCharging(true)
            
            // Opcional: solo cuando el dispositivo tiene suficiente batería
            // setRequiresBatteryNotLow(true)
        }.build()
        
        // Crear trabajo periódico
        val syncWork = PeriodicWorkRequestBuilder<SyncWorker>(
            validInterval,
            TimeUnit.HOURS,
            // Flex interval: tiempo mínimo antes de que pueda ejecutarse
            // Por ejemplo, si el intervalo es 6 horas, el flex puede ser 1 hora
            // Esto significa que el trabajo puede ejecutarse entre las horas 5-6
            validInterval.coerceAtMost(1),
            TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .addTag(WORK_NAME)
            .build()
        
        // Programar trabajo (KEEP reemplaza el trabajo existente si hay uno)
        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncWork
        )
        
        Log.d(TAG, "✅ Sincronización automática iniciada:")
        Log.d(TAG, "   - Intervalo: cada $validInterval horas")
        Log.d(TAG, "   - Solo WiFi: $onlyWifi")
    }
    
    /**
     * Detiene la sincronización automática
     */
    fun stopPeriodicSync() {
        workManager.cancelUniqueWork(WORK_NAME)
        Log.d(TAG, "🛑 Sincronización automática detenida")
    }
    
    /**
     * Reinicia la sincronización automática con las preferencias actuales
     */
    fun restartPeriodicSync() {
        if (preferencesManager.autoSyncEnabled) {
            stopPeriodicSync()
            startPeriodicSync()
        } else {
            stopPeriodicSync()
        }
    }
    
    /**
     * Ejecuta una sincronización única inmediata (one-time work)
     */
    fun syncNow() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncWork = androidx.work.OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .addTag("sync_now")
            .build()
        
        workManager.enqueue(syncWork)
        Log.d(TAG, "🔄 Sincronización inmediata programada")
    }
    
    /**
     * Verifica si la sincronización automática está activa
     */
    suspend fun isSyncActive(): Boolean {
        return withContext(Dispatchers.IO) {
            val workInfos = workManager.getWorkInfosForUniqueWork(WORK_NAME).get()
            workInfos.any { it.state == androidx.work.WorkInfo.State.ENQUEUED || 
                          it.state == androidx.work.WorkInfo.State.RUNNING }
        }
    }
    
    /**
     * Verifica si hay conexión a internet disponible
     */
    private fun hasInternetConnection(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val network = connectivityManager?.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * Verifica si hay conexión WiFi disponible
     */
    private fun hasWifiConnection(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val network = connectivityManager?.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
               capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
}

