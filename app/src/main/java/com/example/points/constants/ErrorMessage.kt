package com.example.points.constants

/**
 * Mensajes de error comunes
 */
enum class ErrorMessage(val value: String) {
    ERROR_DESCONOCIDO("Error desconocido"),
    ERROR_CARGAR_EVENTO("Error al cargar el evento"),
    ERROR_CARGAR_INCIDENTE("Error al cargar el incidente"),
    ERROR_CARGAR_POI("Error al cargar el punto de interés"),
    ERROR_GUARDAR("Error al guardar los datos"),
    ERROR_CARGAR_DATOS("Error al cargar los datos"),
    NO_HAY_EVENTOS("No hay eventos disponibles"),
    NO_HAY_INCIDENTES("No hay incidentes disponibles"),
    NO_HAY_POI("No hay puntos de interés disponibles"),
    INTENTA_CAMBIAR_FILTROS("Intenta cambiar los filtros o crear uno nuevo")
}

