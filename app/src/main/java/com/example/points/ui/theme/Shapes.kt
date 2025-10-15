package com.example.points.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Sistema de formas para la aplicación POINTS
 * Inspirado en Airbnb - Bordes redondeados suaves y modernos
 * Utiliza el sistema de tokens de radio optimizado para estética limpia
 */

val PointsShapes = Shapes(
    // Formas extra pequeñas - Para chips y badges (Estilo Airbnb)
    extraSmall = RoundedCornerShape(6.dp),
    
    // Formas pequeñas - Para botones pequeños y elementos compactos
    small = RoundedCornerShape(12.dp),
    
    // Formas medianas - Para botones estándar y tarjetas pequeñas
    medium = RoundedCornerShape(16.dp),
    
    // Formas grandes - Para tarjetas principales y contenedores
    large = RoundedCornerShape(20.dp),
    
    // Formas extra grandes - Para modales y sheets
    extraLarge = RoundedCornerShape(24.dp),
)

/**
 * Formas personalizadas para casos específicos de POINTS (Estilo Airbnb)
 */
object PointsCustomShapes {
    
    // Formas para botones (Estilo Airbnb)
    val button = RoundedCornerShape(12.dp)
    val buttonSmall = RoundedCornerShape(8.dp)
    val buttonLarge = RoundedCornerShape(16.dp)
    
    // Formas para tarjetas (Estilo Airbnb)
    val card = RoundedCornerShape(16.dp)
    val cardSmall = RoundedCornerShape(12.dp)
    val cardLarge = RoundedCornerShape(20.dp)
    
    // Formas para chips (Estilo Airbnb)
    val chip = RoundedCornerShape(20.dp)
    val chipLarge = RoundedCornerShape(24.dp)
    
    // Formas para campos de texto (Estilo Airbnb)
    val textField = RoundedCornerShape(12.dp)
    val textFieldSmall = RoundedCornerShape(8.dp)
    
    // Formas para diálogos (Estilo Airbnb)
    val dialog = RoundedCornerShape(20.dp)
    val dialogSmall = RoundedCornerShape(16.dp)
    
    // Formas para modales (Estilo Airbnb)
    val modal = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    val sideSheet = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
    
    // Formas para avatares
    val avatar = RoundedCornerShape(50.dp)
    val avatarSmall = RoundedCornerShape(50.dp)
    
    // Formas para imágenes (Estilo Airbnb)
    val image = RoundedCornerShape(12.dp)
    val imageSmall = RoundedCornerShape(8.dp)
    val imageLarge = RoundedCornerShape(16.dp)
    
    // Formas para badges (Estilo Airbnb)
    val badge = RoundedCornerShape(50.dp)
    val badgeSquare = RoundedCornerShape(6.dp)
    
    // Formas para indicadores (Estilo Airbnb)
    val indicator = RoundedCornerShape(50.dp)
    val indicatorSquare = RoundedCornerShape(6.dp)
    
    // Formas para tabs (Estilo Airbnb)
    val tab = RoundedCornerShape(12.dp)
    val tabIndicator = RoundedCornerShape(6.dp)
    
    // Formas para listas (Estilo Airbnb)
    val listItem = RoundedCornerShape(12.dp)
    val listItemFirst = RoundedCornerShape(
        topStart = 12.dp,
        topEnd = 12.dp,
        bottomStart = 4.dp,
        bottomEnd = 4.dp
    )
    val listItemLast = RoundedCornerShape(
        topStart = 4.dp,
        topEnd = 4.dp,
        bottomStart = 12.dp,
        bottomEnd = 12.dp
    )
    val listItemMiddle = RoundedCornerShape(4.dp)
    val listItemSingle = RoundedCornerShape(12.dp)
    
    // Formas para mapas (Estilo Airbnb)
    val mapMarker = RoundedCornerShape(12.dp)
    val mapCluster = RoundedCornerShape(50.dp)
    val mapInfoWindow = RoundedCornerShape(12.dp)
    
