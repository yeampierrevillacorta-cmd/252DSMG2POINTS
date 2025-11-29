package com.example.points.ui.theme

import androidx.compose.runtime.Composable
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
 * Inspirado en Airbnb - Espaciado generoso y limpio
 * Sistema de tokens optimizado para estética moderna
 */

/**
 * Sistema de espaciado basado en 8pt grid (Estilo Airbnb)
 */
@Composable
private fun scaledDp(value: Float): Dp = (value * LocalSpacingScale.current).dp

object Spacing {
    val xs: Dp
        @Composable get() = scaledDp(4f)
    val sm: Dp
        @Composable get() = scaledDp(8f)
    val md: Dp
        @Composable get() = scaledDp(12f)
    val lg: Dp
        @Composable get() = scaledDp(16f)
    val xl: Dp
        @Composable get() = scaledDp(20f)
    val xxl: Dp
        @Composable get() = scaledDp(24f)
    val xxxl: Dp
        @Composable get() = scaledDp(32f)
    val xxxxl: Dp
        @Composable get() = scaledDp(40f)
    val xxxxxl: Dp
        @Composable get() = scaledDp(48f)
    val xxxxxxl: Dp
        @Composable get() = scaledDp(64f)
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
 * Sistema de elevación y sombras (Estilo Airbnb)
 */
object Elevation {
    val none = 0.dp
    val xs = 1.dp
    val sm = 2.dp
    val md = 4.dp
    val lg = 8.dp
    val xl = 12.dp
    val xxl = 16.dp
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
 * Sistema de tamaños de botones (Estilo Airbnb)
 */
object ButtonSize {
    val height: Dp
        @Composable get() = scaledDp(44f)
    val heightLarge: Dp
        @Composable get() = scaledDp(52f)
    val heightSmall: Dp
        @Composable get() = scaledDp(36f)
    val minWidth: Dp
        @Composable get() = scaledDp(80f)
    val paddingHorizontal: Dp
        @Composable get() = Spacing.xl
    val paddingVertical: Dp
        @Composable get() = Spacing.md
}

/**
 * Sistema de tamaños de chips
 */
object ChipSize {
    val height: Dp
        @Composable get() = scaledDp(32f)
    val heightLarge: Dp
        @Composable get() = scaledDp(40f)
    val paddingHorizontal: Dp
        @Composable get() = Spacing.md
    val paddingVertical: Dp
        @Composable get() = Spacing.xs
}

/**
 * Sistema de tamaños de campos de texto
 */
object TextFieldSize {
    val height: Dp
        @Composable get() = scaledDp(56f)
    val heightSmall: Dp
        @Composable get() = scaledDp(48f)
    val paddingHorizontal: Dp
        @Composable get() = Spacing.md
    val paddingVertical: Dp
        @Composable get() = Spacing.sm
}

/**
 * Sistema de tamaños de tarjetas (Estilo Airbnb)
 */
object CardSize {
    val padding: Dp
        @Composable get() = Spacing.xl
    val paddingSmall: Dp
        @Composable get() = Spacing.lg
    val paddingLarge: Dp
        @Composable get() = Spacing.xxl
    val minHeight: Dp
        @Composable get() = scaledDp(100f)
}

/**
 * Sistema de tamaños de listas
 */
object ListSize {
    val itemHeight: Dp
        @Composable get() = scaledDp(56f)
    val itemHeightLarge: Dp
        @Composable get() = scaledDp(72f)
    val itemHeightSmall: Dp
        @Composable get() = scaledDp(48f)
    val paddingHorizontal: Dp
        @Composable get() = Spacing.lg
    val paddingVertical: Dp
        @Composable get() = Spacing.sm
}

/**
 * Sistema de tamaños de diálogos
 */
object DialogSize {
    val padding: Dp
        @Composable get() = Spacing.xl
    val paddingSmall: Dp
        @Composable get() = Spacing.lg
    val minWidth: Dp
        @Composable get() = scaledDp(280f)
    val maxWidth: Dp
        @Composable get() = scaledDp(560f)
}

/**
 * Sistema de tamaños de snackbars
 */
object SnackbarSize {
    val padding: Dp
        @Composable get() = Spacing.lg
    val minHeight: Dp
        @Composable get() = scaledDp(48f)
    val maxWidth: Dp
        @Composable get() = scaledDp(344f)
}

/**
 * Sistema de tamaños de banners
 */
object BannerSize {
    val padding: Dp
        @Composable get() = Spacing.lg
    val minHeight: Dp
        @Composable get() = scaledDp(56f)
    val iconSize: Dp
        @Composable get() = IconSize.lg
}

/**
 * Sistema de tamaños de badges
 */
object BadgeSize {
    val size: Dp
        @Composable get() = scaledDp(16f)
    val sizeLarge: Dp
        @Composable get() = scaledDp(20f)
    val padding: Dp
        @Composable get() = Spacing.xs
    val fontSize = 10.sp
    val fontSizeLarge = 12.sp
}

/**
 * Sistema de tamaños de indicadores
 */
object IndicatorSize {
    val size: Dp
        @Composable get() = scaledDp(8f)
    val sizeLarge: Dp
        @Composable get() = scaledDp(12f)
    val spacing: Dp
        @Composable get() = Spacing.xs
}

/**
 * Sistema de tamaños de separadores
 */
object DividerSize {
    val thickness: Dp
        @Composable get() = Stroke.normal
    val padding: Dp
        @Composable get() = Spacing.lg
}

/**
 * Sistema de tamaños de tabs
 */
object TabSize {
    val height: Dp
        @Composable get() = scaledDp(48f)
    val paddingHorizontal: Dp
        @Composable get() = Spacing.lg
    val paddingVertical: Dp
        @Composable get() = Spacing.sm
    val indicatorHeight: Dp
        @Composable get() = scaledDp(3f)
}

/**
 * Sistema de tamaños de bottom navigation
 */
object BottomNavSize {
    val height: Dp
        @Composable get() = scaledDp(80f)
    val iconSize: Dp
        @Composable get() = IconSize.lg
    val labelSize = 12.sp
    val padding: Dp
        @Composable get() = Spacing.sm
}

/**
 * Sistema de tamaños de top app bar
 */
object TopAppBarSize {
    val height: Dp
        @Composable get() = scaledDp(64f)
    val heightLarge: Dp
        @Composable get() = scaledDp(152f)
    val padding: Dp
        @Composable get() = Spacing.lg
    val iconSize: Dp
        @Composable get() = IconSize.lg
}

/**
 * Sistema de tamaños de floating action button
 */
object FabSize {
    val size: Dp
        @Composable get() = scaledDp(56f)
    val sizeSmall: Dp
        @Composable get() = scaledDp(40f)
    val sizeLarge: Dp
        @Composable get() = scaledDp(64f)
    val iconSize: Dp
        @Composable get() = IconSize.lg
}

/**
 * Sistema de tamaños de mapas
 */
object MapSize {
    val markerSize: Dp
        @Composable get() = scaledDp(40f)
    val clusterSize: Dp
        @Composable get() = scaledDp(32f)
    val clusterTextSize = 12.sp
    val infoWindowPadding: Dp
        @Composable get() = Spacing.md
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
    val height: Dp
        @Composable get() = scaledDp(200f)
    val heightLarge: Dp
        @Composable get() = scaledDp(300f)
    val indicatorSize: Dp
        @Composable get() = IndicatorSize.size
    val indicatorSpacing: Dp
        @Composable get() = Spacing.sm
}

/**
 * Sistema de tamaños de shimmer
 */
object ShimmerSize {
    val height: Dp
        @Composable get() = scaledDp(20f)
    val heightLarge: Dp
        @Composable get() = scaledDp(24f)
    val heightSmall: Dp
        @Composable get() = scaledDp(16f)
    val borderRadius: Dp
        @Composable get() = Radius.sm
}

/**
 * Sistema de tamaños de skeleton
 */
object SkeletonSize {
    val cardHeight: Dp
        @Composable get() = scaledDp(120f)
    val listItemHeight: Dp
        @Composable get() = scaledDp(72f)
    val avatarSize: Dp
        @Composable get() = AvatarSize.lg
    val borderRadius: Dp
        @Composable get() = Radius.md
}
