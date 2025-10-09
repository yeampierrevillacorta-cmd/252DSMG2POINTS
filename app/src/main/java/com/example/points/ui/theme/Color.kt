package com.example.points.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta de marca POINTS - Modo Claro
val PointsPrimary = Color(0xFF0FB4A5)      // Teal principal
val PointsSecondary = Color(0xFF2EAE6A)    // Verde secundario
val PointsTertiary = Color(0xFF2563EB)     // Azul terciario
val PointsAccent = Color(0xFFFF7A00)       // Naranja de acento/alerta
val PointsError = Color(0xFFD92D20)        // Rojo de error
val PointsSuccess = Color(0xFF16A34A)      // Verde de éxito

// Colores neutros - Modo Claro
val PointsInk = Color(0xFF0F172A)          // Negro principal
val PointsSubtle = Color(0xFF64748B)       // Gris sutil
val PointsSurface = Color(0xFFF8FAFC)      // Superficie clara
val PointsOutline = Color(0xFFE2E8F0)      // Borde sutil

// Paleta de marca POINTS - Modo Oscuro
val PointsPrimaryDark = Color(0xFF15C3B1)  // Teal principal oscuro
val PointsSecondaryDark = Color(0xFF35C07A) // Verde secundario oscuro
val PointsTertiaryDark = Color(0xFF4F83FF)  // Azul terciario oscuro
val PointsAccentDark = Color(0xFFFF8A1A)    // Naranja de acento oscuro

// Colores neutros - Modo Oscuro
val PointsSurfaceDark = Color(0xFF0B1220)  // Superficie oscura
val PointsOnSurfaceDark = Color(0xFFE2E8F0) // Texto sobre superficie oscura
val PointsSurfaceVariantDark = Color(0xFF111827) // Variante de superficie oscura
val PointsOutlineDark = Color(0xFF334155)  // Borde oscuro

// Colores de estado para incidentes
val StatusPending = Color(0xFFFF7A00)      // Naranja - Pendiente
val StatusInReview = Color(0xFF2563EB)     // Azul - En revisión
val StatusConfirmed = Color(0xFF16A34A)    // Verde - Confirmado
val StatusRejected = Color(0xFFD92D20)     // Rojo - Rechazado
val StatusResolved = Color(0xFF0FB4A5)     // Teal - Resuelto

// Colores de prioridad
val PriorityLow = Color(0xFF16A34A)        // Verde - Baja
val PriorityMedium = Color(0xFFFF7A00)     // Naranja - Media
val PriorityHigh = Color(0xFFD92D20)       // Rojo - Alta
val PriorityCritical = Color(0xFF7C2D12)   // Rojo oscuro - Crítica

// Colores de categoría
val CategoryInfrastructure = Color(0xFF2563EB)  // Azul - Infraestructura
val CategoryEnvironment = Color(0xFF16A34A)     // Verde - Medio ambiente
val CategorySafety = Color(0xFFD92D20)          // Rojo - Seguridad
val CategoryTransport = Color(0xFF7C3AED)       // Púrpura - Transporte
val CategoryOther = Color(0xFF64748B)           // Gris - Otros

// Colores de mapa
val MapMarkerDefault = Color(0xFF0FB4A5)   // Teal - Marcador por defecto
val MapMarkerAlert = Color(0xFFFF7A00)     // Naranja - Marcador de alerta
val MapCluster = Color(0xFF2563EB)         // Azul - Cluster
val MapUserLocation = Color(0xFF16A34A)    // Verde - Ubicación del usuario

// Colores de feedback
val FeedbackInfo = Color(0xFF2563EB)       // Azul - Información
val FeedbackWarning = Color(0xFFFF7A00)    // Naranja - Advertencia
val FeedbackError = Color(0xFFD92D20)      // Rojo - Error
val FeedbackSuccess = Color(0xFF16A34A)    // Verde - Éxito

// Colores de carga
val ShimmerBase = Color(0xFFE2E8F0)        // Base del shimmer
val ShimmerHighlight = Color(0xFFF1F5F9)   // Resaltado del shimmer

// Colores de transparencia
val OverlayLight = Color(0x80000000)       // Overlay claro
val OverlayDark = Color(0x80000000)        // Overlay oscuro
val Scrim = Color(0x66000000)              // Scrim para modales

// Colores legacy (mantener para compatibilidad)
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)