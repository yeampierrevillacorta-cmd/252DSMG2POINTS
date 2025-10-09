package com.example.points.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.points.ui.theme.*

/**
 * Sistema de tarjetas para POINTS
 * Basado en Material 3 con estados interactivos y variantes específicas
 */

/**
 * Tarjeta de superficie - Tarjeta básica con elevación
 * Para contenido general con sombra sutil
 */
@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    elevation: androidx.compose.ui.unit.Dp = 3.dp,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(Duration.fast),
        label = "card_scale"
    )
    
    val animatedElevation by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.dp.value
            isHovered -> (elevation + 1.dp).value
            isFocused -> elevation.value
            else -> elevation.value
        },
        animationSpec = tween(Duration.fast),
        label = "card_elevation"
    )
    
    Card(
        modifier = modifier
            .scale(scale)
            .clickable(
                enabled = enabled && onClick != null,
                onClick = { onClick?.invoke() },
                role = if (onClick != null) Role.Button else null,
                interactionSource = interactionSource,
                indication = null
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = PointsCustomShapes.card,
        elevation = CardDefaults.cardElevation(
            defaultElevation = animatedElevation.dp,
            pressedElevation = 0.dp,
            hoveredElevation = (elevation + Elevation.xs),
            focusedElevation = elevation
        )
    ) {
        content()
    }
}

/**
 * Tarjeta de información - Para mostrar información importante
 * Con borde sutil y fondo tonal
 */
@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    title: String? = null,
    subtitle: String? = null,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(Duration.fast),
        label = "card_scale"
    )
    
    Card(
        modifier = modifier
            .scale(scale)
            .clickable(
                enabled = enabled && onClick != null,
                onClick = { onClick?.invoke() },
                role = if (onClick != null) Role.Button else null,
                interactionSource = interactionSource,
                indication = null
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = PointsCustomShapes.card,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CardSize.padding)
        ) {
            // Header con icono y título
            if (icon != null || title != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            modifier = Modifier.size(IconSize.md),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    if (title != null) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                
                Spacer(modifier = Modifier.height(Spacing.md))
            }
            
            // Contenido
            content()
        }
    }
}

/**
 * Tarjeta de advertencia - Para alertas y advertencias
 * Con colores de advertencia y borde destacado
 */
@Composable
fun WarningCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    icon: ImageVector = Icons.Default.Warning,
    title: String? = null,
    subtitle: String? = null,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(Duration.fast),
        label = "card_scale"
    )
    
    Card(
        modifier = modifier
            .scale(scale)
            .clickable(
                enabled = enabled && onClick != null,
                onClick = { onClick?.invoke() },
                role = if (onClick != null) Role.Button else null,
                interactionSource = interactionSource,
                indication = null
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        shape = PointsCustomShapes.card,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CardSize.padding)
        ) {
            // Header con icono y título
            if (title != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(IconSize.md),
                        tint = MaterialTheme.colorScheme.error
                    )
                    
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                    )
                }
                
                Spacer(modifier = Modifier.height(Spacing.md))
            }
            
            // Contenido
            content()
        }
    }
}

/**
 * Tarjeta de estadísticas - Para mostrar métricas y números
 * Con diseño optimizado para datos numéricos
 */
