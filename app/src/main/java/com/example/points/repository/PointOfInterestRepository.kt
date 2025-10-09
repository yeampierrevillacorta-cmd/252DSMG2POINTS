package com.example.points.repository

import com.example.points.models.PointOfInterest
import com.example.points.models.EstadoPOI
import com.example.points.models.CategoriaPOI
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

class PointOfInterestRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val poiCollection = firestore.collection("puntos_interes")
    
    // Obtener todos los POIs aprobados en tiempo real
    fun getAllApprovedPOIs(): Flow<List<PointOfInterest>> = callbackFlow {
        val listener = poiCollection
            .whereEqualTo("estado", EstadoPOI.APROBADO.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("POIRepository", "Error getting POIs", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                val pois = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(PointOfInterest::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e("POIRepository", "Error parsing POI ${doc.id}", e)
                        null
                    }
                }?.sortedByDescending { it.fechaCreacion } ?: emptyList()
                
                trySend(pois)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Obtener POIs por categoría
    fun getPOIsByCategory(categoria: CategoriaPOI): Flow<List<PointOfInterest>> = callbackFlow {
        val listener = poiCollection
            .whereEqualTo("estado", EstadoPOI.APROBADO.name)
            .whereEqualTo("categoria", categoria.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("POIRepository", "Error getting POIs by category", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                val pois = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(PointOfInterest::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e("POIRepository", "Error parsing POI ${doc.id}", e)
                        null
                    }
                }?.sortedByDescending { it.fechaCreacion } ?: emptyList()
                
                trySend(pois)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Buscar POIs por nombre o descripción
    fun searchPOIs(query: String): Flow<List<PointOfInterest>> = callbackFlow {
        val listener = poiCollection
            .whereEqualTo("estado", EstadoPOI.APROBADO.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("POIRepository", "Error searching POIs", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                val allPois = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(PointOfInterest::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e("POIRepository", "Error parsing POI ${doc.id}", e)
                        null
                    }
                } ?: emptyList()
                
                // Filtrar por nombre o descripción en el cliente
                val filteredPois = allPois.filter { poi ->
                    poi.nombre.contains(query, ignoreCase = true) ||
                    poi.descripcion.contains(query, ignoreCase = true)
                }.sortedBy { it.nombre }
                
                trySend(filteredPois)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Obtener POIs cercanos a una ubicación
    fun getNearbyPOIs(lat: Double, lon: Double, radiusKm: Double = 5.0): Flow<List<PointOfInterest>> = callbackFlow {
        val listener = poiCollection
            .whereEqualTo("estado", EstadoPOI.APROBADO.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("POIRepository", "Error getting nearby POIs", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                val allPOIs = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(PointOfInterest::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e("POIRepository", "Error parsing POI ${doc.id}", e)
                        null
                    }
                } ?: emptyList()
                
                // Filtrar por distancia
                val nearbyPOIs = allPOIs.filter { poi ->
                    val distance = calculateDistance(lat, lon, poi.ubicacion.lat, poi.ubicacion.lon)
                    distance <= radiusKm
                }.sortedBy { poi ->
                    calculateDistance(lat, lon, poi.ubicacion.lat, poi.ubicacion.lon)
                }
                
                trySend(nearbyPOIs)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Obtener POIs pendientes de moderación (para administradores)
    fun getPendingPOIs(): Flow<List<PointOfInterest>> = callbackFlow {
        val listener = poiCollection
            .whereEqualTo("estado", EstadoPOI.PENDIENTE.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("POIRepository", "Error getting pending POIs", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                val pois = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(PointOfInterest::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e("POIRepository", "Error parsing POI ${doc.id}", e)
                        null
                    }
                }?.sortedBy { it.fechaCreacion } ?: emptyList()
                
                trySend(pois)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Obtener POIs en revisión (para moderadores)
    fun getPOIsInReview(): Flow<List<PointOfInterest>> = callbackFlow {
        val listener = poiCollection
            .whereEqualTo("estado", EstadoPOI.EN_REVISION.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("POIRepository", "Error getting POIs in review", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                val pois = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(PointOfInterest::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e("POIRepository", "Error parsing POI ${doc.id}", e)
                        null
                    }
                }?.sortedBy { it.fechaCreacion } ?: emptyList()
                
                trySend(pois)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Crear un nuevo POI
    suspend fun createPOI(poi: PointOfInterest): Result<String> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }
            
            val poiWithUser = poi.copy(usuarioId = currentUser.uid)
            val docRef = poiCollection.add(poiWithUser).await()
            Log.d("POIRepository", "POI creado con ID: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e("POIRepository", "Error creating POI", e)
            Result.failure(e)
        }
    }
    
    // Actualizar un POI
    suspend fun updatePOI(poi: PointOfInterest): Result<Unit> {
        return try {
            if (poi.id.isEmpty()) {
                return Result.failure(Exception("ID del POI no puede estar vacío"))
            }
            
            poiCollection.document(poi.id).set(poi).await()
            Log.d("POIRepository", "POI actualizado: ${poi.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("POIRepository", "Error updating POI", e)
            Result.failure(e)
        }
    }
    
    // Aprobar un POI (para moderadores/administradores)
    suspend fun approvePOI(poiId: String, comentarios: String? = null): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }
            
            val updateData = mapOf(
                "estado" to EstadoPOI.APROBADO.name,
                "moderadorId" to currentUser.uid,
                "fechaModeracion" to com.google.firebase.Timestamp.now(),
                "comentariosModeracion" to comentarios,
                "fechaActualizacion" to com.google.firebase.Timestamp.now()
            )
            
            poiCollection.document(poiId).update(updateData).await()
            Log.d("POIRepository", "POI aprobado: $poiId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("POIRepository", "Error approving POI", e)
            Result.failure(e)
        }
    }
    
    // Rechazar un POI (para moderadores/administradores)
    suspend fun rejectPOI(poiId: String, comentarios: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }
            
            val updateData = mapOf(
                "estado" to EstadoPOI.RECHAZADO.name,
                "moderadorId" to currentUser.uid,
                "fechaModeracion" to com.google.firebase.Timestamp.now(),
                "comentariosModeracion" to comentarios,
                "fechaActualizacion" to com.google.firebase.Timestamp.now()
            )
            
            poiCollection.document(poiId).update(updateData).await()
            Log.d("POIRepository", "POI rechazado: $poiId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("POIRepository", "Error rejecting POI", e)
            Result.failure(e)
        }
    }
    
    // Subir imagen para un POI
    suspend fun uploadPOIImage(poiId: String, imageUri: Uri): Result<String> {
        return try {
            val fileName = "poi_${poiId}_${UUID.randomUUID()}.jpg"
            val imageRef = storage.reference.child("poi_images/$fileName")
            
            val uploadTask = imageRef.putFile(imageUri).await()
            val downloadUrl = imageRef.downloadUrl.await()
            
            Log.d("POIRepository", "Imagen subida: $downloadUrl")
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Log.e("POIRepository", "Error uploading image", e)
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
    
    // Obtener POI por ID
    suspend fun getPOIById(poiId: String): Result<PointOfInterest?> {
        return try {
            val doc = poiCollection.document(poiId).get().await()
            if (doc.exists()) {
                val poi = doc.toObject(PointOfInterest::class.java)?.copy(id = doc.id)
                Result.success(poi)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e("POIRepository", "Error getting POI by ID", e)
            Result.failure(e)
        }
    }
    
    // Eliminar un POI (solo para administradores)
    suspend fun deletePOI(poiId: String): Result<Unit> {
        return try {
            poiCollection.document(poiId).delete().await()
            Log.d("POIRepository", "POI eliminado: $poiId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("POIRepository", "Error deleting POI", e)
            Result.failure(e)
        }
    }
}
