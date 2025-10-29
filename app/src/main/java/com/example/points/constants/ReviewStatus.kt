package com.example.points.constants

/**
 * Enum genérico para estados de revisión/aprobación utilizados en toda la aplicación
 * Puede aplicarse a Eventos, Incidentes, POI, etc.
 */
enum class ReviewStatus(val displayName: String, val emoji: String) {
    PENDIENTE("Pendiente", "⏳"),
    EN_REVISION("En Revisión", "🔍"),
    APROBADO("Aprobado", "✅"),
    CONFIRMADO("Confirmado", "✅"),
    RECHAZADO("Rechazado", "❌"),
    CANCELADO("Cancelado", "❌"),
    FINALIZADO("Finalizado", "🏁"),
    RESUELTO("Resuelto", "✅"),
    SUSPENDIDO("Suspendido", "⏸️");
    
    /**
     * Obtiene el texto formateado con emoji
     */
    fun getFormattedText(): String = "$emoji $displayName"
    
    /**
     * Verifica si el estado indica que está en proceso de revisión
     */
    fun isInReview(): Boolean {
        return this == PENDIENTE || this == EN_REVISION
    }
    
    /**
     * Verifica si el estado indica que fue aprobado/confirmado
     */
    fun isApproved(): Boolean {
        return this == APROBADO || this == CONFIRMADO || this == FINALIZADO || this == RESUELTO
    }
    
    /**
     * Verifica si el estado indica que fue rechazado o cancelado
     */
    fun isRejected(): Boolean {
        return this == RECHAZADO || this == CANCELADO || this == SUSPENDIDO
    }
}

