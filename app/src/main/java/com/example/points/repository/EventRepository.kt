package com.example.points.repository

import com.example.points.models.Event
import com.example.points.models.EstadoEvento
import com.example.points.models.CategoriaEvento
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import android.net.Uri
import android.util.Log
import java.util.UUID

class EventRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val eventCollection = firestore.collection("eventos")
    
    // Obtener todos los eventos aprobados en tiempo real
    fun getAllApprovedEvents(): Flow<List<Event>> = callbackFlow {
        val listener = eventCollection
            .whereEqualTo("estado", EstadoEvento.APROBADO.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("EventRepository", "Error getting events", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                val events = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val event = doc.toObject(Event::class.java)?.copy(id = doc.id)
                        // Filtrar eventos cancelados en el cliente
                        if (event != null && !event.cancelado) {
                            event
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        Log.e("EventRepository", "Error parsing event ${doc.id}", e)
                        null
                    }
                } ?: emptyList()
                
                // Ordenar por fecha en el cliente
                val sortedEvents = events.sortedBy { it.fechaInicio }
                trySend(sortedEvents)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Obtener eventos próximos (próximos 7 días)
    fun getUpcomingEvents(): Flow<List<Event>> = callbackFlow {
        val now = com.google.firebase.Timestamp.now()
        val sevenDaysFromNow = com.google.firebase.Timestamp(
            now.seconds + (7 * 24 * 60 * 60), 0
        )
        
        val listener = eventCollection
            .whereEqualTo("estado", EstadoEvento.APROBADO.name)
            .whereGreaterThanOrEqualTo("fechaInicio", now)
            .whereLessThanOrEqualTo("fechaInicio", sevenDaysFromNow)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("EventRepository", "Error getting upcoming events", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                val events = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val event = doc.toObject(Event::class.java)?.copy(id = doc.id)
                        // Filtrar eventos cancelados en el cliente
                        if (event != null && !event.cancelado) {
                            event
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        Log.e("EventRepository", "Error parsing event ${doc.id}", e)
                        null
                    }
                } ?: emptyList()
                
                trySend(events)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Obtener eventos por categoría
    fun getEventsByCategory(categoria: CategoriaEvento): Flow<List<Event>> = callbackFlow {
        val now = com.google.firebase.Timestamp.now()
        
        val listener = eventCollection
            .whereEqualTo("estado", EstadoEvento.APROBADO.name)
            .whereEqualTo("cancelado", false)
            .whereEqualTo("categoria", categoria.name)
            .whereGreaterThanOrEqualTo("fechaFin", now) // Eventos que no han terminado
            .orderBy("fechaFin", Query.Direction.ASCENDING)
            .orderBy("fechaInicio", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("EventRepository", "Error getting events by category", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                val events = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Event::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e("EventRepository", "Error parsing event ${doc.id}", e)
                        null
                    }
                } ?: emptyList()
                
                trySend(events)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Buscar eventos por nombre o descripción
    fun searchEvents(query: String): Flow<List<Event>> = callbackFlow {
        val now = com.google.firebase.Timestamp.now()
        
        val listener = eventCollection
            .whereEqualTo("estado", EstadoEvento.APROBADO.name)
            .whereEqualTo("cancelado", false)
            .whereGreaterThanOrEqualTo("fechaFin", now)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("EventRepository", "Error searching events", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                val allEvents = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Event::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e("EventRepository", "Error parsing event ${doc.id}", e)
                        null
                    }
                } ?: emptyList()
                
                // Filtrar por nombre, descripción o etiquetas en el cliente
                val filteredEvents = allEvents.filter { event ->
                    event.nombre.contains(query, ignoreCase = true) ||
                    event.descripcion.contains(query, ignoreCase = true) ||
                    event.etiquetas.any { tag -> tag.contains(query, ignoreCase = true) }
                }.sortedBy { it.fechaInicio }
                
                trySend(filteredEvents)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Obtener eventos cercanos a una ubicación
    fun getNearbyEvents(lat: Double, lon: Double, radiusKm: Double = 10.0): Flow<List<Event>> = callbackFlow {
        val now = com.google.firebase.Timestamp.now()
        
        val listener = eventCollection
            .whereEqualTo("estado", EstadoEvento.APROBADO.name)
            .whereEqualTo("cancelado", false)
            .whereGreaterThanOrEqualTo("fechaFin", now)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("EventRepository", "Error getting nearby events", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                val allEvents = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Event::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e("EventRepository", "Error parsing event ${doc.id}", e)
                        null
                    }
                } ?: emptyList()
                
                // Filtrar por distancia
                val nearbyEvents = allEvents.filter { event ->
                    val distance = calculateDistance(lat, lon, event.ubicacion.lat, event.ubicacion.lon)
                    distance <= radiusKm
                }.sortedBy { event ->
                    calculateDistance(lat, lon, event.ubicacion.lat, event.ubicacion.lon)
                }
                
                trySend(nearbyEvents)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Obtener eventos pendientes de moderación (para administradores)
    fun getPendingEvents(): Flow<List<Event>> = callbackFlow {
        val listener = eventCollection
            .whereEqualTo("estado", EstadoEvento.PENDIENTE.name)
            .orderBy("fechaCreacion", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("EventRepository", "Error getting pending events", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                val events = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Event::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e("EventRepository", "Error parsing event ${doc.id}", e)
                        null
                    }
                } ?: emptyList()
                
                trySend(events)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Obtener eventos en revisión (para moderadores)
    fun getEventsInReview(): Flow<List<Event>> = callbackFlow {
        val listener = eventCollection
            .whereEqualTo("estado", EstadoEvento.EN_REVISION.name)
            .orderBy("fechaCreacion", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("EventRepository", "Error getting events in review", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                val events = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Event::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e("EventRepository", "Error parsing event ${doc.id}", e)
                        null
                    }
                } ?: emptyList()
                
                trySend(events)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Obtener eventos del usuario actual
    fun getUserEvents(): Flow<List<Event>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(emptyList())
            return@callbackFlow
        }
        
        val listener = eventCollection
            .whereEqualTo("usuarioId", currentUser.uid)
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("EventRepository", "Error getting user events", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                val events = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Event::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e("EventRepository", "Error parsing event ${doc.id}", e)
                        null
                    }
                } ?: emptyList()
                
                trySend(events)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Crear un nuevo evento
    suspend fun createEvent(event: Event): Result<String> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }
            
            val eventWithUser = event.copy(usuarioId = currentUser.uid)
            val docRef = eventCollection.add(eventWithUser).await()
            Log.d("EventRepository", "Evento creado con ID: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e("EventRepository", "Error creating event", e)
            Result.failure(e)
        }
    }
    
    // Actualizar un evento
    suspend fun updateEvent(event: Event): Result<Unit> {
        return try {
            if (event.id.isEmpty()) {
                return Result.failure(Exception("ID del evento no puede estar vacío"))
            }
            
            eventCollection.document(event.id).set(event).await()
            Log.d("EventRepository", "Evento actualizado: ${event.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("EventRepository", "Error updating event", e)
            Result.failure(e)
        }
    }
    
    // Aprobar un evento (para moderadores/administradores)
    suspend fun approveEvent(eventId: String, comentarios: String? = null): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }
            
            val updateData = mapOf(
                "estado" to EstadoEvento.APROBADO.name,
                "moderadorId" to currentUser.uid,
                "fechaModeracion" to com.google.firebase.Timestamp.now(),
                "comentariosModeracion" to comentarios,
                "fechaActualizacion" to com.google.firebase.Timestamp.now()
            )
            
            eventCollection.document(eventId).update(updateData).await()
            Log.d("EventRepository", "Evento aprobado: $eventId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("EventRepository", "Error approving event", e)
            Result.failure(e)
        }
    }
    
    // Rechazar un evento (para moderadores/administradores)
    suspend fun rejectEvent(eventId: String, comentarios: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }
            
            val updateData = mapOf(
                "estado" to EstadoEvento.RECHAZADO.name,
                "moderadorId" to currentUser.uid,
                "fechaModeracion" to com.google.firebase.Timestamp.now(),
                "comentariosModeracion" to comentarios,
                "fechaActualizacion" to com.google.firebase.Timestamp.now()
            )
            
            eventCollection.document(eventId).update(updateData).await()
            Log.d("EventRepository", "Evento rechazado: $eventId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("EventRepository", "Error rejecting event", e)
            Result.failure(e)
        }
    }
    
    // Cancelar un evento
    suspend fun cancelEvent(eventId: String, motivo: String): Result<Unit> {
        return try {
            val updateData = mapOf(
                "cancelado" to true,
                "motivoCancelacion" to motivo,
                "fechaActualizacion" to com.google.firebase.Timestamp.now()
            )
            
            eventCollection.document(eventId).update(updateData).await()
            Log.d("EventRepository", "Evento cancelado: $eventId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("EventRepository", "Error canceling event", e)
            Result.failure(e)
        }
    }
    
    // Inscribirse a un evento
    suspend fun registerToEvent(eventId: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }
            
            // Verificar si el evento existe y tiene cupos
            val eventDoc = eventCollection.document(eventId).get().await()
            if (!eventDoc.exists()) {
                return Result.failure(Exception("Evento no encontrado"))
            }
            
            val event = eventDoc.toObject(Event::class.java)
            if (event == null) {
                return Result.failure(Exception("Error al obtener datos del evento"))
            }
            
            if (event.cancelado || event.estado != EstadoEvento.APROBADO) {
                return Result.failure(Exception("El evento no está disponible para inscripción"))
            }
            
            if (event.capacidad != null && event.inscripciones >= event.capacidad) {
                return Result.failure(Exception("No hay cupos disponibles"))
            }
            
            // Incrementar el contador de inscripciones
            val updateData = mapOf(
                "inscripciones" to (event.inscripciones + 1),
                "fechaActualizacion" to com.google.firebase.Timestamp.now()
            )
            
            eventCollection.document(eventId).update(updateData).await()
            Log.d("EventRepository", "Usuario inscrito al evento: $eventId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("EventRepository", "Error registering to event", e)
            Result.failure(e)
        }
    }
    
    // Subir imagen para un evento
    suspend fun uploadEventImage(eventId: String, imageUri: Uri): Result<String> {
        return try {
            val fileName = "event_${eventId}_${UUID.randomUUID()}.jpg"
            val imageRef = storage.reference.child("event_images/$fileName")
            
            val uploadTask = imageRef.putFile(imageUri).await()
            val downloadUrl = imageRef.downloadUrl.await()
            
            Log.d("EventRepository", "Imagen subida: $downloadUrl")
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Log.e("EventRepository", "Error uploading image", e)
            Result.failure(e)
        }
    }
    
    // Calcular distancia entre dos puntos (fórmula de Haversine)
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // Radio de la Tierra en kilómetros
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        
        return earthRadius * c
    }
    
    // Obtener evento por ID
    suspend fun getEventById(eventId: String): Result<Event?> {
        return try {
            val doc = eventCollection.document(eventId).get().await()
            if (doc.exists()) {
                val event = doc.toObject(Event::class.java)?.copy(id = doc.id)
                Result.success(event)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e("EventRepository", "Error getting event by ID", e)
            Result.failure(e)
        }
    }
    
    // Eliminar un evento (solo para administradores o creador)
    suspend fun deleteEvent(eventId: String): Result<Unit> {
        return try {
            eventCollection.document(eventId).delete().await()
            Log.d("EventRepository", "Evento eliminado: $eventId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("EventRepository", "Error deleting event", e)
            Result.failure(e)
        }
    }
}
