package com.example.points.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.points.ui.theme.*

/**
 * Sistema de chips para POINTS
 * Basado en Material 3 con estados interactivos y contadores
 */

/**
 * Chip de filtro - Para seleccionar opciones de filtro
 */
@Composable
fun FilterChip(
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    count: Int? = null,
    contentDescription: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    val backgroundColor = when {
        selected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    
    val contentColor = when {
        selected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    val borderColor = when {
        selected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }
    
    Box(
        modifier = modifier
            .clip(PointsCustomShapes.chip)
            .background(backgroundColor)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = borderColor,
                shape = PointsCustomShapes.chip
            )
            .clickable(
                enabled = enabled,
                onClick = onClick,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = null
            )
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = contentDescription,
                    modifier = Modifier.size(16.dp),
                    tint = contentColor
                )
            }
            
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
            )
            
            if (count != null && count > 0) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (count > 99) "99+" else count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            if (trailingIcon != null) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = contentDescription,
                    modifier = Modifier.size(16.dp),
                    tint = contentColor
                )
            }
        }
    }
}

/**
 * Chip de estado - Para mostrar estados de incidentes
 */
@Composable
fun StatusChip(
    status: IncidentStatus,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true
) {
    val (backgroundColor, contentColor, icon) = when (status) {
        IncidentStatus.PENDING -> Triple(
            StatusPending.copy(alpha = 0.12f),
            StatusPending,
            Icons.Default.Schedule
        )
        IncidentStatus.IN_REVIEW -> Triple(
            StatusInReview.copy(alpha = 0.12f),
            StatusInReview,
            Icons.Default.Visibility
        )
        IncidentStatus.CONFIRMED -> Triple(
            StatusConfirmed.copy(alpha = 0.12f),
            StatusConfirmed,
            Icons.Default.CheckCircle
        )
        IncidentStatus.REJECTED -> Triple(
            StatusRejected.copy(alpha = 0.12f),
            StatusRejected,
            Icons.Default.Cancel
        )
        IncidentStatus.RESOLVED -> Triple(
            StatusResolved.copy(alpha = 0.12f),
            StatusResolved,
            Icons.Default.Done
        )
    }
    
    Box(
        modifier = modifier
            .clip(PointsCustomShapes.chip)
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (showIcon) {
                Icon(
                    imageVector = icon,
                    contentDescription = status.displayName,
                    modifier = Modifier.size(16.dp),
                    tint = contentColor
                )
            }
            
            Text(
                text = status.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
        }
    }
}

/**
 * Chip de prioridad - Para mostrar niveles de prioridad
 */
@Composable
fun PriorityChip(
    priority: IncidentPriority,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true
) {
    val (backgroundColor, contentColor, icon) = when (priority) {
        IncidentPriority.LOW -> Triple(
            PriorityLow.copy(alpha = 0.12f),
            PriorityLow,
            Icons.Default.KeyboardArrowDown
        )
        IncidentPriority.MEDIUM -> Triple(
            PriorityMedium.copy(alpha = 0.12f),
            PriorityMedium,
            Icons.Default.Remove
        )
        IncidentPriority.HIGH -> Triple(
            PriorityHigh.copy(alpha = 0.12f),
            PriorityHigh,
            Icons.Default.KeyboardArrowUp
        )
        IncidentPriority.CRITICAL -> Triple(
            PriorityCritical.copy(alpha = 0.12f),
            PriorityCritical,
            Icons.Default.PriorityHigh
        )
    }
    
    Box(
        modifier = modifier
            .clip(PointsCustomShapes.chip)
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (showIcon) {
                Icon(
                    imageVector = icon,
                    contentDescription = priority.displayName,
                    modifier = Modifier.size(16.dp),
                    tint = contentColor
                )
            }
            
            Text(
                text = priority.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
        }
    }
}

/**
 * Chip de categoría - Para mostrar categorías de incidentes
 */
@Composable
fun CategoryChip(
    category: IncidentCategory,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true
) {
    val (backgroundColor, contentColor, icon) = when (category) {
        IncidentCategory.INFRASTRUCTURE -> Triple(
            CategoryInfrastructure.copy(alpha = 0.12f),
            CategoryInfrastructure,
            Icons.Default.Build
        )
        IncidentCategory.ENVIRONMENT -> Triple(
            CategoryEnvironment.copy(alpha = 0.12f),
            CategoryEnvironment,
            Icons.Default.Eco
        )
        IncidentCategory.SAFETY -> Triple(
            CategorySafety.copy(alpha = 0.12f),
            CategorySafety,
            Icons.Default.Security
        )
        IncidentCategory.TRANSPORT -> Triple(
            CategoryTransport.copy(alpha = 0.12f),
            CategoryTransport,
            Icons.Default.DirectionsCar
        )
        IncidentCategory.OTHER -> Triple(
            CategoryOther.copy(alpha = 0.12f),
            CategoryOther,
            Icons.Default.MoreHoriz
        )
    }
    
    Box(
        modifier = modifier
            .clip(PointsCustomShapes.chip)
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (showIcon) {
                Icon(
                    imageVector = icon,
                    contentDescription = category.displayName,
                    modifier = Modifier.size(16.dp),
                    tint = contentColor
                )
            }
            
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
            )
        }
    }
}

/**
 * Enums para configuración de chips
 */
enum class IncidentStatus(val displayName: String) {
    PENDING("Pendiente"),
    IN_REVIEW("En Revisión"),
    CONFIRMED("Confirmado"),
    REJECTED("Rechazado"),
    RESOLVED("Resuelto")
}

enum class IncidentPriority(val displayName: String) {
    LOW("Baja"),
    MEDIUM("Media"),
    HIGH("Alta"),
    CRITICAL("Crítica")
}

enum class IncidentCategory(val displayName: String) {
    INFRASTRUCTURE("Infraestructura"),
    ENVIRONMENT("Medio Ambiente"),
    SAFETY("Seguridad"),
    TRANSPORT("Transporte"),
    OTHER("Otros")
}

/**
 * Previews de los chips
 */
@Preview(showBackground = true, name = "Filter Chips")
@Composable
private fun FilterChipPreview() {
    PointsTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    onClick = { },
                    label = "Todos",
                    selected = true
                )
                FilterChip(
                    onClick = { },
                    label = "Pendientes",
                    count = 5
                )
                FilterChip(
                    onClick = { },
                    label = "Resueltos",
                    count = 12
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Status Chips")
@Composable
private fun StatusChipPreview() {
    PointsTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusChip(IncidentStatus.PENDING)
            StatusChip(IncidentStatus.IN_REVIEW)
            StatusChip(IncidentStatus.CONFIRMED)
            StatusChip(IncidentStatus.RESOLVED)
        }
    }
}