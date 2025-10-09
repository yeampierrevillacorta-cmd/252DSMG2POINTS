package com.example.points.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Utilidades para Puntos de Interés
 */

@Composable
fun getCategoryIcon(iconName: String): ImageVector {
    return when (iconName) {
        "restaurant" -> Icons.Filled.Restaurant
        "movie" -> Icons.Filled.Movie
        "museum" -> Icons.Filled.Museum
        "fitness_center" -> Icons.Filled.FitnessCenter
        "local_hospital" -> Icons.Filled.LocalHospital
        "school" -> Icons.Filled.School
        "directions_bus" -> Icons.Filled.DirectionsBus
        "build" -> Icons.Filled.Build
        "place" -> Icons.Filled.Place
        "ev_station" -> Icons.Filled.EvStation
        "park" -> Icons.Filled.Park
        "shopping_cart" -> Icons.Filled.ShoppingCart
        else -> Icons.Filled.Place
    }
}
