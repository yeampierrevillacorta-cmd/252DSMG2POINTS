package com.example.points.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.points.models.Incident
import com.example.points.models.Event
import com.example.points.models.Notification
import com.example.points.models.TipoNotificacion
import com.example.points.repository.IncidentRepository
import com.example.points.repository.EventRepository
import com.example.points.repository.NotificationRepository
import com.example.points.services.LocationService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.first

/**
 * Worker de WorkManager para verificar incidentes y eventos cercanos
 * Implementa la Unidad 7 de Android Basics: WorkManager
 * 
 * Este worker se ejecuta periódicamente para:
 * 1. Obtener la ubicación actual del usuario
 * 2. Buscar incidentes y eventos cercanos
 * 3. Crear notificaciones para nuevos incidentes/eventos
 * 4. Enviar notificaciones push locales
 */
class AlertWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val incidentRepository = IncidentRepository()
    private val eventRepository = EventRepository()
    private val notificationRepository = NotificationRepository()
    private val locationService = LocationService(context)
    private val auth = FirebaseAuth.getInstance()
    
    companion object {
        private const val TAG = "AlertWorker"
        const val WORK_NAME = "alert_check_work"
        
        // Claves para parámetros de entrada
        const val KEY_RADIUS_KM = "radius_km"
        const val KEY_ENABLE_INCIDENTS = "enable_incidents"
        const val KEY_ENABLE_EVENTS = "enable_events"
    }
    
    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Iniciando verificación de alertas")
            
            val userId = auth.currentUser?.uid
            if (userId == null) {
                Log.w(TAG, "Usuario no autenticado, cancelando verificación")
                return Result.success()
            }
            
            // Obtener parámetros de configuración
            val radiusKm = inputData.getDouble(KEY_RADIUS_KM, 5.0)
            val enableIncidents = inputData.getBoolean(KEY_ENABLE_INCIDENTS, true)
            val enableEvents = inputData.getBoolean(KEY_ENABLE_EVENTS, true)
            
            // Verificar permisos de ubicación
            if (!locationService.checkPermissions()) {
                Log.w(TAG, "Permisos de ubicación no concedidos")
                return Result.success()
            }
            
            // Verificar si la ubicación está habilitada
            if (!locationService.isLocationEnabled()) {
                Log.w(TAG, "Ubicación deshabilitada en el dispositivo")
                return Result.success()
            }
            
            // Obtener ubicación actual del usuario
            // Intentar obtener ubicación actualizada, si falla usar última conocida
            var locationState = locationService.getCurrentLocation()
            var userLat = locationState.latitude
            var userLon = locationState.longitude
            
            // Si no hay ubicación, intentar forzar actualización
            if (userLat == null || userLon == null) {
                Log.d(TAG, "No hay ubicación actual, intentando obtener última ubicación conocida...")
                locationState = locationService.forceLocationUpdate()
                userLat = locationState.latitude
                userLon = locationState.longitude
            }
            
            if (userLat == null || userLon == null) {
                Log.w(TAG, "No se pudo obtener ubicación del usuario. Verifica que los permisos de ubicación estén concedidos y que el GPS esté activado.")
                return Result.success() // No reintentar si no hay ubicación (evita loops infinitos)
            }
            
            Log.d(TAG, "Ubicación del usuario: lat=$userLat, lon=$userLon, radio=${radiusKm}km")
            
            // Obtener notificaciones existentes para evitar duplicados
            // Usar first() para obtener el primer valor del Flow
            val existingNotifications = notificationRepository.getUserNotifications(userId).first()
            val existingIncidentIds = existingNotifications
                .filter { it.tipo == TipoNotificacion.INCIDENTE && it.incidenteId != null }
                .mapNotNull { it.incidenteId }
                .toSet()
            val existingEventIds = existingNotifications
                .filter { it.tipo == TipoNotificacion.EVENTO && it.eventoId != null }
                .mapNotNull { it.eventoId }
                .toSet()
            
            var newNotificationsCount = 0
            
            // Verificar incidentes cercanos
            if (enableIncidents) {
                val nearbyIncidents = incidentRepository.getNearbyIncidents(
                    userLat, 
                    userLon, 
                    radiusKm
                ).first()
                
                Log.d(TAG, "Encontrados ${nearbyIncidents.size} incidentes cercanos")
                
                nearbyIncidents.forEach { incident ->
                    // Solo crear notificación si no existe ya
                    if (!existingIncidentIds.contains(incident.id)) {
                        val distance = locationService.calculateDistance(
                            userLat, userLon,
                            incident.ubicacion.lat, incident.ubicacion.lon
                        )
                        
                        val notification = Notification(
                            tipo = TipoNotificacion.INCIDENTE,
                            mensaje = "Nuevo incidente de ${incident.tipo} a ${String.format("%.1f", distance)} km",
                            fechaHora = Timestamp.now(),
                            usuarioId = userId,
                            leida = false,
                            incidenteId = incident.id
                        )
                        
                        notificationRepository.createNotification(notification)
                        newNotificationsCount++
                        
                        Log.d(TAG, "Notificación creada para incidente: ${incident.id}")
                    }
                }
            }
            
            // Verificar eventos cercanos
            if (enableEvents) {
                val nearbyEvents = eventRepository.getNearbyEvents(
                    userLat,
                    userLon,
                    radiusKm
                ).first()
                
                Log.d(TAG, "Encontrados ${nearbyEvents.size} eventos cercanos")
                
                nearbyEvents.forEach { event ->
                    // Solo crear notificación si no existe ya
                    if (!existingEventIds.contains(event.id)) {
                        val distance = locationService.calculateDistance(
                            userLat, userLon,
                            event.ubicacion.lat, event.ubicacion.lon
                        )
                        
                        val notification = Notification(
                            tipo = TipoNotificacion.EVENTO,
                            mensaje = "Nuevo evento: ${event.nombre} a ${String.format("%.1f", distance)} km",
                            fechaHora = Timestamp.now(),
                            usuarioId = userId,
                            leida = false,
                            eventoId = event.id
                        )
                        
                        notificationRepository.createNotification(notification)
                        newNotificationsCount++
                        
                        Log.d(TAG, "Notificación creada para evento: ${event.id}")
                    }
                }
            }
            
            Log.d(TAG, "Verificación completada. $newNotificationsCount nuevas notificaciones creadas")
            
            // Si hay nuevas notificaciones, programar una notificación push local
            if (newNotificationsCount > 0) {
                NotificationHelper.showNotification(
                    applicationContext,
                    newNotificationsCount
                )
            }
            
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error en AlertWorker", e)
            Result.retry() // Reintentar en caso de error
        }
    }
}

