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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

/**
 * Tarjeta con efecto de elevación dinámica
 */
@Composable
fun DynamicElevatedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevation: Dp = 4.dp,
    hoverElevation: Dp = 8.dp,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .clickable(enabled = onClick != null) { 
                onClick?.invoke()
                isPressed = !isPressed
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) hoverElevation else elevation
        )
    ) {
        content()
    }
}

/**
 * Tarjeta con efecto de gradiente animado
 */
@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    colors: List<Color>,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_offset"
    )
    
    Card(
        modifier = modifier
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = colors,
                        start = androidx.compose.ui.geometry.Offset(
                            gradientOffset * 1000f,
                            gradientOffset * 1000f
                        ),
                        end = androidx.compose.ui.geometry.Offset(
                            (1f - gradientOffset) * 1000f,
                            (1f - gradientOffset) * 1000f
                        )
                    )
                )
        ) {
            content()
        }
    }
}

/**
 * Tarjeta con efecto de flip
 */
@Composable
fun FlipCard(
    modifier: Modifier = Modifier,
    frontContent: @Composable () -> Unit,
    backContent: @Composable () -> Unit,
    isFlipped: Boolean = false
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(600, easing = EaseInOutSine),
        label = "rotation"
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (rotation <= 90f) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            rotationY = rotation
                        }
                ) {
                    frontContent()
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            rotationY = rotation + 180f
                        }
                ) {
                    backContent()
                }
            }
        }
    }
}

/**
 * Tarjeta con efecto de expansión
 */
@Composable
fun ExpandableCard(
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    collapsedContent: @Composable () -> Unit,
    expandedContent: @Composable () -> Unit
) {
    val expandedHeight by animateDpAsState(
        targetValue = if (isExpanded) 200.dp else 80.dp,
        animationSpec = tween(300, easing = EaseInOutSine),
        label = "expanded_height"
    )
    
    Card(
        modifier = modifier
            .height(expandedHeight),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            expandedContent()
        }
        
        AnimatedVisibility(
            visible = !isExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            collapsedContent()
        }
    }
}

/**
 * Tarjeta con efecto de pulso
 */
@Composable
fun PulseCard(
    modifier: Modifier = Modifier,
    pulseColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    
    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
            },
        colors = CardDefaults.cardColors(
            containerColor = pulseColor.copy(alpha = pulseAlpha * 0.1f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            pulseColor.copy(alpha = pulseAlpha * 0.2f),
                            pulseColor.copy(alpha = pulseAlpha * 0.05f)
                        )
                    )
                )
        ) {
            content()
        }
    }
}

/**
 * Tarjeta con efecto de cristal esmerilado
 */
@Composable
fun FrostedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .clickable(enabled = onClick != null) { onClick?.invoke() },
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
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.2f),
                            Color.White.copy(alpha = 0.1f),
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
 * Tarjeta con efecto de neón
 */
@Composable
fun NeonCard(
    modifier: Modifier = Modifier,
    neonColor: Color = Color.Cyan,
    onClick: (() -> Unit)? = null,
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
            .clickable(enabled = onClick != null) { onClick?.invoke() }
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
 * Tarjeta con efecto de metal
 */
@Composable
fun MetalCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .clickable(enabled = onClick != null) { onClick?.invoke() },
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
 * Tarjeta con efecto de papel
 */
@Composable
fun PaperCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .clickable(enabled = onClick != null) { onClick?.invoke() },
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
 * Componente de menú moderno siguiendo las especificaciones de diseño
 * - Sin bordes ni outline
 * - Forma redondeada 20-24dp
 * - Altura mínima 72-84dp, ancho fillMaxWidth
 * - Container con color Material3 y gradiente sutil
 * - Sombra suave (elevation 2-6dp)
 * - Icono + título + subtítulo alineados a la izquierda
 * - Ripple suave y animación de escala al presionar
 */
@Composable
fun ModernMenuItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    iconColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    titleColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    subtitleColor: Color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(150, easing = EaseInOutSine),
        label = "scale_animation"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null // Material3 maneja el ripple automáticamente
            ) { onClick() }
            .semantics {
                contentDescription = title
            },
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        shape = RoundedCornerShape(22.dp), // 20-24dp como especificado
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp // 2-6dp como especificado
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    // Gradiente sutil radial para efecto moderno
                    // Comentario: Este gradiente usa colores del tema Material3
                    // Para variar con el tema, cambiar containerColor por otros colores del scheme
                    Brush.radialGradient(
                        colors = listOf(
                            containerColor,
                            containerColor.copy(alpha = 0.8f),
                            containerColor.copy(alpha = 0.9f)
                        ),
                        radius = 300f
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 20.dp), // 16-20dp como especificado
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono con padding izquierdo de 16dp
                Box(
                    modifier = Modifier
                        .size(28.dp) // 24-28dp como especificado
                        .padding(start = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Contenido de texto
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium, // Tipografía Material3
                        color = titleColor,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall, // Tipografía Material3
                        color = subtitleColor,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
