package com.example.points.constants

/**
 * Mensajes de éxito comunes
 */
enum class SuccessMessage(val value: String) {
    OPERACION_EXITOSA("Operación completada exitosamente"),
    EVENTO_CREADO("Evento creado exitosamente"),
    INCIDENTE_CREADO("Incidente creado exitosamente"),
    POI_ENVIADO("¡Punto de interés enviado exitosamente! Será revisado por nuestros moderadores antes de ser publicado."),
    USUARIO_REGISTRADO("Registro exitoso ✅"),
    LOGIN_EXITOSO("Login exitoso ✅"),
    PASSWORD_RESET_ENVIADO("Correo de recuperación enviado 📧")
}

