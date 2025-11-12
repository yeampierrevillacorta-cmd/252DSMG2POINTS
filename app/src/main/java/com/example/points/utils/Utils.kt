package com.example.points.utils

import androidx.compose.ui.graphics.Color

class Utils {
    fun colorAleatorio(): Color {
        var colors = mutableListOf(
            Color(0xFFF44336),
            Color(0xFF4CAF50),
            Color(0xFFFFEB3B),
            Color(0xFF673AB7),
            Color(0xFF9C27B0),
            Color(0xFF03A9F4),
            Color(0xFFCDDC39),
            Color(0xFFE91E63),
            Color(0xFF00BCD4),
            Color(0xFFFF9800),
            Color(0xFF009688),
        )
        val randonNumber = (Math.random() * colors.size).toInt()
        // seleccionar un color aleatorio
        val color = colors[randonNumber]
        // remover de la lista colores para no repetir
        colors.removeAt(randonNumber)
        return color
    }
}

