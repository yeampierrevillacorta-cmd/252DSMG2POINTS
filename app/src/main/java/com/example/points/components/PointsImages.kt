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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.points.ui.theme.*

/**
 * Sistema de imágenes para POINTS
 * Imágenes redondeadas, carruseles y placeholders
 */

/**
 * Imagen redondeada - Para avatares y fotos de perfil
 */
@Composable
fun RoundedImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    contentDescription: String? = null,
    placeholder: ImageVector = Icons.Default.Person,
    error: ImageVector = Icons.Default.Error,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            // Aquí se cargaría la imagen real con Coil
            // Por ahora mostramos un placeholder
            Icon(
                imageVector = placeholder,
                contentDescription = contentDescription,
                modifier = Modifier.size(size * 0.6f),
                tint = contentColor
            )
        } else {
            Icon(
                imageVector = placeholder,
                contentDescription = contentDescription,
                modifier = Modifier.size(size * 0.6f),
                tint = contentColor
            )
        }
    }
}

/**
 * Imagen de incidente - Para fotos de incidentes
 */
@Composable
fun IncidentImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    placeholder: ImageVector = Icons.Default.Image,
    error: ImageVector = Icons.Default.BrokenImage,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(PointsCustomShapes.image)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            // Aquí se cargaría la imagen real con Coil
            // Por ahora mostramos un placeholder
            Icon(
                imageVector = placeholder,
                contentDescription = contentDescription,
                modifier = Modifier.size(48.dp),
                tint = contentColor
            )
        } else {
            Icon(
                imageVector = placeholder,
                contentDescription = contentDescription,
                modifier = Modifier.size(48.dp),
                tint = contentColor
            )
        }
    }
}

/**
 * Carrusel de imágenes - Para múltiples fotos
 */
@Composable
fun ImageCarousel(
    images: List<String>,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    showIndicators: Boolean = true,
    autoPlay: Boolean = false,
    autoPlayInterval: Long = 3000L
) {
    var currentIndex by remember { mutableStateOf(0) }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(PointsCustomShapes.carousel)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (images.isNotEmpty()) {
            // Aquí se implementaría el carrusel real
            // Por ahora mostramos la imagen actual
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = contentDescription,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (showIndicators && images.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(images.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == currentIndex) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                )
                        )
                    }
                }
            }
        } else {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = contentDescription,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Imagen de mapa - Para ubicaciones
 */
@Composable
fun MapImage(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    placeholder: ImageVector = Icons.Default.Map,
    error: ImageVector = Icons.Default.Error,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(PointsCustomShapes.mapCard)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        // Aquí se cargaría el mapa real
        // Por ahora mostramos un placeholder
        Icon(
            imageVector = placeholder,
            contentDescription = contentDescription,
            modifier = Modifier.size(48.dp),
            tint = contentColor
        )
    }
}

/**
 * Avatar con badge - Para usuarios con notificaciones
 */
@Composable
fun AvatarWithBadge(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    contentDescription: String? = null,
    badgeCount: Int? = null,
    showBadge: Boolean = badgeCount != null && badgeCount > 0,
    badgeColor: Color = MaterialTheme.colorScheme.error,
    badgeContentColor: Color = MaterialTheme.colorScheme.onError
) {
    Box(
        modifier = modifier
    ) {
        RoundedImage(
            imageUrl = imageUrl,
            size = size,
            contentDescription = contentDescription
        )
        
        if (showBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(badgeColor),
                contentAlignment = Alignment.Center
            ) {
                if (badgeCount != null && badgeCount > 0) {
                    Text(
                        text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeContentColor
                    )
                }
            }
        }
    }
}

/**
 * Previews de los componentes de imagen
 */
@Preview(showBackground = true, name = "Rounded Images")
@Composable
private fun RoundedImagePreview() {
    PointsTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RoundedImage(
                imageUrl = null,
                size = 40.dp,
                contentDescription = "Avatar"
            )
            
            RoundedImage(
                imageUrl = null,
                size = 60.dp,
                contentDescription = "Avatar grande"
            )
            
            AvatarWithBadge(
                imageUrl = null,
                size = 40.dp,
                contentDescription = "Avatar con badge",
                badgeCount = 5
            )
        }
    }
}

@Preview(showBackground = true, name = "Incident Images")
@Composable
private fun IncidentImagePreview() {
    PointsTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IncidentImage(
                imageUrl = null,
                contentDescription = "Imagen de incidente"
            )
            
            MapImage(
                latitude = 0.0,
                longitude = 0.0,
                contentDescription = "Mapa de ubicación"
            )
        }
    }
}

@Preview(showBackground = true, name = "Image Carousel")
@Composable
private fun ImageCarouselPreview() {
    PointsTheme {
        ImageCarousel(
            images = listOf("image1", "image2", "image3"),
            contentDescription = "Carrusel de imágenes"
        )
    }
}