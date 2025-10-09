package com.example.points.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.points.ui.theme.*

/**
 * Sistema de carga para POINTS
 * Shimmer, skeleton y placeholders con animaciones suaves
 */

/**
 * Modificador para aplicar efecto shimmer
 */
fun Modifier.shimmerEffect(
    isLoading: Boolean,
    shape: androidx.compose.ui.graphics.Shape = PointsCustomShapes.shimmer,
    color: Color = ShimmerBase,
    highlightColor: Color = ShimmerHighlight
): Modifier = composed {
    if (isLoading) {
        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnimation = transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1200,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer_translate"
        )
        
        val brush = Brush.linearGradient(
            colors = listOf(
                color,
                highlightColor,
                color
            ),
            start = Offset.Zero,
            end = Offset(x = translateAnimation.value, y = translateAnimation.value)
        )
        
        this.background(brush, shape)
    } else {
        this
    }
}

/**
 * Placeholder con efecto shimmer
 */
@Composable
fun ShimmerPlaceholder(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = PointsCustomShapes.shimmer,
    color: Color = ShimmerBase,
    highlightColor: Color = ShimmerHighlight
) {
    Box(
        modifier = modifier
            .shimmerEffect(
                isLoading = true,
                shape = shape,
                color = color,
                highlightColor = highlightColor
            )
    )
}

/**
 * Skeleton para tarjetas de incidentes
 */
@Composable
fun IncidentCardSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(PointsCustomShapes.incidentCard),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Título
            ShimmerPlaceholder(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(20.dp)
            )
            
            // Descripción
            ShimmerPlaceholder(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
            )
            
            ShimmerPlaceholder(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(16.dp)
            )
            
            // Metadatos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShimmerPlaceholder(
                    modifier = Modifier
                        .width(60.dp)
                        .height(24.dp)
                        .clip(PointsCustomShapes.chip)
                )
                
                ShimmerPlaceholder(
                    modifier = Modifier
                        .width(80.dp)
                        .height(24.dp)
                        .clip(PointsCustomShapes.chip)
                )
            }
        }
    }
}

/**
 * Skeleton para tarjetas de estadísticas
 */
@Composable
fun StatCardSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(PointsCustomShapes.statCard),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            // Icono
            ShimmerPlaceholder(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
            )
            
            // Valor
            ShimmerPlaceholder(
                modifier = Modifier
                    .width(60.dp)
                    .height(24.dp)
            )
            
            // Etiqueta
            ShimmerPlaceholder(
                modifier = Modifier
                    .width(80.dp)
                    .height(16.dp)
            )
        }
    }
}

/**
 * Skeleton para lista de incidentes
 */
@Composable
fun IncidentListSkeleton(
    modifier: Modifier = Modifier,
    itemCount: Int = 5
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(itemCount) {
            IncidentCardSkeleton()
        }
    }
}

/**
 * Skeleton para grid de estadísticas
 */
@Composable
fun StatGridSkeleton(
    modifier: Modifier = Modifier,
    columns: Int = 2,
    itemCount: Int = 4
) {
    val rows = (itemCount + columns - 1) / columns
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(rows) { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(columns) { col ->
                    val index = row * columns + col
                    if (index < itemCount) {
                        StatCardSkeleton(
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * Indicador de carga circular
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 24.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = color,
        strokeWidth = 2.dp
    )
}

/**
 * Indicador de carga con texto
 */
@Composable
fun LoadingWithText(
    text: String,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 24.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Column(
        modifier = modifier,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LoadingIndicator(size = size, color = color)
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

/**
 * Función de conveniencia para mostrar carga con mensaje
 */
@Composable
fun PointsLoading(
    message: String = "Cargando...",
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 24.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    LoadingWithText(
        text = message,
        modifier = modifier,
        size = size,
        color = color
    )
}

/**
 * Previews de los componentes de carga
 */
@Preview(showBackground = true, name = "Shimmer Placeholder")
@Composable
private fun ShimmerPlaceholderPreview() {
    PointsTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ShimmerPlaceholder(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
            )
            
            ShimmerPlaceholder(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Incident Card Skeleton")
@Composable
private fun IncidentCardSkeletonPreview() {
    PointsTheme {
        IncidentCardSkeleton()
    }
}

@Preview(showBackground = true, name = "Stat Card Skeleton")
@Composable
private fun StatCardSkeletonPreview() {
    PointsTheme {
        StatCardSkeleton()
    }
}

@Preview(showBackground = true, name = "Loading Indicator")
@Composable
private fun LoadingIndicatorPreview() {
    PointsTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            LoadingIndicator()
            LoadingWithText("Cargando datos...")
        }
    }
}