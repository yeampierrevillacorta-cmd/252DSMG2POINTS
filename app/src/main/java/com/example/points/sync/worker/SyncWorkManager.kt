package com.example.points.sync.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.points.sync.data.SyncPreferences
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Gestor de WorkManager para sincronización periódica
 * Implementa la Unidad 7 de Android Basics: WorkManager
 */
class SyncWorkManager(private val context: Context) {
    
    companion object {
        private const val SYNC_WORK_NAME = "points_sync_work"
        private const val MIN_PERIODIC_INTERVAL_MINUTES = 15L // Mínimo de WorkManager
    }
    
    private val workManager = WorkManager.getInstance(context)
    private val syncPreferences = SyncPreferences(context)
    
    /**
     * Programar sincronización periódica
     */
    suspend fun schedulePeriodicSync() {
        val syncEnabled = syncPreferences.getAutoSyncEnabled()
        if (!syncEnabled) {
            cancelPeriodicSync()
            return
        }
        
        val frequencyMinutes = syncPreferences.getSyncFrequency()
        val wifiOnly = syncPreferences.syncOnWifiOnly.first()
        
        // WorkManager requiere mínimo 15 minutos para trabajos periódicos
        val intervalMinutes = frequencyMinutes.coerceAtLeast(MIN_PERIODIC_INTERVAL_MINUTES.toInt())
        
        // Crear restricciones
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(
                if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
            )
            .setRequiresBatteryNotLow(true)
            .build()
        
        // Crear trabajo periódico
        val syncWork = PeriodicWorkRequestBuilder<SyncWorker>(
            intervalMinutes.toLong(),
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(SYNC_WORK_NAME)
            .build()
        
        // Programar trabajo (reemplaza si ya existe)
        workManager.enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            syncWork
        )
    }
    
    /**
     * Cancelar sincronización periódica
     */
    fun cancelPeriodicSync() {
        workManager.cancelUniqueWork(SYNC_WORK_NAME)
    }
    
    /**
     * Ejecutar sincronización inmediata (una sola vez)
     */
    suspend fun syncNow() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncWork = androidx.work.OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .addTag("sync_now")
            .build()
        
        workManager.enqueue(syncWork)
    }
    
    /**
     * Verificar si hay trabajo programado
     */
    fun hasScheduledWork(): Boolean {
        val workInfos = workManager.getWorkInfosForUniqueWork(SYNC_WORK_NAME).get()
        return workInfos.any { it.state == androidx.work.WorkInfo.State.ENQUEUED || 
                              it.state == androidx.work.WorkInfo.State.RUNNING }
    }
}

