package com.example.points.utils

import android.util.Log
import com.example.points.models.Incident
import com.example.points.models.EstadoIncidente
import com.example.points.models.TipoIncidente
import com.example.points.models.Ubicacion
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

object TestDataCreator {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    fun createTestIncidents() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e("TestDataCreator", "Usuario no autenticado")
            return
        }
        
        val testIncidents = listOf(
            Incident(
                tipo = TipoIncidente.INSEGURIDAD.displayName,
                descripcion = "Robo a mano armada en la esquina del parque",
                ubicacion = Ubicacion(
                    lat = -12.0464,
                    lon = -77.0428,
                    direccion = "Plaza de Armas, Lima"
                ),
                fechaHora = Timestamp.now(),
                estado = EstadoIncidente.PENDIENTE,
                usuarioId = currentUser.uid
            ),
            Incident(
                tipo = TipoIncidente.ACCIDENTE_TRANSITO.displayName,
                descripcion = "Choque entre dos vehículos en avenida principal",
                ubicacion = Ubicacion(
                    lat = -12.0520,
                    lon = -77.0450,
                    direccion = "Av. Abancay, Lima"
                ),
                fechaHora = Timestamp(Date(System.currentTimeMillis() - 3600000)), // 1 hora atrás
                estado = EstadoIncidente.CONFIRMADO,
                usuarioId = currentUser.uid
            ),
            Incident(
                tipo = TipoIncidente.INCENDIO.displayName,
                descripcion = "Incendio en edificio comercial",
                ubicacion = Ubicacion(
                    lat = -12.0400,
                    lon = -77.0380,
                    direccion = "Jr. de la Unión, Lima"
                ),
                fechaHora = Timestamp(Date(System.currentTimeMillis() - 7200000)), // 2 horas atrás
                estado = EstadoIncidente.EN_REVISION,
                usuarioId = currentUser.uid
            ),
            Incident(
                tipo = TipoIncidente.SERVICIO_PUBLICO.displayName,
                descripcion = "Falta de agua potable en el sector",
                ubicacion = Ubicacion(
                    lat = -12.0600,
                    lon = -77.0500,
                    direccion = "San Juan de Lurigancho, Lima"
                ),
                fechaHora = Timestamp(Date(System.currentTimeMillis() - 10800000)), // 3 horas atrás
                estado = EstadoIncidente.PENDIENTE,
                usuarioId = currentUser.uid
            ),
            Incident(
                tipo = TipoIncidente.VANDALISMO.displayName,
                descripcion = "Grafitis y daños en propiedad pública",
                ubicacion = Ubicacion(
                    lat = -12.0300,
                    lon = -77.0350,
                    direccion = "Miraflores, Lima"
                ),
                fechaHora = Timestamp(Date(System.currentTimeMillis() - 14400000)), // 4 horas atrás
                estado = EstadoIncidente.RESUELTO,
                usuarioId = currentUser.uid
            )
        )
        
        Log.d("TestDataCreator", "Creando ${testIncidents.size} incidentes de prueba...")
        
        testIncidents.forEach { incident ->
            firestore.collection("incidentes")
                .add(incident)
                .addOnSuccessListener { documentReference ->
                    Log.d("TestDataCreator", "Incidente creado con ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.e("TestDataCreator", "Error creando incidente", e)
                }
        }
    }
    
    fun clearTestData() {
        Log.d("TestDataCreator", "Eliminando datos de prueba...")
        
        firestore.collection("incidentes")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete()
                        .addOnSuccessListener {
                            Log.d("TestDataCreator", "Documento ${document.id} eliminado")
                        }
                        .addOnFailureListener { e ->
                            Log.e("TestDataCreator", "Error eliminando documento ${document.id}", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("TestDataCreator", "Error obteniendo documentos para eliminar", e)
            }
    }
}
