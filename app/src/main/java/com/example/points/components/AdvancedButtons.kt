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
 * Sistema de botones para POINTS
 * Basado en Material 3 con estados interactivos y animaciones sutiles
 */

/**
 * Botón primario - Acción principal
 * Usa el color primario de la marca con ripple sutil
 */
@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    icon: ImageVector? = null,
    iconPosition: IconPosition = IconPosition.Start,
    size: ButtonSize = ButtonSize.Medium,
    loading: Boolean = false,
    contentDescription: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(Duration.fast),
        label = "button_scale"
    )
    
    val elevation by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.dp.value
            isHovered -> Elevation.sm.value
            else -> Elevation.none.value
        },
        animationSpec = tween(Duration.fast),
        label = "button_elevation"
    )
    
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .scale(scale)
            .height(size.height)
            .clip(PointsCustomShapes.button),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = elevation.dp,
            pressedElevation = 0.dp,
            hoveredElevation = Elevation.sm,
            focusedElevation = Elevation.xs,
            disabledElevation = 0.dp
        ),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(
            horizontal = size.paddingHorizontal,
            vertical = size.paddingVertical
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(IconSize.sm),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            ButtonContent(
                text = text,
                icon = icon,
                iconPosition = iconPosition,
                contentDescription = contentDescription
            )
        }
    }
}

/**
 * Botón secundario - Acción secundaria
 * Usa el color secundario de la marca
 */
@Composable
fun SecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    icon: ImageVector? = null,
    iconPosition: IconPosition = IconPosition.Start,
    size: ButtonSize = ButtonSize.Medium,
    loading: Boolean = false,
    contentDescription: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(Duration.fast),
        label = "button_scale"
    )
    
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .scale(scale)
            .height(size.height)
            .clip(PointsCustomShapes.button),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(
            horizontal = size.paddingHorizontal,
            vertical = size.paddingVertical
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(IconSize.sm),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onSecondary
            )
        } else {
            ButtonContent(
                text = text,
                icon = icon,
                iconPosition = iconPosition,
                contentDescription = contentDescription
            )
        }
    }
}

/**
 * Botón tonal - Acción con fondo tonal
 * Usa el color primario con transparencia
 */
@Composable
fun TonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    icon: ImageVector? = null,
    iconPosition: IconPosition = IconPosition.Start,
    size: ButtonSize = ButtonSize.Medium,
    loading: Boolean = false,
    contentDescription: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(Duration.fast),
        label = "button_scale"
    )
    
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .scale(scale)
            .height(size.height)
            .clip(PointsCustomShapes.button),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(
            horizontal = size.paddingHorizontal,
            vertical = size.paddingVertical
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(IconSize.sm),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        } else {
            ButtonContent(
                text = text,
                icon = icon,
                iconPosition = iconPosition,
                contentDescription = contentDescription
            )
        }
    }
}

/**
 * Botón de texto - Acción sutil
 * Solo texto con ripple sutil
 */
@Composable
fun PointsTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    icon: ImageVector? = null,
    iconPosition: IconPosition = IconPosition.Start,
    size: ButtonSize = ButtonSize.Medium,
    loading: Boolean = false,
    contentDescription: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(Duration.fast),
        label = "button_scale"
    )
    
    TextButton(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .scale(scale)
            .height(size.height)
            .clip(PointsCustomShapes.button),
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(
            horizontal = size.paddingHorizontal,
            vertical = size.paddingVertical
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(IconSize.sm),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            ButtonContent(
                text = text,
                icon = icon,
                iconPosition = iconPosition,
                contentDescription = contentDescription
            )
        }
    }
}

/**
 * Botón con borde - Acción con borde
 * Borde con color primario
 */
@Composable
fun OutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    icon: ImageVector? = null,
    iconPosition: IconPosition = IconPosition.Start,
    size: ButtonSize = ButtonSize.Medium,
    loading: Boolean = false,
    contentDescription: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(Duration.fast),
        label = "button_scale"
    )
    
    OutlinedButton(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .scale(scale)
            .height(size.height)
            .clip(PointsCustomShapes.button),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.dp
        ),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(
            horizontal = size.paddingHorizontal,
            vertical = size.paddingVertical
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(IconSize.sm),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            ButtonContent(
                text = text,
                icon = icon,
                iconPosition = iconPosition,
                contentDescription = contentDescription
            )
        }
    }
}

/**
 * Botón de icono - Acción con solo icono
 * Icono circular con ripple
 */
