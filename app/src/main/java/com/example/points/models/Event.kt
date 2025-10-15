package com.example.points.models

import com.google.firebase.Timestamp

data class Event(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val categoria: CategoriaEvento = CategoriaEvento.CULTURAL,
    val ubicacion: Ubicacion = Ubicacion(),
    val direccion: String = "",
    val fechaInicio: Timestamp = Timestamp.now(),
    val fechaFin: Timestamp = Timestamp.now(),
    val horaInicio: String = "",
    val horaFin: String = "",
    val esRecurrente: Boolean = false,
    val frecuenciaRecurrencia: FrecuenciaRecurrencia? = null,
    val fechaFinRecurrencia: Timestamp? = null,
    val imagenes: List<String> = emptyList(),
    val organizador: String = "",
    val contacto: ContactoEvento = ContactoEvento(),
    val precio: PrecioEvento = PrecioEvento(),
    val capacidad: Int? = null,
    val inscripciones: Int = 0,
    val estado: EstadoEvento = EstadoEvento.PENDIENTE,
    val fechaCreacion: Timestamp = Timestamp.now(),
    val fechaActualizacion: Timestamp = Timestamp.now(),
    val usuarioId: String = "",
    val moderadorId: String? = null,
    val fechaModeracion: Timestamp? = null,
    val comentariosModeracion: String? = null,
    val caracteristicas: List<CaracteristicaEvento> = emptyList(),
    val etiquetas: List<String> = emptyList(),
    val sitioWeb: String? = null,
    val redesSociales: RedesSociales = RedesSociales(),
    val esGratuito: Boolean = true,
    val requiereInscripcion: Boolean = false,
    val edadMinima: Int? = null,
    val edadMaxima: Int? = null,
    val accesibilidad: Boolean = false,
    val estacionamiento: Boolean = false,
    val transportePublico: Boolean = false,
    val cancelado: Boolean = false,
    val motivoCancelacion: String? = null
)

data class ContactoEvento(
    val telefono: String? = null,
    val email: String? = null,
    val nombreContacto: String? = null
)

data class PrecioEvento(
    val esGratuito: Boolean = true,
    val precioGeneral: Double? = null,
    val precioEstudiantes: Double? = null,
    val precioAdultosMayores: Double? = null,
    val descuentos: List<DescuentoEvento> = emptyList()
)

data class DescuentoEvento(
    val nombre: String = "",
    val porcentaje: Double = 0.0,
    val condiciones: String = ""
)

data class RedesSociales(
    val facebook: String? = null,
    val instagram: String? = null,
    val twitter: String? = null,
    val youtube: String? = null,
    val tiktok: String? = null
)

enum class CategoriaEvento(val displayName: String, val icon: String) {
    CULTURAL("Cultural", "theater_comedy"),
    DEPORTIVO("Deportivo", "sports_soccer"),
    MUSICAL("Musical", "music_note"),
    EDUCATIVO("Educativo", "school"),
    GASTRONOMICO("Gastronómico", "restaurant"),
    TECNOLOGICO("Tecnológico", "computer"),
    ARTISTICO("Artístico", "palette"),
    COMERCIAL("Comercial", "store"),
    RELIGIOSO("Religioso", "church"),
    COMUNITARIO("Comunitario", "groups"),
    FESTIVAL("Festival", "celebration"),
    CONFERENCIA("Conferencia", "mic"),
    TALLER("Taller", "build"),
    EXPOSICION("Exposición", "museum"),
    FERIA("Feria", "storefront"),
    OTRO("Otro", "event")
}

enum class EstadoEvento(val displayName: String) {
    PENDIENTE("Pendiente"),
    EN_REVISION("En Revisión"),
    APROBADO("Aprobado"),
    RECHAZADO("Rechazado"),
    CANCELADO("Cancelado"),
    FINALIZADO("Finalizado")
}

enum class FrecuenciaRecurrencia(val displayName: String) {
    DIARIO("Diario"),
    SEMANAL("Semanal"),
    MENSUAL("Mensual"),
    ANUAL("Anual")
}

