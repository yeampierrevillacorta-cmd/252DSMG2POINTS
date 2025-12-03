package com.example.points.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

/**
 * Fondo animado con efectos de partículas
 */
@Composable
fun AnimatedBackground(
    modifier: Modifier = Modifier,
    particleCount: Int = 20
) {
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    
    Canvas(modifier = modifier.fillMaxSize()) {
        // Fondo con gradiente
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.05f),
                    secondaryColor.copy(alpha = 0.03f),
                    Color.Transparent
                )
            )
        )
        
        // Partículas animadas
        repeat(particleCount) { index ->
            val angle = (animatedOffset + index * (360f / particleCount)) * Math.PI / 180
            val radius = size.minDimension * 0.3f
            
            val x = (size.width / 2) + (cos(angle) * radius).toFloat()
            val y = (size.height / 2) + (sin(angle) * radius).toFloat()
            
            val color = when (index % 3) {
                0 -> primaryColor
                1 -> secondaryColor
                else -> tertiaryColor
            }
            
            drawCircle(
                color = color.copy(alpha = 0.1f),
                radius = 40f + (index % 3) * 20f,
                center = Offset(x, y)
            )
        }
    }
}

/**
 * Fondo con gradiente animado
 */
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    
    val colorOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color_offset"
    )
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.1f * (1 - colorOffset)),
                    secondaryColor.copy(alpha = 0.15f * colorOffset),
                    tertiaryColor.copy(alpha = 0.1f * (1 - colorOffset)),
                    Color.Transparent
                )
            )
        )
    }
}

