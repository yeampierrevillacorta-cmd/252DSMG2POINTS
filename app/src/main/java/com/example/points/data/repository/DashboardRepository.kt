package com.example.points.data.repository

import android.util.Log
import com.example.points.models.Incident
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class DashboardRepository(private val firestore: FirebaseFirestore) {
    companion object {
        private const val TAG = "DashboardRepository"
        private const val INCIDENTS_COLLECTION = "incidentes"
    }
    
    suspend fun getAllIncidents(): Result<List<Incident>> {
        return try {
            val snapshot = firestore.collection(INCIDENTS_COLLECTION).get().await()
            val incidents = snapshot.documents.mapNotNull { doc ->
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
                            com.example.points.models.Ubicacion(
                                lat = (ubicacionData?.get("lat") as? Number)?.toDouble() ?: 0.0,
                                lon = (ubicacionData?.get("lon") as? Number)?.toDouble() ?: 0.0,
                                direccion = ubicacionData?.get("direccion") as? String ?: ""
                            )
                        } catch (e: Exception) {
                            com.example.points.models.Ubicacion()
                        },
                        fechaHora = data?.get("fechaHora") as? com.google.firebase.Timestamp 
                            ?: com.google.firebase.Timestamp.now(),
                        estado = try {
                            val estadoString = data?.get("estado") as? String
                            if (estadoString != null) {
                                com.example.points.models.EstadoIncidente.fromString(estadoString)
                            } else {
                                com.example.points.models.EstadoIncidente.PENDIENTE
                            }
                        } catch (e: Exception) {
                            com.example.points.models.EstadoIncidente.PENDIENTE
                        },
                        usuarioId = data?.get("usuarioId") as? String ?: ""
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parseando documento ${doc.id}", e)
                    null
                }
            }
            Result.success(incidents)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener la lista de incidentes", e)
            Result.failure(e)
        }
    }
}

