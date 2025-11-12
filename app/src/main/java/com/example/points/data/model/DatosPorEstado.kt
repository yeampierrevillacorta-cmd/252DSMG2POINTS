package com.example.points.data.model

data class DatosPorEstado(
    val tipo: String, // "Incidentes", "Eventos", "POIs"
    val atendido: Int,
    val denegado: Int,
    val enRevision: Int
)

