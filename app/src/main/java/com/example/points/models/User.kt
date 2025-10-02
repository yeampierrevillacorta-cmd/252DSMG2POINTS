package com.example.points.models

data class User(
    val id: String = "",
    val nombre: String = "",
    val email: String = "",
    val tipo: TipoUsuario = TipoUsuario.CIUDADANO,
    val notificaciones: Boolean = true,
    val photoUrl: String? = null,
    val telefono: String = ""
)

enum class TipoUsuario(val displayName: String) {
    CIUDADANO("Ciudadano"),
    ADMINISTRADOR("Administrador"),
    MODERADOR("Moderador")
}
