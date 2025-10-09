package com.example.points.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Sistema de formas para la aplicación POINTS
 * Basado en Material 3 con esquinas redondeadas consistentes
 * Utiliza el sistema de tokens de radio definido en DesignTokens
 */

val PointsShapes = Shapes(
    // Formas extra pequeñas - Para chips y badges
    extraSmall = RoundedCornerShape(4.dp),
    
    // Formas pequeñas - Para botones pequeños y elementos compactos
    small = RoundedCornerShape(8.dp),
    
    // Formas medianas - Para botones estándar y tarjetas pequeñas
    medium = RoundedCornerShape(16.dp),
    
    // Formas grandes - Para tarjetas principales y contenedores
    large = RoundedCornerShape(24.dp),
    
    // Formas extra grandes - Para modales y sheets
    extraLarge = RoundedCornerShape(28.dp),
)

/**
 * Formas personalizadas para casos específicos de POINTS
 */
object PointsCustomShapes {
    
    // Formas para botones
    val button = RoundedCornerShape(16.dp)
    val buttonSmall = RoundedCornerShape(8.dp)
    val buttonLarge = RoundedCornerShape(20.dp)
    
    // Formas para tarjetas
    val card = RoundedCornerShape(24.dp)
    val cardSmall = RoundedCornerShape(16.dp)
    val cardLarge = RoundedCornerShape(28.dp)
    
    // Formas para chips
    val chip = RoundedCornerShape(8.dp)
    val chipLarge = RoundedCornerShape(12.dp)
    
    // Formas para campos de texto
    val textField = RoundedCornerShape(12.dp)
    val textFieldSmall = RoundedCornerShape(8.dp)
    
    // Formas para diálogos
    val dialog = RoundedCornerShape(28.dp)
    val dialogSmall = RoundedCornerShape(24.dp)
    
    // Formas para modales
    val modal = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val sideSheet = RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp)
    
    // Formas para avatares
    val avatar = RoundedCornerShape(50.dp)
    val avatarSmall = RoundedCornerShape(50.dp)
    
    // Formas para imágenes
    val image = RoundedCornerShape(16.dp)
    val imageSmall = RoundedCornerShape(8.dp)
    val imageLarge = RoundedCornerShape(24.dp)
    
    // Formas para badges
    val badge = RoundedCornerShape(50.dp)
    val badgeSquare = RoundedCornerShape(4.dp)
    
    // Formas para indicadores
    val indicator = RoundedCornerShape(50.dp)
    val indicatorSquare = RoundedCornerShape(4.dp)
    
    // Formas para tabs
    val tab = RoundedCornerShape(8.dp)
    val tabIndicator = RoundedCornerShape(4.dp)
    
    // Formas para listas
    val listItem = RoundedCornerShape(16.dp)
    val listItemFirst = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = 4.dp,
        bottomEnd = 4.dp
    )
    val listItemLast = RoundedCornerShape(
        topStart = 4.dp,
        topEnd = 4.dp,
        bottomStart = 16.dp,
        bottomEnd = 16.dp
    )
    val listItemMiddle = RoundedCornerShape(4.dp)
    val listItemSingle = RoundedCornerShape(16.dp)
    
    // Formas para mapas
    val mapMarker = RoundedCornerShape(8.dp)
    val mapCluster = RoundedCornerShape(50.dp)
    val mapInfoWindow = RoundedCornerShape(16.dp)
    
    // Formas para carruseles
    val carousel = RoundedCornerShape(16.dp)
    val carouselIndicator = RoundedCornerShape(50.dp)
    
    // Formas para shimmer
    val shimmer = RoundedCornerShape(8.dp)
    val shimmerLarge = RoundedCornerShape(16.dp)
    
    // Formas para skeleton
    val skeleton = RoundedCornerShape(12.dp)
    val skeletonCard = RoundedCornerShape(24.dp)
    
    // Formas para feedback
    val snackbar = RoundedCornerShape(16.dp)
    val banner = RoundedCornerShape(8.dp)
    
    // Formas para navegación
    val bottomNav = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp
    )
    val topAppBar = RoundedCornerShape(
        bottomStart = 24.dp,
        bottomEnd = 24.dp
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
