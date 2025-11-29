package com.example.points.constants

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import com.example.points.ui.theme.Spacing

/**
 * Espaciados comunes utilizados en la aplicación
 */
object AppSpacing {
    val SMALL: Dp
        @Composable get() = Spacing.xs
    val MEDIUM: Dp
        @Composable get() = Spacing.sm
    val STANDARD: Dp
        @Composable get() = Spacing.lg
    val LARGE: Dp
        @Composable get() = Spacing.xxl
    val EXTRA_LARGE: Dp
        @Composable get() = Spacing.xxxl
    
    // Espaciados específicos
    val CARD_PADDING: Dp
        @Composable get() = Spacing.lg
    val SCREEN_PADDING: Dp
        @Composable get() = Spacing.xxl
    val BUTTON_SPACING: Dp
        @Composable get() = Spacing.sm
    val SECTION_SPACING: Dp
        @Composable get() = Spacing.xxl
}

