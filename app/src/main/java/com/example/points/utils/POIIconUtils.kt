package com.example.points.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.points.models.CategoriaPOI

object POIIconUtils {
    
    fun getPOIIcon(categoria: CategoriaPOI): ImageVector {
        return when (categoria) {
            CategoriaPOI.COMIDA -> Icons.Default.Restaurant
            CategoriaPOI.ENTRETENIMIENTO -> Icons.Default.Movie
            CategoriaPOI.CULTURA -> Icons.Default.Museum
            CategoriaPOI.DEPORTE -> Icons.Default.FitnessCenter
            CategoriaPOI.SALUD -> Icons.Default.LocalHospital
            CategoriaPOI.EDUCACION -> Icons.Default.School
            CategoriaPOI.TRANSPORTE -> Icons.Default.DirectionsBus
            CategoriaPOI.SERVICIOS -> Icons.Default.Build
            CategoriaPOI.TURISMO -> Icons.Default.Place
            CategoriaPOI.RECARGA_ELECTRICA -> Icons.Default.EvStation
            CategoriaPOI.PARQUES -> Icons.Default.Park
            CategoriaPOI.SHOPPING -> Icons.Default.ShoppingCart
            CategoriaPOI.OTRO -> Icons.Default.Place
        }
    }
    
    fun getPOIColor(categoria: CategoriaPOI): androidx.compose.ui.graphics.Color {
        return when (categoria) {
            CategoriaPOI.COMIDA -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Verde
            CategoriaPOI.ENTRETENIMIENTO -> androidx.compose.ui.graphics.Color(0xFF9C27B0) // Púrpura
            CategoriaPOI.CULTURA -> androidx.compose.ui.graphics.Color(0xFF607D8B) // Azul gris
            CategoriaPOI.DEPORTE -> androidx.compose.ui.graphics.Color(0xFFFF5722) // Rojo profundo
            CategoriaPOI.SALUD -> androidx.compose.ui.graphics.Color(0xFFF44336) // Rojo
            CategoriaPOI.EDUCACION -> androidx.compose.ui.graphics.Color(0xFF3F51B5) // Índigo
            CategoriaPOI.TRANSPORTE -> androidx.compose.ui.graphics.Color(0xFF607D8B) // Azul gris
            CategoriaPOI.SERVICIOS -> androidx.compose.ui.graphics.Color(0xFF795548) // Marrón
            CategoriaPOI.TURISMO -> androidx.compose.ui.graphics.Color(0xFF00BCD4) // Cian
            CategoriaPOI.RECARGA_ELECTRICA -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Verde
            CategoriaPOI.PARQUES -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Verde
            CategoriaPOI.SHOPPING -> androidx.compose.ui.graphics.Color(0xFFE91E63) // Rosa
            CategoriaPOI.OTRO -> androidx.compose.ui.graphics.Color(0xFF9E9E9E) // Gris
        }
    }
}
