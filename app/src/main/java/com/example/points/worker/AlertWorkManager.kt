package com.example.points.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Gestor de WorkManager para alertas de incidentes y eventos cercanos
 * Implementa la Unidad 7 de Android Basics: WorkManager
 * 
 * Este manager programa trabajos periódicos para verificar:
 * - Incidentes cercanos al usuario
 * - Eventos cercanos al usuario
 * - Crear notificaciones automáticamente
 */
class AlertWorkManager(private val context: Context) {
    
    private val workManager = WorkManager.getInstance(context)
    
    companion object {
        private const val TAG = "AlertWorkManager"
        const val WORK_NAME = "alert_periodic_work"
        
        // Intervalo mínimo de WorkManager es 15 minutos
        private const val MIN_PERIODIC_INTERVAL_MINUTES = 15L
    }
    
    /**
     * Programar verificación periódica de alertas
     * @param radiusKm Radio en kilómetros para buscar incidentes/eventos
     * @param enableIncidents Habilitar alertas de incidentes
     * @param enableEvents Habilitar alertas de eventos
     * @param intervalMinutes Intervalo en minutos (mínimo 15)
     */
    fun schedulePeriodicAlerts(
        radiusKm: Double = 5.0,
        enableIncidents: Boolean = true,
        enableEvents: Boolean = true,
        intervalMinutes: Long = MIN_PERIODIC_INTERVAL_MINUTES
    ) {
        val actualInterval = intervalMinutes.coerceAtLeast(MIN_PERIODIC_INTERVAL_MINUTES)
        
        // Restricciones: requiere conexión a internet y batería no baja
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val workRequest = PeriodicWorkRequestBuilder<AlertWorker>(
            actualInterval,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setInputData(
                androidx.work.Data.Builder()
                    .putDouble(AlertWorker.KEY_RADIUS_KM, radiusKm)
                    .putBoolean(AlertWorker.KEY_ENABLE_INCIDENTS, enableIncidents)
                    .putBoolean(AlertWorker.KEY_ENABLE_EVENTS, enableEvents)
                    .build()
            )
            .addTag("alerts")
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
        
        Log.d(TAG, "Alertas programadas: radio=${radiusKm}km, intervalo=${actualInterval}min")
    }
    
    /**
     * Cancelar verificación periódica de alertas
     */
    fun cancelPeriodicAlerts() {
        workManager.cancelUniqueWork(WORK_NAME)
        Log.d(TAG, "Alertas periódicas canceladas")
    }
    
    /**
     * Ejecutar verificación de alertas inmediatamente (una vez)
     */
    fun checkAlertsNow(
        radiusKm: Double = 5.0,
        enableIncidents: Boolean = true,
        enableEvents: Boolean = true
    ) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val workRequest = androidx.work.OneTimeWorkRequestBuilder<AlertWorker>()
            .setConstraints(constraints)
            .setInputData(
                androidx.work.Data.Builder()
                    .putDouble(AlertWorker.KEY_RADIUS_KM, radiusKm)
                    .putBoolean(AlertWorker.KEY_ENABLE_INCIDENTS, enableIncidents)
                    .putBoolean(AlertWorker.KEY_ENABLE_EVENTS, enableEvents)
                    .build()
            )
            .addTag("alerts")
            .build()
        
        workManager.enqueue(workRequest)
        Log.d(TAG, "Verificación de alertas ejecutada inmediatamente")
    }
    
    /**
     * Verificar si las alertas están activas
     */
    suspend fun isAlertsEnabled(): Boolean {
        return try {
            val workInfos = withContext(Dispatchers.IO) {
                workManager.getWorkInfosForUniqueWork(WORK_NAME).get()
            }
            workInfos.any { workInfo ->
                workInfo.state == androidx.work.WorkInfo.State.ENQUEUED || 
                workInfo.state == androidx.work.WorkInfo.State.RUNNING
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking alerts status", e)
            false
        }
    }
}