enum class CaracteristicaEvento(val displayName: String) {
    ACCESIBLE_SILLA_RUEDAS("Accesible en silla de ruedas"),
    ESTACIONAMIENTO_GRATUITO("Estacionamiento gratuito"),
    TRANSPORTE_PUBLICO("Acceso por transporte público"),
    MASCOTAS_BIENVENIDAS("Mascotas bienvenidas"),
    AIRE_ACONDICIONADO("Aire acondicionado"),
    WIFI_GRATUITO("WiFi gratuito"),
    COMIDA_INCLUIDA("Comida incluida"),
    BEBIDAS_INCLUIDAS("Bebidas incluidas"),
    MATERIAL_INCLUIDO("Material incluido"),
    CERTIFICADO("Entrega certificado"),
    GRUPO_LIMITADO("Grupo limitado"),
    NIVEL_PRINCIPIANTE("Nivel principiante"),
    NIVEL_INTERMEDIO("Nivel intermedio"),
    NIVEL_AVANZADO("Nivel avanzado"),
    FAMILIAR("Apto para toda la familia"),
    ADULTOS_SOLO("Solo adultos"),
    MENORES_ACOMPANADOS("Menores acompañados"),
    FOTOGRAFIA_PERMITIDA("Fotografía permitida"),
    GRABACION_PERMITIDA("Grabación permitida"),
    VENTA_ALIMENTOS("Venta de alimentos"),
    VENTA_BEBIDAS("Venta de bebidas"),
    VENTA_MERCHANDISING("Venta de merchandising")
}

// Extensión para obtener el icono de la categoría
fun CategoriaEvento.getIconResource(): String = this.icon

// Extensión para verificar si un evento está activo
fun Event.estaActivo(): Boolean {
    val ahora = Timestamp.now()
    return !cancelado && 
           estado == EstadoEvento.APROBADO && 
           fechaInicio <= ahora && 
           fechaFin >= ahora
}

// Extensión para verificar si un evento es próximo
fun Event.esProximo(): Boolean {
    val ahora = Timestamp.now()
    val unDiaEnMillis = 24 * 60 * 60 * 1000L
    val unDiaDespues = Timestamp(ahora.seconds + unDiaEnMillis / 1000, 0)
    
    return !cancelado && 
           estado == EstadoEvento.APROBADO && 
           fechaInicio > ahora && 
           fechaInicio <= unDiaDespues
}

// Extensión para obtener el estado del evento con emoji
fun Event.getEstadoConEmoji(): String {
    return when (estado) {
        EstadoEvento.PENDIENTE -> "⏳ ${estado.displayName}"
        EstadoEvento.EN_REVISION -> "🔍 ${estado.displayName}"
        EstadoEvento.APROBADO -> if (cancelado) "❌ Cancelado" else "✅ ${estado.displayName}"
        EstadoEvento.RECHAZADO -> "❌ ${estado.displayName}"
        EstadoEvento.CANCELADO -> "❌ ${estado.displayName}"
        EstadoEvento.FINALIZADO -> "🏁 ${estado.displayName}"
    }
}

// Extensión para obtener la duración del evento
fun Event.getDuracion(): String {
    val inicio = fechaInicio.toDate()
    val fin = fechaFin.toDate()
    val duracionMs = fin.time - inicio.time
    val duracionHoras = duracionMs / (1000 * 60 * 60)
    val duracionMinutos = (duracionMs % (1000 * 60 * 60)) / (1000 * 60)
    
    return when {
        duracionHoras > 0 -> "${duracionHoras}h ${duracionMinutos}min"
        duracionMinutos > 0 -> "${duracionMinutos}min"
        else -> "Duración no especificada"
    }
}

// Extensión para verificar si el evento tiene cupos disponibles
fun Event.tieneCuposDisponibles(): Boolean {
    return capacidad?.let { cap -> inscripciones < cap } ?: true
}

// Extensión para obtener el porcentaje de ocupación
fun Event.getPorcentajeOcupacion(): Double {
    return capacidad?.let { cap ->
        if (cap > 0) (inscripciones.toDouble() / cap.toDouble()) * 100.0 else 0.0
    } ?: 0.0
}

// Extensión para formatear el precio
fun Event.getPrecioFormateado(): String {
    return when {
        esGratuito -> "Gratuito"
        precio.precioGeneral != null -> "$${String.format("%.0f", precio.precioGeneral)}"
        else -> "Precio no especificado"
    }
}

// Extensión para obtener la edad recomendada
fun Event.getEdadRecomendada(): String {
    return when {
        edadMinima != null && edadMaxima != null -> "${edadMinima}-${edadMaxima} años"
        edadMinima != null -> "Desde ${edadMinima} años"
        edadMaxima != null -> "Hasta ${edadMaxima} años"
        else -> "Todas las edades"
    }
}