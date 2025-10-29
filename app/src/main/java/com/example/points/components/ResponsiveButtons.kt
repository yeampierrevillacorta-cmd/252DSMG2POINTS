package com.example.points.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.points.constants.AppSpacing

/**
 * Configuración para un botón responsive
 */
data class ResponsiveButtonConfig(
    val text: String,
    val onClick: () -> Unit,
    val icon: ImageVector? = null,
    val enabled: Boolean = true,
    val isOutlined: Boolean = false,
    val buttonColors: ButtonColors? = null,
    val weight: Float = 1f
)

/**
 * Colores personalizados para un botón
 */
data class ButtonColors(
    val containerColor: androidx.compose.ui.graphics.Color? = null,
    val contentColor: androidx.compose.ui.graphics.Color? = null
)

/**
 * Componente de fila de botones responsive que se ajusta al tamaño de la pantalla
 * Los botones se distribuyen horizontalmente y el texto se ajusta automáticamente
 * 
 * @param buttons Lista de configuraciones de botones
 * @param modifier Modificador para el contenedor
 * @param spacing Espacio entre botones (default: AppSpacing.BUTTON_SPACING)
 * @param minTextSize Tamaño mínimo del texto (default: 10.sp)
 * @param maxTextSize Tamaño máximo del texto (default: 14.sp)
 * @param iconSize Tamaño del icono (default: 18.dp)
 */
@Composable
fun ResponsiveButtonRow(
    buttons: List<ResponsiveButtonConfig>,
    modifier: Modifier = Modifier,
    spacing: Dp = AppSpacing.BUTTON_SPACING,
    minTextSize: androidx.compose.ui.unit.TextUnit = 10.sp,
    maxTextSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    iconSize: Dp = 18.dp
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    // Calcular el ancho disponible por botón
    val screenWidthDp = configuration.screenWidthDp.dp
    val paddingDp = AppSpacing.STANDARD * 2 // Padding total (izquierda + derecha)
    val availableWidth = screenWidthDp - paddingDp
    val totalSpacing = spacing * (buttons.size - 1)
    val totalWeight = buttons.sumOf { it.weight.toDouble() }
    val avgButtonWidth = (availableWidth - totalSpacing) / totalWeight.toFloat()
    
    // Calcular tamaño de texto basado en el ancho promedio disponible
    val buttonWidthPx = with(density) { avgButtonWidth.toPx() }
    val calculatedTextSize = remember(buttonWidthPx, minTextSize, maxTextSize) {
        val minSize = minTextSize.value
        val maxSize = maxTextSize.value
        val range = maxSize - minSize
        
        val textSizeValue = when {
            buttonWidthPx < 120f -> minSize // Pantalla muy pequeña
            buttonWidthPx < 200f -> minSize + range * 0.3f // Pantalla pequeña
            buttonWidthPx < 300f -> minSize + range * 0.7f // Pantalla mediana
            else -> maxSize // Pantalla grande
        }
        
        androidx.compose.ui.unit.TextUnit(
            textSizeValue,
            androidx.compose.ui.unit.TextUnitType.Sp
        )
    }
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        buttons.forEach { buttonConfig ->
            ResponsiveButton(
                config = buttonConfig,
                modifier = Modifier.weight(buttonConfig.weight),
                textSize = calculatedTextSize,
                iconSize = iconSize
            )
        }
    }
}

/**
 * Botón individual responsive
 */
@Composable
private fun ResponsiveButton(
    config: ResponsiveButtonConfig,
    modifier: Modifier = Modifier,
    textSize: androidx.compose.ui.unit.TextUnit,
    iconSize: Dp
) {
    val buttonContent = @Composable {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (config.icon != null) {
                Icon(
                    imageVector = config.icon,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize)
                )
                Spacer(modifier = Modifier.width(AppSpacing.SMALL))
            }
            
            Text(
                text = config.text,
                fontSize = textSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
    
    if (config.isOutlined) {
        OutlinedButton(
            onClick = config.onClick,
            enabled = config.enabled,
            modifier = modifier,
            colors = config.buttonColors?.let {
                ButtonDefaults.outlinedButtonColors(
                    containerColor = it.containerColor ?: MaterialTheme.colorScheme.surface,
                    contentColor = it.contentColor ?: MaterialTheme.colorScheme.onSurface
                )
            } ?: ButtonDefaults.outlinedButtonColors()
        ) {
            buttonContent()
        }
    } else {
        Button(
            onClick = config.onClick,
            enabled = config.enabled,
            modifier = modifier,
            colors = config.buttonColors?.let {
                ButtonDefaults.buttonColors(
                    containerColor = it.containerColor ?: MaterialTheme.colorScheme.primary,
                    contentColor = it.contentColor ?: MaterialTheme.colorScheme.onPrimary
                )
            } ?: ButtonDefaults.buttonColors()
        ) {
            buttonContent()
        }
    }
}
