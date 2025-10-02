package com.example.points.models

import com.google.firebase.Timestamp

data class Notification(
    val id: String = "",
    val tipo: TipoNotificacion = TipoNotificacion.INCIDENTE,
    val mensaje: String = "",
    val fechaHora: Timestamp = Timestamp.now(),
    val usuarioId: String = "",
    val leida: Boolean = false,
    val incidenteId: String? = null,
    val eventoId: String? = null
)

enum class TipoNotificacion(val displayName: String) {
    INCIDENTE("Incidente"),
    EVENTO("Evento"),
    SISTEMA("Sistema"),
    CONFIRMACION("Confirmación")
}
