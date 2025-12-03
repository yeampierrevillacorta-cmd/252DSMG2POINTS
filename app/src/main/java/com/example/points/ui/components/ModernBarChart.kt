package com.example.points.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.points.ui.theme.PointsPrimary
import kotlinx.coroutines.delay

/**
 * Datos para el gráfico de barras
 */
data class BarChartData(
    val label: String,
    val value: Float,
    val color: Color,
    val icon: String? = null
)

/**
 * Gráfico de barras moderno y profesional con animaciones
 */
@Composable
fun ModernBarChart(
    data: List<BarChartData>,
    modifier: Modifier = Modifier,
    title: String? = null,
    showValues: Boolean = true,
    showGrid: Boolean = true,
    animationDuration: Int = 1000
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress = remember { Animatable(0f) }
    
    LaunchedEffect(key1 = data) {
        animationPlayed = false
        delay(100)
        animationPlayed = true
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = animationDuration,
                easing = EaseOutCubic
            )
        )
    }
    
    val maxValue = data.maxOfOrNull { it.value } ?: 1f
    
    ModernCard(
        modifier = modifier,
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Título
            if (title != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Valor máximo
                    Text(
                        text = "Máx: ${maxValue.toInt()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
            }
            
            // Gráfico
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val chartWidth = size.width
                    val chartHeight = size.height - 40.dp.toPx() // Espacio para etiquetas
                    val barWidth = (chartWidth / data.size) * 0.6f
                    val spacing = (chartWidth / data.size) * 0.4f
                    
                    // Dibujar líneas de cuadrícula si está habilitado
                    if (showGrid) {
                        drawGridLines(chartHeight, maxValue)
                    }
                    
                    // Dibujar barras
                    data.forEachIndexed { index, barData ->
                        val barHeight = (barData.value / maxValue) * chartHeight * animatedProgress.value
                        val x = index * (chartWidth / data.size) + spacing / 2
                        val y = chartHeight - barHeight
                        
                        // Sombra de la barra
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.1f),
                                    Color.Transparent
                                ),
                                startY = y + barHeight,
                                endY = y + barHeight + 10f
                            ),
                            topLeft = Offset(x + 2f, y + barHeight),
                            size = Size(barWidth, 10f),
                            cornerRadius = CornerRadius(8.dp.toPx())
                        )
                        
                        // Barra principal con gradiente
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    barData.color,
                                    barData.color.copy(alpha = 0.7f)
                                )
                            ),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                        
                        // Highlight en la parte superior de la barra
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.3f),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight * 0.3f),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                    }
                }
                
                // Etiquetas y valores
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        data.forEach { barData ->
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Valor
                                if (showValues && animationPlayed) {
                                    Text(
                                        text = barData.value.toInt().toString(),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = barData.color
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                // Etiqueta
                                Text(
                                    text = barData.label,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface.copy(0.7f),
                                    maxLines = 2,
                                    lineHeight = 12.sp
                                )
                            }
                        }
                    }
                }
            }
            
            // Leyenda
            if (data.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    data.take(3).forEach { barData ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(barData.color)
                            )
                            Text(
                                text = barData.label,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dibuja líneas de cuadrícula en el fondo del gráfico
 */
private fun DrawScope.drawGridLines(chartHeight: Float, maxValue: Float) {
    val gridLines = 5
    val gridColor = Color.Gray.copy(alpha = 0.1f)
    
    for (i in 0..gridLines) {
        val y = chartHeight - (chartHeight / gridLines) * i
        
        // Línea horizontal
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1.dp.toPx()
        )
    }
}

/**
 * Gráfico de barras horizontal
 */
@Composable
fun HorizontalBarChart(
    data: List<BarChartData>,
    modifier: Modifier = Modifier,
    title: String? = null,
    showPercentage: Boolean = true
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress = remember { Animatable(0f) }
    
    LaunchedEffect(key1 = data) {
        animationPlayed = false
        delay(100)
        animationPlayed = true
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = EaseOutCubic
            )
        )
    }
    
    val maxValue = data.maxOfOrNull { it.value } ?: 1f
    
    ModernCard(
        modifier = modifier,
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Título
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Barras horizontales
            data.forEach { barData ->
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = barData.label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(0.3f)
                        )
                        
                        Box(
                            modifier = Modifier
                                .weight(0.6f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.LightGray.copy(alpha = 0.2f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth((barData.value / maxValue) * animatedProgress.value)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                barData.color,
                                                barData.color.copy(alpha = 0.7f)
                                            )
                                        )
                                    )
                            )
                        }
                        
                        Text(
                            text = if (showPercentage) {
                                "${((barData.value / maxValue) * 100).toInt()}%"
                            } else {
                                barData.value.toInt().toString()
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = barData.color,
                            modifier = Modifier.weight(0.1f),
                            textAlign = TextAlign.End
                        )
                    }
                    
                    if (barData != data.last()) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

