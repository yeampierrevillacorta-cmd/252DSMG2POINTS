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
 * Sistema de feedback para POINTS
 * Snackbars, banners y diálogos con estilo consistente
 */

/**
 * Snackbar personalizada - Para mensajes breves
 */
@Composable
fun PointsSnackbar(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    type: SnackbarType = SnackbarType.Info,
    duration: SnackbarDuration = SnackbarDuration.Short
) {
    val (backgroundColor, contentColor, icon) = when (type) {
        SnackbarType.Info -> Triple(
            MaterialTheme.colorScheme.inverseSurface,
            MaterialTheme.colorScheme.inverseOnSurface,
            Icons.Default.Info
        )
        SnackbarType.Success -> Triple(
            PointsSuccess,
            MaterialTheme.colorScheme.onPrimary,
            Icons.Default.CheckCircle
        )
        SnackbarType.Warning -> Triple(
            PointsAccent,
            MaterialTheme.colorScheme.onPrimary,
            Icons.Default.Warning
        )
        SnackbarType.Error -> Triple(
            PointsError,
            MaterialTheme.colorScheme.onPrimary,
            Icons.Default.Error
        )
    }
    
    Card(
        modifier = modifier
            .clip(PointsCustomShapes.snackbar)
            .background(backgroundColor),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = contentColor
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                modifier = Modifier.weight(1f)
            )
            
            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = onAction,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = contentColor
                    )
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

/**
 * Banner de información - Para mensajes persistentes
 */
@Composable
fun InfoBanner(
    title: String,
    message: String? = null,
    modifier: Modifier = Modifier,
    type: BannerType = BannerType.Info,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null
) {
    val (backgroundColor, contentColor, icon) = when (type) {
        BannerType.Info -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            Icons.Default.Info
        )
        BannerType.Success -> Triple(
            PointsSuccess.copy(alpha = 0.12f),
            PointsSuccess,
            Icons.Default.CheckCircle
        )
        BannerType.Warning -> Triple(
            PointsAccent.copy(alpha = 0.12f),
            PointsAccent,
            Icons.Default.Warning
        )
        BannerType.Error -> Triple(
            PointsError.copy(alpha = 0.12f),
            PointsError,
            Icons.Default.Error
        )
        BannerType.Offline -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Default.WifiOff
        )
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(PointsCustomShapes.banner)
            .background(backgroundColor),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = contentColor
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor
                )
                
                if (message != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (actionLabel != null && onAction != null) {
                    TextButton(
                        onClick = onAction,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = contentColor
                        )
                    ) {
                        Text(
                            text = actionLabel,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                
                if (onDismiss != null) {
                    IconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = contentColor
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Diálogo de confirmación - Para confirmar acciones
 */
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = "Confirmar",
    dismissText: String = "Cancelar",
    type: DialogType = DialogType.Info,
    destructive: Boolean = false
) {
    val (icon, iconColor) = when (type) {
        DialogType.Info -> Pair(Icons.Default.Info, MaterialTheme.colorScheme.primary)
        DialogType.Warning -> Pair(Icons.Default.Warning, PointsAccent)
        DialogType.Error -> Pair(Icons.Default.Error, PointsError)
        DialogType.Success -> Pair(Icons.Default.CheckCircle, PointsSuccess)
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = iconColor
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (destructive) MaterialTheme.colorScheme.error
                                   else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = confirmText,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = dismissText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = PointsCustomShapes.dialog
    )
}

/**
 * Enums para configuración de feedback
 */
enum class SnackbarType {
    Info, Success, Warning, Error
}

enum class BannerType {
    Info, Success, Warning, Error, Offline
}

enum class DialogType {
    Info, Warning, Error, Success
}

/**
 * Función de conveniencia para mostrar feedback con mensaje
 */
@Composable
fun PointsFeedback(
    message: String,
    type: String = "info",
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val bannerType = when (type.lowercase()) {
        "error" -> BannerType.Error
        "success" -> BannerType.Success
        "warning" -> BannerType.Warning
        "empty" -> BannerType.Info
        else -> BannerType.Info
    }
    
    InfoBanner(
        title = message,
        modifier = modifier,
        type = bannerType,
        actionLabel = if (onRetry != null) "Reintentar" else null,
        onAction = onRetry
    )
}

/**
 * Previews de los componentes de feedback
 */
@Preview(showBackground = true, name = "Snackbars")
@Composable
private fun SnackbarPreview() {
    PointsTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PointsSnackbar(
                message = "Operación completada exitosamente",
                type = SnackbarType.Success,
                actionLabel = "Deshacer",
                onAction = { }
            )
            
            PointsSnackbar(
                message = "Error al guardar los datos",
                type = SnackbarType.Error,
                actionLabel = "Reintentar",
                onAction = { }
            )
        }
    }
}

@Preview(showBackground = true, name = "Banners")
@Composable
private fun BannerPreview() {
    PointsTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoBanner(
                title = "Sin conexión",
                message = "Verifica tu conexión a internet",
                type = BannerType.Offline,
                actionLabel = "Reintentar",
                onAction = { },
                onDismiss = { }
            )
            
            InfoBanner(
                title = "Información importante",
                message = "Nueva actualización disponible",
                type = BannerType.Info,
                actionLabel = "Actualizar",
                onAction = { }
            )
        }
    }
}