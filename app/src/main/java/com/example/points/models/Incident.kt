package com.example.points.models

import com.google.firebase.Timestamp
import java.util.Date

data class Incident(
    val id: String = "",
    val tipo: String = "",
    val descripcion: String = "",
    val fotoUrl: String? = null,
    val videoUrl: String? = null,
    val ubicacion: Ubicacion = Ubicacion(),
    val fechaHora: Timestamp = Timestamp.now(),
    val estado: EstadoIncidente = EstadoIncidente.PENDIENTE,
    val usuarioId: String = ""
)

data class Ubicacion(
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val direccion: String = ""
)

enum class EstadoIncidente(val displayName: String) {
    PENDIENTE("Pendiente"),
    EN_REVISION("En Revisión"),
    CONFIRMADO("Confirmado"),
    RECHAZADO("Rechazado"),
    RESUELTO("Resuelto");
    
    companion object {
        fun fromString(value: String): EstadoIncidente {
            return when (value.lowercase()) {
                "pendiente" -> PENDIENTE
                "en revisión", "en_revision" -> EN_REVISION
                "confirmado" -> CONFIRMADO
                "rechazado" -> RECHAZADO
                "resuelto" -> RESUELTO
                else -> PENDIENTE // Default fallback
            }
        }
    }
}

enum class TipoIncidente(val displayName: String) {
    INSEGURIDAD("Inseguridad"),
    ACCIDENTE_TRANSITO("Accidente de Tránsito"),
    INCENDIO("Incendio"),
    INUNDACION("Inundación"),
    VANDALISMO("Vandalismo"),
    SERVICIO_PUBLICO("Servicio Público"),
    OTRO("Otro")
}

// Extensión para convertir Date a Timestamp
fun Date.toTimestamp(): Timestamp = Timestamp(this)

// Extensión para convertir Timestamp a Date
fun Timestamp.toDate(): Date = this.toDate()