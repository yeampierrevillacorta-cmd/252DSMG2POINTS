package com.example.points.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin

/**
 * Design Tokens para la aplicación POINTS
 * Sistema de tokens basado en Material 3 y la identidad de marca
 */

/**
 * Sistema de espaciado basado en 8pt grid
 */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
    val xxxxl = 40.dp
}

/**
 * Sistema de radios de esquinas
 */
object Radius {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 28.dp
    val round = 50.dp
}

/**
 * Sistema de elevación y sombras
 */
object Elevation {
    val none = 0.dp
    val xs = 1.dp
    val sm = 3.dp
    val md = 6.dp
    val lg = 12.dp
    val xl = 16.dp
}

/**
 * Sistema de duraciones de animación
 */
object Duration {
    val fast = 120
    val medium = 180
    val slow = 240
    val slower = 320
}

/**
 * Sistema de easing para animaciones
 */
object Easing {
    val standard = FastOutSlowInEasing
    val fastOutSlowIn = FastOutSlowInEasing
    val linear = LinearEasing
    val easeInOut = EaseInOut
}

/**
 * Sistema de trazos y bordes
 */
object Stroke {
    val thin = 0.5.dp
    val normal = 1.dp
    val thick = 2.dp
    val extraThick = 3.dp
    
    val cap = StrokeCap.Round
    val join = StrokeJoin.Round
}

/**
 * Sistema de z-index para capas
 */
object ZIndex {
    val content = 0f
    val surface = 1f
    val dropdown = 2f
    val modal = 3f
    val tooltip = 4f
    val snackbar = 5f
    val banner = 6f
}

/**
 * Sistema de tamaños de iconos
 */
object IconSize {
    val xs = 12.dp
    val sm = 16.dp
    val md = 20.dp
    val lg = 24.dp
    val xl = 28.dp
    val xxl = 32.dp
    val xxxl = 40.dp
    val xxxxl = 48.dp
}

/**
 * Sistema de tamaños de avatares
 */
object AvatarSize {
    val xs = 24.dp
    val sm = 32.dp
    val md = 40.dp
    val lg = 48.dp
    val xl = 56.dp
    val xxl = 64.dp
    val xxxl = 80.dp
}

/**
 * Sistema de tamaños de botones
 */
object ButtonSize {
    val height = 40.dp
    val heightLarge = 48.dp
    val heightSmall = 32.dp
    val minWidth = 64.dp
    val paddingHorizontal = Spacing.lg
    val paddingVertical = Spacing.sm
}

/**
 * Sistema de tamaños de chips
 */
object ChipSize {
    val height = 32.dp
    val heightLarge = 40.dp
    val paddingHorizontal = Spacing.md
    val paddingVertical = Spacing.xs
}

/**
 * Sistema de tamaños de campos de texto
 */
object TextFieldSize {
    val height = 56.dp
    val heightSmall = 48.dp
    val paddingHorizontal = Spacing.md
    val paddingVertical = Spacing.sm
}

/**
 * Sistema de tamaños de tarjetas
 */
object CardSize {
    val padding = Spacing.lg
    val paddingSmall = Spacing.md
    val paddingLarge = Spacing.xl
    val minHeight = 80.dp
}

/**
 * Sistema de tamaños de listas
 */
object ListSize {
    val itemHeight = 56.dp
    val itemHeightLarge = 72.dp
    val itemHeightSmall = 48.dp
    val paddingHorizontal = Spacing.lg
    val paddingVertical = Spacing.sm
}

/**
 * Sistema de tamaños de diálogos
 */
object DialogSize {
    val padding = Spacing.xl
    val paddingSmall = Spacing.lg
    val minWidth = 280.dp
    val maxWidth = 560.dp
}

/**
 * Sistema de tamaños de snackbars
 */
object SnackbarSize {
    val padding = Spacing.lg
    val minHeight = 48.dp
    val maxWidth = 344.dp
}

/**
 * Sistema de tamaños de banners
 */
object BannerSize {
    val padding = Spacing.lg
    val minHeight = 56.dp
    val iconSize = IconSize.lg
}

/**
 * Sistema de tamaños de badges
 */
object BadgeSize {
    val size = 16.dp
    val sizeLarge = 20.dp
    val padding = Spacing.xs
    val fontSize = 10.sp
    val fontSizeLarge = 12.sp
}

/**
 * Sistema de tamaños de indicadores
 */
object IndicatorSize {
    val size = 8.dp
    val sizeLarge = 12.dp
    val spacing = Spacing.xs
}

/**
 * Sistema de tamaños de separadores
 */
object DividerSize {
    val thickness = Stroke.normal
    val padding = Spacing.lg
}

/**
 * Sistema de tamaños de tabs
 */
object TabSize {
    val height = 48.dp
    val paddingHorizontal = Spacing.lg
    val paddingVertical = Spacing.sm
    val indicatorHeight = 3.dp
}

/**
 * Sistema de tamaños de bottom navigation
 */
object BottomNavSize {
    val height = 80.dp
    val iconSize = IconSize.lg
    val labelSize = 12.sp
    val padding = Spacing.sm
}

/**
 * Sistema de tamaños de top app bar
 */
object TopAppBarSize {
    val height = 64.dp
    val heightLarge = 152.dp
    val padding = Spacing.lg
    val iconSize = IconSize.lg
}

/**
 * Sistema de tamaños de floating action button
 */
object FabSize {
    val size = 56.dp
    val sizeSmall = 40.dp
    val sizeLarge = 64.dp
    val iconSize = IconSize.lg
}

/**
 * Sistema de tamaños de mapas
 */
object MapSize {
    val markerSize = 40.dp
    val clusterSize = 32.dp
    val clusterTextSize = 12.sp
    val infoWindowPadding = Spacing.md
}

/**
 * Sistema de tamaños de imágenes
 */
object ImageSize {
    val thumbnail = 64.dp
    val small = 96.dp
    val medium = 128.dp
    val large = 192.dp
    val xlarge = 256.dp
    val aspectRatio = 16f / 9f
}

/**
 * Sistema de tamaños de carruseles
 */
object CarouselSize {
    val height = 200.dp
    val heightLarge = 300.dp
    val indicatorSize = IndicatorSize.size
    val indicatorSpacing = Spacing.sm
}

/**
 * Sistema de tamaños de shimmer
 */
object ShimmerSize {
    val height = 20.dp
    val heightLarge = 24.dp
    val heightSmall = 16.dp
    val borderRadius = Radius.sm
}

/**
 * Sistema de tamaños de skeleton
 */
object SkeletonSize {
    val cardHeight = 120.dp
    val listItemHeight = 72.dp
    val avatarSize = AvatarSize.lg
    val borderRadius = Radius.md
}
