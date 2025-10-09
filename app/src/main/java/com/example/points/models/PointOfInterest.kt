package com.example.points.models

import com.google.firebase.Timestamp

data class PointOfInterest(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val categoria: CategoriaPOI = CategoriaPOI.COMIDA,
    val ubicacion: Ubicacion = Ubicacion(),
    val direccion: String = "",
    val telefono: String? = null,
    val email: String? = null,
    val sitioWeb: String? = null,
    val horarios: List<Horario> = emptyList(),
    val imagenes: List<String> = emptyList(),
    val calificacion: Double = 0.0,
    val totalCalificaciones: Int = 0,
    val fechaCreacion: Timestamp = Timestamp.now(),
    val fechaActualizacion: Timestamp = Timestamp.now(),
    val estado: EstadoPOI = EstadoPOI.PENDIENTE,
    val usuarioId: String = "",
    val moderadorId: String? = null,
    val fechaModeracion: Timestamp? = null,
    val comentariosModeracion: String? = null,
    val caracteristicas: List<CaracteristicaPOI> = emptyList(),
    val precio: RangoPrecio? = null,
    val accesibilidad: Boolean = false,
    val estacionamiento: Boolean = false,
    val wifi: Boolean = false
)

data class Horario(
    val dia: DiaSemana,
    val horaApertura: String,
    val horaCierre: String,
    val cerrado: Boolean = false
)

enum class DiaSemana(val displayName: String) {
    LUNES("Lunes"),
    MARTES("Martes"),
    MIERCOLES("Miércoles"),
    JUEVES("Jueves"),
    VIERNES("Viernes"),
    SABADO("Sábado"),
    DOMINGO("Domingo")
}

enum class CategoriaPOI(val displayName: String, val icon: String) {
    COMIDA("Comida", "restaurant"),
    ENTRETENIMIENTO("Entretenimiento", "movie"),
    CULTURA("Cultura", "museum"),
    DEPORTE("Deporte", "fitness_center"),
    SALUD("Salud", "local_hospital"),
    EDUCACION("Educación", "school"),
    TRANSPORTE("Transporte", "directions_bus"),
    SERVICIOS("Servicios", "build"),
    TURISMO("Turismo", "place"),
    RECARGA_ELECTRICA("Recarga Eléctrica", "ev_station"),
    PARQUES("Parques", "park"),
    SHOPPING("Shopping", "shopping_cart"),
    OTRO("Otro", "place")
}

enum class EstadoPOI(val displayName: String) {
    PENDIENTE("Pendiente"),
    EN_REVISION("En Revisión"),
    APROBADO("Aprobado"),
    RECHAZADO("Rechazado"),
    SUSPENDIDO("Suspendido")
}

enum class CaracteristicaPOI(val displayName: String) {
    ACCESIBLE_SILLA_RUEDAS("Accesible en silla de ruedas"),
    ESTACIONAMIENTO_GRATUITO("Estacionamiento gratuito"),
    WIFI_GRATUITO("WiFi gratuito"),
    MASCOTAS_BIENVENIDAS("Mascotas bienvenidas"),
    TERRAZA("Terraza"),
    AIRE_ACONDICIONADO("Aire acondicionado"),
    MUSICA_EN_VIVO("Música en vivo"),
    DELIVERY("Delivery"),
    RESERVAS("Acepta reservas"),
    TARJETAS_CREDITO("Acepta tarjetas de crédito"),
    EFECTIVO_SOLO("Solo efectivo"),
    VEGETARIANO("Opciones vegetarianas"),
    VEGANO("Opciones veganas"),
    SIN_GLUTEN("Opciones sin gluten"),
    HORARIO_24H("Atención 24 horas"),
    ESTACIONAMIENTO_PAGO("Estacionamiento de pago"),
    VALET_PARKING("Valet parking"),
    BANOS_PUBLICOS("Baños públicos"),
    ATM("Cajero automático"),
    FUMADORES("Zona de fumadores")
}

enum class RangoPrecio(val displayName: String, val simbolo: String) {
    GRATIS("Gratis", "Gratis"),
    ECONOMICO("Económico", "$"),
    MODERADO("Moderado", "$$"),
    CARO("Caro", "$$$"),
    MUY_CARO("Muy caro", "$$$$")
}

// Extensión para obtener el icono de la categoría
fun CategoriaPOI.getIconResource(): String = this.icon

// Extensión para verificar si un POI está abierto en un momento específico
fun PointOfInterest.estaAbierto(dia: DiaSemana, hora: String): Boolean {
    val horarioDelDia = horarios.find { it.dia == dia }
    return horarioDelDia?.let { horario ->
        if (horario.cerrado) return false
        
        val horaActual = hora.split(":").let { "${it[0]}:${it[1]}" }
        val horaApertura = horario.horaApertura
        val horaCierre = horario.horaCierre
        
        // Si cierra después de medianoche (ej: 23:00 - 02:00)
        if (horaCierre < horaApertura) {
            horaActual >= horaApertura || horaActual <= horaCierre
        } else {
            horaActual >= horaApertura && horaActual <= horaCierre
        }
    } ?: false
}

// Extensión para obtener el próximo horario de apertura
fun PointOfInterest.getProximoHorarioApertura(): Horario? {
    val hoy = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)
    val diasSemana = listOf(
        DiaSemana.DOMINGO, DiaSemana.LUNES, DiaSemana.MARTES, 
        DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES, DiaSemana.SABADO
    )
    
    val diaActual = diasSemana[hoy - 1]
    val horariosOrdenados = horarios.sortedBy { it.dia.ordinal }
    
    // Buscar el próximo horario disponible
    for (i in 0 until 7) {
        val diaIndex = (diaActual.ordinal + i) % 7
        val horario = horarios.find { it.dia.ordinal == diaIndex && !it.cerrado }
        if (horario != null) return horario
    }
    
    return null
}
