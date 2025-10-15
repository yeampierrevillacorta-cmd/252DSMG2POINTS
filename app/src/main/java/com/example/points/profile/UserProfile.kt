package com.example.points.profile

import com.example.points.models.TipoUsuario

data class UserProfile(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val tipo: TipoUsuario = TipoUsuario.CIUDADANO,
    val notificaciones: Boolean = true,
    val photoUrl: String? = null
)
