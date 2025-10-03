package com.example.points.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valentinilk.shimmer.shimmer

/**
 * Panel con efecto de cristal (Glassmorphism)
 */
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.2f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            content()
        }
    }
}

/**
 * Panel con efecto de neón
 */
@Composable
fun NeonPanel(
    modifier: Modifier = Modifier,
    neonColor: Color = Color.Cyan,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "neon")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    
    Card(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = neonColor.copy(alpha = glowAlpha),
                spotColor = neonColor.copy(alpha = glowAlpha)
            ),
        colors = CardDefaults.cardColors(
            containerColor = neonColor.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            neonColor.copy(alpha = 0.2f),
                            neonColor.copy(alpha = 0.05f)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    color = neonColor.copy(alpha = glowAlpha),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            content()
        }
    }
}

/**
 * Panel con efecto de metal
 */
@Composable
fun MetalPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C2C2C)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF4A4A4A),
                            Color(0xFF2C2C2C),
                            Color(0xFF1A1A1A)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color(0xFF6A6A6A),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            content()
        }
    }
}

/**
 * Panel con efecto de papel
 */
@Composable
fun PaperPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFF5F5F5)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color(0xFFE0E0E0),
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            content()
        }
    }
}

/**
 * Panel con efecto de holograma
 */
@Composable
fun HologramPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hologram")
    val scanLine by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_line"
    )
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0A0A0A)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A2E),
                            Color(0xFF16213E),
                            Color(0xFF0A0A0A)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    color = Color(0xFF00D4FF),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            // Línea de escaneo animada
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .offset(y = ((scanLine - 0.5f) * 200f).dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFF00D4FF),
                                Color.Transparent
                            )
                        )
                    )
            )
            
            content()
        }
    }
}

/**
 * Panel con efecto de cristal esmerilado
 */
@Composable
fun FrostedGlassPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.25f),
                            Color.White.copy(alpha = 0.1f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            content()
        }
    }
}

/**
 * Panel con efecto de carga (Shimmer)
 */
@Composable
fun ShimmerPanel(
    modifier: Modifier = Modifier,
    isLoading: Boolean = true,
    content: @Composable () -> Unit
) {
    if (isLoading) {
        Box(
            modifier = modifier
                .shimmer()
                .background(
                    Color.Gray.copy(alpha = 0.3f),
                    RoundedCornerShape(12.dp)
                )
        ) {
            // Placeholder content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(16.dp)
                        .background(Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(12.dp)
                        .background(Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(12.dp)
                        .background(Color.Gray.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                )
            }
        }
    } else {
        content()
    }
}

/**
 * Panel con efecto de partículas
 */
@Composable
fun ParticlePanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF2D1B69),
                            Color(0xFF1A1A2E)
                        )
                    )
                )
        ) {
            // Partículas animadas
            repeat(5) { index ->
                val particleAlpha by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 2000 + (index * 400),
                            easing = EaseInOutSine
                        ),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "particle_$index"
                )
                
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .offset(
                            x = (index * 20).dp,
                            y = (index * 15).dp
                        )
                        .background(
                            Color.Cyan.copy(alpha = particleAlpha),
                            CircleShape
                        )
                )
            }
            
            content()
        }
    }
}

/**
 * Panel con efecto de ondas
 */
@Composable
fun WavePanel(
    modifier: Modifier = Modifier,
    waveColor: Color = Color.Blue,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waves")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = waveColor.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            waveColor.copy(alpha = 0.2f),
                            waveColor.copy(alpha = 0.05f)
                        )
                    )
                )
        ) {
            // Ondas animadas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .offset(y = ((waveOffset - 0.5f) * 40f).dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                waveColor.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )
            
            content()
        }
    }
}
