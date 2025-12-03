package com.example.points.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Esquema de colores claro para POINTS
 * Basado en la identidad de marca con colores teal, verde y azul
 */
private val PointsLightColorScheme = lightColorScheme(
    // Colores primarios
    primary = PointsPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = PointsPrimary.copy(alpha = 0.12f),
    onPrimaryContainer = PointsPrimary.copy(alpha = 0.9f),
    
    // Colores secundarios
    secondary = PointsSecondary,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = PointsSecondary.copy(alpha = 0.12f),
    onSecondaryContainer = PointsSecondary.copy(alpha = 0.9f),
    
    // Colores terciarios
    tertiary = PointsTertiary,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    tertiaryContainer = PointsTertiary.copy(alpha = 0.12f),
    onTertiaryContainer = PointsTertiary.copy(alpha = 0.9f),
    
    // Colores de error
    error = PointsError,
    onError = androidx.compose.ui.graphics.Color.White,
    errorContainer = PointsError.copy(alpha = 0.12f),
    onErrorContainer = PointsError.copy(alpha = 0.9f),
    
    // Colores de superficie
    background = PointsSurface,
    onBackground = PointsInk,
    surface = PointsSurface,
    onSurface = PointsInk,
    surfaceVariant = PointsSurface.copy(alpha = 0.5f),
    onSurfaceVariant = PointsSubtle,
    
    // Colores de superficie elevada
    surfaceContainerHighest = PointsSurface,
    surfaceContainerHigh = PointsSurface.copy(alpha = 0.8f),
    surfaceContainer = PointsSurface.copy(alpha = 0.6f),
    surfaceContainerLow = PointsSurface.copy(alpha = 0.4f),
    surfaceContainerLowest = PointsSurface.copy(alpha = 0.2f),
    
    // Colores de borde y outline
    outline = PointsOutline,
    outlineVariant = PointsOutline.copy(alpha = 0.5f),
    
    // Colores de inversión
    inverseSurface = PointsInk,
    inversePrimary = PointsPrimary.copy(alpha = 0.8f),
    
    // Colores de scrim
    scrim = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.4f),
)

/**
 * Esquema de colores oscuro para POINTS
 * Adaptado para modo oscuro manteniendo la identidad de marca
 */
private val PointsDarkColorScheme = darkColorScheme(
    // Colores primarios
    primary = PointsPrimaryDark,
    onPrimary = PointsInk,
    primaryContainer = PointsPrimaryDark.copy(alpha = 0.2f),
    onPrimaryContainer = PointsPrimaryDark.copy(alpha = 0.9f),
    
    // Colores secundarios
    secondary = PointsSecondaryDark,
    onSecondary = PointsInk,
    secondaryContainer = PointsSecondaryDark.copy(alpha = 0.2f),
    onSecondaryContainer = PointsSecondaryDark.copy(alpha = 0.9f),
    
    // Colores terciarios
    tertiary = PointsTertiaryDark,
    onTertiary = PointsInk,
    tertiaryContainer = PointsTertiaryDark.copy(alpha = 0.2f),
    onTertiaryContainer = PointsTertiaryDark.copy(alpha = 0.9f),
    
    // Colores de error
    error = PointsError,
    onError = PointsInk,
    errorContainer = PointsError.copy(alpha = 0.2f),
    onErrorContainer = PointsError.copy(alpha = 0.9f),
    
    // Colores de superficie
    background = PointsSurfaceDark,
    onBackground = PointsOnSurfaceDark,
    surface = PointsSurfaceDark,
    onSurface = PointsOnSurfaceDark,
    surfaceVariant = PointsSurfaceVariantDark,
    onSurfaceVariant = PointsOnSurfaceDark.copy(alpha = 0.7f),
    
    // Colores de superficie elevada
    surfaceContainerHighest = PointsSurfaceVariantDark,
    surfaceContainerHigh = PointsSurfaceVariantDark.copy(alpha = 0.8f),
    surfaceContainer = PointsSurfaceVariantDark.copy(alpha = 0.6f),
    surfaceContainerLow = PointsSurfaceVariantDark.copy(alpha = 0.4f),
    surfaceContainerLowest = PointsSurfaceVariantDark.copy(alpha = 0.2f),
    
    // Colores de borde y outline
    outline = PointsOutlineDark,
    outlineVariant = PointsOutlineDark.copy(alpha = 0.5f),
    
    // Colores de inversión
    inverseSurface = PointsOnSurfaceDark,
    inversePrimary = PointsPrimaryDark.copy(alpha = 0.8f),
    
    // Colores de scrim
    scrim = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f),
)

/**
 * Tema principal de POINTS
 * Integra la identidad de marca con Material 3 y soporte para Dynamic Color
 * 
 * @param darkTheme Si usar el tema oscuro
 * @param dynamicColor Si usar Dynamic Color (Android 12+)
 * @param content Contenido a renderizar con el tema
 */
@Composable
fun PointsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) {
                // Usar Dynamic Color pero mantener los colores de marca como fallback
                try {
                    dynamicDarkColorScheme(context)
                } catch (e: Exception) {
                    PointsDarkColorScheme
                }
            } else {
                try {
                    dynamicLightColorScheme(context)
                } catch (e: Exception) {
                    PointsLightColorScheme
                }
            }
        }
        darkTheme -> PointsDarkColorScheme
        else -> PointsLightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    val spacingScale = rememberSpacingScale()

    CompositionLocalProvider(
        LocalSpacingScale provides spacingScale
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PointsTypography,
            shapes = PointsShapes,
            content = content
        )
    }
}