@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    value: String,
    label: String,
    icon: ImageVector? = null,
    trend: StatTrend? = null,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(Duration.fast),
        label = "card_scale"
    )
    
    Card(
        modifier = modifier
            .scale(scale)
            .clickable(
                enabled = enabled && onClick != null,
                onClick = { onClick?.invoke() },
                role = if (onClick != null) Role.Button else null,
                interactionSource = interactionSource,
                indication = null
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = PointsCustomShapes.statCard,
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CardSize.padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(IconSize.lg),
                    tint = color
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
            }
            
            // Valor
            Text(
                text = value,
                style = PointsTextStyles.statValue,
                color = color
            )
            
            // Etiqueta
            Text(
                text = label,
                style = PointsTextStyles.statLabel,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            // Tendencia
            if (trend != null) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Icon(
                        imageVector = trend.icon,
                        contentDescription = trend.description,
                        modifier = Modifier.size(IconSize.xs),
                        tint = trend.color
                    )
                    Text(
                        text = trend.text,
                        style = PointsTextStyles.statLabel,
                        color = trend.color
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta de incidente - Para mostrar información de incidentes
 * Con diseño específico para el contexto de POINTS
 */
@Composable
fun IncidentCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    title: String,
    description: String,
    location: String,
    status: IncidentStatus,
    priority: IncidentPriority,
    category: IncidentCategory,
    timestamp: String,
    imageUrl: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(Duration.fast),
        label = "card_scale"
    )
    
    Card(
        modifier = modifier
            .scale(scale)
            .clickable(
                enabled = enabled && onClick != null,
                onClick = { onClick?.invoke() },
                role = if (onClick != null) Role.Button else null,
                interactionSource = interactionSource,
                indication = null
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = PointsCustomShapes.incidentCard,
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CardSize.padding)
        ) {
            // Header con estado y prioridad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(status = status)
                PriorityChip(priority = priority)
            }
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            // Título
            Text(
                text = title,
                style = PointsTextStyles.incidentTitle,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(Spacing.xs))
            
            // Descripción
            Text(
                text = description,
                style = PointsTextStyles.incidentDescription,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(Spacing.sm))
            
            // Categoría
            CategoryChip(category = category)
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            // Footer con ubicación y timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Ubicación",
                        modifier = Modifier.size(IconSize.sm),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = location,
                        style = PointsTextStyles.incidentMeta,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Text(
                    text = timestamp,
                    style = PointsTextStyles.incidentMeta,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * Tarjeta de menú - Para opciones de menú
 * Con diseño optimizado para navegación
 */
@Composable
fun MenuCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true,
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    trailingIcon: ImageVector = Icons.Default.ChevronRight,
    badge: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(Duration.fast),
        label = "card_scale"
    )
    
    Card(
        modifier = modifier
            .scale(scale)
            .clickable(
                enabled = enabled,
                onClick = onClick,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = null
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = PointsCustomShapes.card,
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CardSize.padding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono principal
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(IconSize.lg),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(Spacing.md))
            }
            
            // Contenido principal
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Badge
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badge,
                        style = PointsTextStyles.chipCount,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.sm))
            }
            
            // Icono trailing
            Icon(
                imageVector = trailingIcon,
                contentDescription = "Ir a $title",
                modifier = Modifier.size(IconSize.sm),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Enums para configuración de tarjetas
 */
enum class StatTrend(
    val icon: ImageVector,
    val color: Color,
    val text: String,
    val description: String
) {
    UP(
        icon = Icons.Default.TrendingUp,
        color = PointsSuccess,
        text = "+12%",
        description = "Aumento"
    ),
    DOWN(
        icon = Icons.Default.TrendingDown,
        color = PointsError,
        text = "-5%",
        description = "Disminución"
    ),
    STABLE(
        icon = Icons.Default.TrendingFlat,
        color = PointsSubtle,
        text = "0%",
        description = "Estable"
    )
}

/**
 * Previews de las tarjetas
 */
@Preview(showBackground = true, name = "Surface Card")
@Composable
private fun SurfaceCardPreview() {
    PointsTheme {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            SurfaceCard(
                onClick = { },
                elevation = Elevation.sm
            ) {
                Column(
                    modifier = Modifier.padding(CardSize.padding)
                ) {
                    Text(
                        text = "Tarjeta de Superficie",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text(
                        text = "Esta es una tarjeta básica con elevación sutil.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Info Card")
@Composable
private fun InfoCardPreview() {
    PointsTheme {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            InfoCard(
                onClick = { },
                icon = Icons.Default.Info,
                title = "Información Importante",
                subtitle = "Esta es una tarjeta de información"
            ) {
                Text(
                    text = "Contenido adicional de la tarjeta de información.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Warning Card")
@Composable
private fun WarningCardPreview() {
    PointsTheme {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            WarningCard(
                onClick = { },
                title = "Advertencia",
                subtitle = "Esta es una tarjeta de advertencia"
            ) {
                Text(
                    text = "Contenido de la advertencia que requiere atención.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Stat Card")
@Composable
private fun StatCardPreview() {
    PointsTheme {
        Row(
            modifier = Modifier.padding(Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            StatCard(
                onClick = { },
                value = "1,234",
                label = "Incidentes",
                icon = Icons.Default.Report,
                trend = StatTrend.UP
            )
            StatCard(
                onClick = { },
                value = "89%",
                label = "Resueltos",
                icon = Icons.Default.CheckCircle,
                trend = StatTrend.STABLE
            )
        }
    }
}

@Preview(showBackground = true, name = "Incident Card")
@Composable
private fun IncidentCardPreview() {
    PointsTheme {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            IncidentCard(
                onClick = { },
                title = "Bache en Av. Principal",
                description = "Bache profundo en la intersección con la calle secundaria, representa riesgo para vehículos.",
                location = "Av. Principal 123",
                status = IncidentStatus.PENDING,
                priority = IncidentPriority.HIGH,
                category = IncidentCategory.INFRASTRUCTURE,
                timestamp = "Hace 2 horas"
            )
        }
    }
}

@Preview(showBackground = true, name = "Menu Card")
@Composable
private fun MenuCardPreview() {
    PointsTheme {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            MenuCard(
                onClick = { },
                title = "Mis Reportes",
                subtitle = "Ver todos mis reportes",
                icon = Icons.Default.List,
                badge = "5"
            )
            MenuCard(
                onClick = { },
                title = "Configuración",
                subtitle = "Ajustes de la aplicación",
                icon = Icons.Default.Settings
            )
        }
    }
}