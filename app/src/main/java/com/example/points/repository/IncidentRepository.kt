package com.example.points.repository

import com.example.points.models.Incident
import com.example.points.models.EstadoIncidente
import com.example.points.models.TipoIncidente
import com.example.points.models.Ubicacion
import com.example.points.models.detection.DetectionResponse
import com.example.points.network.DetectionApiService
import com.example.points.utils.EnvironmentConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.withContext
import android.content.Context
import android.net.Uri
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import java.util.UUID

class IncidentRepository(
    private val detectionApiService: DetectionApiService? = null
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val incidentsCollection = firestore.collection("incidentes")
    
    // Obtener todos los incidentes en tiempo real
    fun getAllIncidents(): Flow<List<Incident>> = callbackFlow {
        val listener = incidentsCollection
            .orderBy("fechaHora", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("IncidentRepository", "Error en listener de Firebase: ${error.message}", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                val incidents = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data
                        Incident(
                            id = doc.id,
                            tipo = data?.get("tipo") as? String ?: "",
                            descripcion = data?.get("descripcion") as? String ?: "",
                            fotoUrl = data?.get("fotoUrl") as? String,
                            videoUrl = data?.get("videoUrl") as? String,
                            ubicacion = try {
                                val ubicacionData = data?.get("ubicacion") as? Map<String, Any>
                                Ubicacion(
                                    lat = (ubicacionData?.get("lat") as? Number)?.toDouble() ?: 0.0,
                                    lon = (ubicacionData?.get("lon") as? Number)?.toDouble() ?: 0.0,
                                    direccion = ubicacionData?.get("direccion") as? String ?: ""
                                )
                            } catch (e: Exception) {
                                Ubicacion()
                            },
                            fechaHora = data?.get("fechaHora") as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now(),
                            estado = try {
                                val estadoString = data?.get("estado") as? String
                                if (estadoString != null) {
                                    EstadoIncidente.fromString(estadoString)
                                } else {
                                    EstadoIncidente.PENDIENTE
                                }
                            } catch (e: Exception) {
                                EstadoIncidente.PENDIENTE
                            },
                            usuarioId = data?.get("usuarioId") as? String ?: "",
                            prioridad = data?.get("prioridad") as? String,
                            etiqueta_ia = data?.get("etiqueta_ia") as? String
                        )
                    } catch (e: Exception) {
                        Log.e("IncidentRepository", "Error parseando documento ${doc.id}: ${e.message}")
                        null
                    }
                } ?: emptyList()
                
                trySend(incidents)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Obtener incidentes filtrados
    fun getIncidentsByType(tipo: TipoIncidente): Flow<List<Incident>> = callbackFlow {
        val listener = incidentsCollection
            .whereEqualTo("tipo", tipo.displayName)
            .orderBy("fechaHora", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val incidents = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data
                        val incident = Incident(
                            id = doc.id,
                            tipo = data?.get("tipo") as? String ?: "",
                            descripcion = data?.get("descripcion") as? String ?: "",
                            fotoUrl = data?.get("fotoUrl") as? String,
                            videoUrl = data?.get("videoUrl") as? String,
                            ubicacion = try {
                                val ubicacionData = data?.get("ubicacion") as? Map<String, Any>
                                Ubicacion(
                                    lat = (ubicacionData?.get("lat") as? Number)?.toDouble() ?: 0.0,
                                    lon = (ubicacionData?.get("lon") as? Number)?.toDouble() ?: 0.0,
                                    direccion = ubicacionData?.get("direccion") as? String ?: ""
                                )
                            } catch (e: Exception) {
                                Ubicacion()
                            },
                            fechaHora = data?.get("fechaHora") as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now(),
                            estado = try {
                                val estadoString = data?.get("estado") as? String
                                if (estadoString != null) {
                                    EstadoIncidente.fromString(estadoString)
                                } else {
                                    EstadoIncidente.PENDIENTE
                                }
                            } catch (e: Exception) {
                                EstadoIncidente.PENDIENTE
                            },
                            usuarioId = data?.get("usuarioId") as? String ?: "",
                            prioridad = data?.get("prioridad") as? String,
                            etiqueta_ia = data?.get("etiqueta_ia") as? String
                        )
                        incident
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                trySend(incidents)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Obtener incidentes por estado
    fun getIncidentsByStatus(estado: EstadoIncidente): Flow<List<Incident>> = callbackFlow {
        val listener = incidentsCollection
            .whereEqualTo("estado", estado.displayName)
            .orderBy("fechaHora", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val incidents = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data
                        val incident = Incident(
                            id = doc.id,
                            tipo = data?.get("tipo") as? String ?: "",
                            descripcion = data?.get("descripcion") as? String ?: "",
                            fotoUrl = data?.get("fotoUrl") as? String,
                            videoUrl = data?.get("videoUrl") as? String,
                            ubicacion = try {
                                val ubicacionData = data?.get("ubicacion") as? Map<String, Any>
                                Ubicacion(
                                    lat = (ubicacionData?.get("lat") as? Number)?.toDouble() ?: 0.0,
                                    lon = (ubicacionData?.get("lon") as? Number)?.toDouble() ?: 0.0,
                                    direccion = ubicacionData?.get("direccion") as? String ?: ""
                                )
                            } catch (e: Exception) {
                                Ubicacion()
                            },
                            fechaHora = data?.get("fechaHora") as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now(),
                            estado = try {
                                val estadoString = data?.get("estado") as? String
                                if (estadoString != null) {
                                    EstadoIncidente.fromString(estadoString)
                                } else {
                                    EstadoIncidente.PENDIENTE
                                }
                            } catch (e: Exception) {
                                EstadoIncidente.PENDIENTE
                            },
                            usuarioId = data?.get("usuarioId") as? String ?: "",
                            prioridad = data?.get("prioridad") as? String,
                            etiqueta_ia = data?.get("etiqueta_ia") as? String
                        )
                        incident
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                trySend(incidents)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Crear nuevo incidente
    suspend fun createIncident(incident: Incident): Result<String> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }
            
            val incidentWithUser = incident.copy(usuarioId = currentUser.uid)
            val docRef = incidentsCollection.add(incidentWithUser).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Subir imagen a Firebase Storage
    suspend fun uploadImage(uri: Uri): Result<String> {
        return try {
            val fileName = "incidents/${UUID.randomUUID()}.jpg"
            val storageRef = storage.reference.child(fileName)
            
            val uploadTask = storageRef.putFile(uri).await()
            val downloadUrl = storageRef.downloadUrl.await()
            
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Analizar imagen con API de detección de amenazas
    suspend fun analyzeImageForThreats(uri: Uri, context: Context): Result<DetectionResponse> = withContext(Dispatchers.IO) {
        if (detectionApiService == null) {
            Log.w("IncidentRepository", "DetectionApiService no disponible")
            return@withContext Result.failure(Exception("Servicio de detección no configurado"))
        }
        
        // Solución temporal: API key hardcodeada
        // TODO: Implementar lectura desde .env en producción
        val apiKey = "INGRESA_TU_API_KEY_AQUI"
        val finalApiKey = EnvironmentConfig.DETECTION_API_KEY.ifEmpty { apiKey }
        
        var tempFile: File? = null
        try {
            // Convertir Uri a File temporal
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                return@withContext Result.failure(Exception("No se pudo abrir el archivo de imagen"))
            }
            
            // Crear archivo temporal
            tempFile = File.createTempFile("detection_", ".jpg", context.cacheDir)
            
            // Copiar contenido del inputStream al archivo temporal
            inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            // Verificar que el archivo se creó correctamente
            if (!tempFile.exists() || tempFile.length() == 0L) {
                return@withContext Result.failure(Exception("Error al procesar la imagen"))
            }
            
            // Detectar el tipo MIME real de la imagen
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            
            // Crear RequestBody y MultipartBody.Part
            val requestFile = tempFile.asRequestBody(mimeType.toMediaType())
            val imagePart = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
            
            // Llamar a la API
            val response = detectionApiService.detectarAmenazas(finalApiKey, imagePart)
            
            Log.d("IncidentRepository", "Análisis completado: ${response.cantidad_amenazas} amenaza(s) detectada(s)")
            
            Result.success(response)
            
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                400 -> "Solicitud inválida a la API de detección"
                401 -> "API key inválida o no autorizada"
                403 -> "Acceso denegado a la API de detección"
                429 -> "Límite de solicitudes excedido. Intenta más tarde"
                500 -> "Error del servidor de detección"
                else -> "Error del servidor: ${e.message()}"
            }
            Log.e("IncidentRepository", "Error HTTP ${e.code()}: $errorMessage")
            Result.failure(Exception(errorMessage))
        } catch (e: IOException) {
            Log.e("IncidentRepository", "Error de conexión al analizar imagen", e)
            Result.failure(Exception("Error de conexión. Verifica tu conexión a internet."))
        } catch (e: Exception) {
            Log.e("IncidentRepository", "Error al analizar imagen: ${e.message}", e)
            Result.failure(Exception("Error al analizar imagen: ${e.message}"))
        } finally {
            tempFile?.delete()
        }
    }
    
    // Actualizar estado de incidente (para administradores)
    suspend fun updateIncidentStatus(incidentId: String, newStatus: EstadoIncidente): Result<Unit> {
        return try {
            incidentsCollection.document(incidentId)
                .update("estado", newStatus.displayName)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Obtener incidente por ID
    suspend fun getIncidentById(incidentId: String): Result<Incident?> {
        return try {
            val document = incidentsCollection.document(incidentId).get().await()
            if (!document.exists()) {
                return Result.success(null)
            }
            
            val incident = parseDocumentToIncident(document)
            Result.success(incident)
        } catch (e: Exception) {
            Log.e("IncidentRepository", "Error obteniendo incidente por ID: $incidentId", e)
            Result.failure(e)
        }
    }
    
    // Eliminar incidente
    suspend fun deleteIncident(incidentId: String): Result<Unit> {
        return try {
            incidentsCollection.document(incidentId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Obtener incidentes pendientes (para administradores)
    fun getPendingIncidents(): Flow<List<Incident>> = callbackFlow {
        val listener = incidentsCollection
            .whereEqualTo("estado", EstadoIncidente.PENDIENTE.displayName)
            .orderBy("fechaHora", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val incidents = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Incident::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                trySend(incidents)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Obtener incidentes confirmados (para mostrar en mapa público)
    fun getConfirmedIncidents(): Flow<List<Incident>> = callbackFlow {
        val listener = incidentsCollection
            .whereEqualTo("estado", EstadoIncidente.CONFIRMADO.displayName)
            .orderBy("fechaHora", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val incidents = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Incident::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                trySend(incidents)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Obtener estadísticas de incidentes
    suspend fun getIncidentStats(): Result<Map<String, Int>> {
        return try {
            val allIncidents = incidentsCollection.get().await()
            val incidents = allIncidents.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Incident::class.java)
                } catch (e: Exception) {
                    null
                }
            }
            
            val stats = mapOf(
                "total" to incidents.size,
                "pendientes" to incidents.count { it.estado == EstadoIncidente.PENDIENTE },
                "en_revision" to incidents.count { it.estado == EstadoIncidente.EN_REVISION },
                "confirmados" to incidents.count { it.estado == EstadoIncidente.CONFIRMADO },
                "rechazados" to incidents.count { it.estado == EstadoIncidente.RECHAZADO },
                "resueltos" to incidents.count { it.estado == EstadoIncidente.RESUELTO }
            )
            
            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Obtener incidentes por usuario
    suspend fun getIncidentsByUser(userId: String): Result<List<Incident>> {
        return try {
            val snapshot = incidentsCollection
                .whereEqualTo("usuarioId", userId)
                .orderBy("fechaHora", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val incidents = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Incident::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    null
                }
            }
            
            Result.success(incidents)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Función auxiliar para parsear un documento de Firebase a Incident
    private fun parseDocumentToIncident(doc: com.google.firebase.firestore.DocumentSnapshot): Incident? {
        return try {
            val data = doc.data
            Incident(
                id = doc.id,
                tipo = data?.get("tipo") as? String ?: "",
                descripcion = data?.get("descripcion") as? String ?: "",
                fotoUrl = data?.get("fotoUrl") as? String,
                videoUrl = data?.get("videoUrl") as? String,
                ubicacion = try {
                    val ubicacionData = data?.get("ubicacion") as? Map<String, Any>
                    Ubicacion(
                        lat = (ubicacionData?.get("lat") as? Number)?.toDouble() ?: 0.0,
                        lon = (ubicacionData?.get("lon") as? Number)?.toDouble() ?: 0.0,
                        direccion = ubicacionData?.get("direccion") as? String ?: ""
                    )
                } catch (e: Exception) {
                    Ubicacion()
                },
                fechaHora = data?.get("fechaHora") as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now(),
                estado = try {
                    val estadoString = data?.get("estado") as? String
                    if (estadoString != null) {
                        EstadoIncidente.fromString(estadoString)
                    } else {
                        EstadoIncidente.PENDIENTE
                    }
                } catch (e: Exception) {
                    EstadoIncidente.PENDIENTE
                },
                usuarioId = data?.get("usuarioId") as? String ?: "",
                prioridad = data?.get("prioridad") as? String,
                etiqueta_ia = data?.get("etiqueta_ia") as? String
            )
        } catch (e: Exception) {
            Log.e("IncidentRepository", "Error parseando documento ${doc.id}", e)
            null
        }
    }
}