@Composable
fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector,
    contentDescription: String? = null,
    size: IconButtonSize = IconButtonSize.Medium,
    loading: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(Duration.fast),
        label = "icon_button_scale"
    )
    
    Box(
        modifier = modifier
            .size(size.size)
            .scale(scale)
            .clip(CircleShape)
            .clickable(
                enabled = enabled && !loading,
                onClick = onClick,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = null
            )
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(size.iconSize),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(size.iconSize),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Botón flotante de acción - FAB
 * Botón circular flotante
 */
@Composable
fun FloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector,
    contentDescription: String? = null,
    size: FabSize = FabSize.Medium,
    loading: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(Duration.fast),
        label = "fab_scale"
    )
    
    Box(
        modifier = modifier
            .size(size.size)
            .scale(scale)
            .clip(CircleShape)
            .clickable(
                enabled = enabled && !loading,
                onClick = onClick,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = null
            )
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(size.iconSize),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(size.iconSize),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

/**
 * Contenido del botón con icono y texto
 */
@Composable
private fun ButtonContent(
    text: String,
    icon: ImageVector?,
    iconPosition: IconPosition,
    contentDescription: String?
) {
    when (iconPosition) {
        IconPosition.Start -> {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    modifier = Modifier.size(IconSize.sm)
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
        IconPosition.End -> {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
            if (icon != null) {
                Spacer(modifier = Modifier.width(Spacing.sm))
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    modifier = Modifier.size(IconSize.sm)
                )
            }
        }
        IconPosition.Top -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = contentDescription,
                        modifier = Modifier.size(IconSize.sm)
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        IconPosition.Bottom -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge
                )
                if (icon != null) {
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Icon(
                        imageVector = icon,
                        contentDescription = contentDescription,
                        modifier = Modifier.size(IconSize.sm)
                    )
                }
            }
        }
    }
}

/**
 * Enums para configuración de botones
 */
enum class IconPosition {
    Start, End, Top, Bottom
}

enum class ButtonSize(
    val height: androidx.compose.ui.unit.Dp,
    val paddingHorizontal: androidx.compose.ui.unit.Dp,
    val paddingVertical: androidx.compose.ui.unit.Dp
) {
    Small(32.dp, 12.dp, 4.dp),
    Medium(40.dp, 16.dp, 8.dp),
    Large(48.dp, 20.dp, 12.dp)
}

enum class IconButtonSize(
    val size: androidx.compose.ui.unit.Dp,
    val iconSize: androidx.compose.ui.unit.Dp
) {
    Small(32.dp, 16.dp),
    Medium(40.dp, 20.dp),
    Large(48.dp, 24.dp)
}

enum class FabSize(
    val size: androidx.compose.ui.unit.Dp,
    val iconSize: androidx.compose.ui.unit.Dp
) {
    Small(40.dp, 20.dp),
    Medium(56.dp, 24.dp),
    Large(64.dp, 28.dp)
}

/**
 * Previews de los botones
 */
@Preview(showBackground = true, name = "Primary Button")
@Composable
private fun PrimaryButtonPreview() {
    PointsTheme {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            PrimaryButton(
                onClick = { },
                text = "Botón Primario",
                icon = Icons.Default.Add
            )
            PrimaryButton(
                onClick = { },
                text = "Cargando...",
                loading = true
            )
            PrimaryButton(
                onClick = { },
                text = "Deshabilitado",
                enabled = false
            )
        }
    }
}

@Preview(showBackground = true, name = "Secondary Button")
@Composable
private fun SecondaryButtonPreview() {
    PointsTheme {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            SecondaryButton(
                onClick = { },
                text = "Botón Secundario",
                icon = Icons.Default.Edit
            )
            TonalButton(
                onClick = { },
                text = "Botón Tonal",
                icon = Icons.Default.Info
            )
        }
    }
}

@Preview(showBackground = true, name = "Text & Outlined Buttons")
@Composable
private fun TextOutlinedButtonPreview() {
    PointsTheme {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            PointsTextButton(
                onClick = { },
                text = "Botón de Texto",
                icon = Icons.Default.Link
            )
            OutlinedButton(
                onClick = { },
                text = "Botón con Borde",
                icon = Icons.Default.Download
            )
        }
    }
}

@Preview(showBackground = true, name = "Icon Buttons")
@Composable
private fun IconButtonPreview() {
    PointsTheme {
        Row(
            modifier = Modifier.padding(Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            IconButton(
                onClick = { },
                icon = Icons.Default.Favorite,
                contentDescription = "Favorito"
            )
            FloatingActionButton(
                onClick = { },
                icon = Icons.Default.Add,
                contentDescription = "Agregar"
            )
        }
    }
}