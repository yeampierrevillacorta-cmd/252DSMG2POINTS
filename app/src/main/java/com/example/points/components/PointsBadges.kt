package com.example.points.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.points.ui.theme.*

/**
 * Sistema de badges para POINTS
 * Para mostrar estados, contadores y notificaciones
 */

/**
 * Badge de estado - Para mostrar estados de incidentes
 */
@Composable
fun StatusBadge(
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
            .clip(PointsCustomShapes.badge)
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
 * Badge de contador - Para mostrar números
 */
@Composable
fun CountBadge(
    count: Int,
    modifier: Modifier = Modifier,
    maxCount: Int = 99,
    color: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    val displayText = if (count > maxCount) "$maxCount+" else count.toString()
    
    Box(
        modifier = modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
    }
}

/**
 * Badge de notificación - Para alertas
 */
@Composable
fun NotificationBadge(
    modifier: Modifier = Modifier,
    hasNotification: Boolean = true,
    count: Int? = null
) {
    if (hasNotification) {
        Box(
            modifier = modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.error),
            contentAlignment = Alignment.Center
        ) {
            if (count != null && count > 0) {
                Text(
                    text = if (count > 99) "99+" else count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onError
                )
            }
        }
    }
}

/**
 * Previews de los badges
 */
@Preview(showBackground = true, name = "Status Badges")
@Composable
private fun StatusBadgePreview() {
    PointsTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusBadge(IncidentStatus.PENDING)
                StatusBadge(IncidentStatus.IN_REVIEW)
                StatusBadge(IncidentStatus.CONFIRMED)
                StatusBadge(IncidentStatus.RESOLVED)
            }
        }
    }
}

@Preview(showBackground = true, name = "Count Badges")
@Composable
private fun CountBadgePreview() {
    PointsTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CountBadge(count = 5)
            CountBadge(count = 12)
            CountBadge(count = 99)
            CountBadge(count = 150, maxCount = 99)
        }
    }
}