    // Formas para carruseles (Estilo Airbnb)
    val carousel = RoundedCornerShape(12.dp)
    val carouselIndicator = RoundedCornerShape(50.dp)
    
    // Formas para shimmer (Estilo Airbnb)
    val shimmer = RoundedCornerShape(8.dp)
    val shimmerLarge = RoundedCornerShape(12.dp)
    
    // Formas para skeleton (Estilo Airbnb)
    val skeleton = RoundedCornerShape(8.dp)
    val skeletonCard = RoundedCornerShape(16.dp)
    
    // Formas para feedback (Estilo Airbnb)
    val snackbar = RoundedCornerShape(12.dp)
    val banner = RoundedCornerShape(8.dp)
    
    // Formas para navegación (Estilo Airbnb)
    val bottomNav = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 20.dp
    )
    val topAppBar = RoundedCornerShape(
        bottomStart = 20.dp,
        bottomEnd = 20.dp
    )
    
    // Formas para floating action button
    val fab = RoundedCornerShape(50.dp)
    val fabSmall = RoundedCornerShape(50.dp)
    val fabLarge = RoundedCornerShape(50.dp)
    
    // Formas para dividers
    val divider = RoundedCornerShape(4.dp)
    
    // Formas para progress indicators
    val progressBar = RoundedCornerShape(50.dp)
    val progressBarLinear = RoundedCornerShape(4.dp)
    
    // Formas para switches
    val switch = RoundedCornerShape(50.dp)
    val switchTrack = RoundedCornerShape(50.dp)
    val switchThumb = RoundedCornerShape(50.dp)
    
    // Formas para sliders
    val slider = RoundedCornerShape(50.dp)
    val sliderTrack = RoundedCornerShape(4.dp)
    val sliderThumb = RoundedCornerShape(50.dp)
    
    // Formas para checkboxes y radio buttons
    val checkbox = RoundedCornerShape(4.dp)
    val radioButton = RoundedCornerShape(50.dp)
    
    // Formas para tooltips
    val tooltip = RoundedCornerShape(8.dp)
    
    // Formas para dropdowns
    val dropdown = RoundedCornerShape(16.dp)
    val dropdownItem = RoundedCornerShape(8.dp)
    
    // Formas para search bars
    val searchBar = RoundedCornerShape(16.dp)
    val searchBarExpanded = RoundedCornerShape(24.dp)
    
    // Formas para cards de estadísticas
    val statCard = RoundedCornerShape(24.dp)
    val statCardSmall = RoundedCornerShape(16.dp)
    
    // Formas para cards de incidentes
    val incidentCard = RoundedCornerShape(24.dp)
    val incidentCardCompact = RoundedCornerShape(16.dp)
    
    // Formas para cards de perfil
    val profileCard = RoundedCornerShape(28.dp)
    val profileAvatar = RoundedCornerShape(50.dp)
    
    // Formas para cards de mapa
    val mapCard = RoundedCornerShape(24.dp)
    val mapCardCompact = RoundedCornerShape(16.dp)
    
    // Formas para cards de notificaciones
    val notificationCard = RoundedCornerShape(16.dp)
    val notificationBadge = RoundedCornerShape(50.dp)
    
    // Formas para cards de configuración
    val settingsCard = RoundedCornerShape(16.dp)
    val settingsItem = RoundedCornerShape(8.dp)
    
    // Formas para cards de ayuda
    val helpCard = RoundedCornerShape(24.dp)
    val helpItem = RoundedCornerShape(16.dp)
    
    // Formas para cards de contacto
    val contactCard = RoundedCornerShape(24.dp)
    val contactItem = RoundedCornerShape(16.dp)
    
    // Formas para cards de reportes
    val reportCard = RoundedCornerShape(24.dp)
    val reportItem = RoundedCornerShape(16.dp)
    
    // Formas para cards de administración
    val adminCard = RoundedCornerShape(24.dp)
    val adminItem = RoundedCornerShape(16.dp)
}
