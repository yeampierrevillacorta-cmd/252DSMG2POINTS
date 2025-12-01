package com.example.points.models.detection

import kotlinx.serialization.Serializable

/**
 * Respuesta de la API de detección de amenazas
 */
@Serializable
data class DetectionResponse(
    val cantidad_amenazas: Int,
    val cantidad_objetos: Int,
    val detalles: List<DetalleDeteccion>,
    val estado: String,
    val imagen_procesada: String? = null
)

@Serializable
data class DetalleDeteccion(
    val box: BoundingBox,
    val confianza: Double,
    val es_amenaza: Boolean,
    val objeto: String
)

@Serializable
data class BoundingBox(
    val x1: Int,
    val x2: Int,
    val y1: Int,
    val y2: Int
)

