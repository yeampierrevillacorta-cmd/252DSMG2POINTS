package com.example.points.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import kotlin.math.min

/**
 * CompositionLocal que expone un factor de escalado para espaciamiento
 * y tamaños basado en la ventana disponible.
 */
val LocalSpacingScale = staticCompositionLocalOf { 1f }

@Composable
internal fun rememberSpacingScale(): Float {
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp
    val heightDp = configuration.screenHeightDp
    val smallestSide = min(widthDp, heightDp)

    return when {
        widthDp >= 1200 || smallestSide >= 1000 -> 1.3f
        widthDp >= 840 -> 1.15f
        widthDp >= 600 -> 1.05f
        widthDp <= 360 -> 0.9f
        else -> 1f
    }
}

