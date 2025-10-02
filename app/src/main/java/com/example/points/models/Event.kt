package com.example.points.models

import com.google.firebase.Timestamp

data class Event(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val ubicacion: String = "",
    val coordenadas: Ubicacion? = null,
    val fechaHora: Timestamp = Timestamp.now(),
    val tipo: TipoEvento = TipoEvento.CULTURAL,
    val organizador: String = "",
    val participantes: List<String> = emptyList(),
    val imagenUrl: String? = null
)

enum class TipoEvento(val displayName: String) {
    CULTURAL("Cultural"),
    DEPORTIVO("Deportivo"),
    EDUCATIVO("Educativo"),
    COMUNITARIO("Comunitario"),
    GUBERNAMENTAL("Gubernamental"),
    COMERCIAL("Comercial"),
    OTRO("Otro")
}
