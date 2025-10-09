package com.example.points.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.points.utils.ImageLoaderConfig

/**
 * Cargador de imágenes optimizado para reducir logs de HWUI
 */
@Composable
fun OptimizedAsyncImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    placeholder: ImageVector = Icons.Default.Image,
    error: ImageVector = Icons.Default.BrokenImage,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val imageLoader = remember { ImageLoaderConfig.createOptimizedImageLoader(context) }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null && imageUrl.isNotEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(false) // Deshabilitar crossfade para reducir logs
                    .build(),
                imageLoader = imageLoader,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
                onError = {
                    // En caso de error, mostrar icono de error
                }
            )
        } else {
            // Mostrar placeholder si no hay URL
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = placeholder,
                    contentDescription = contentDescription,
                    modifier = Modifier.size(48.dp),
                    tint = contentColor
                )
            }
        }
    }
}

/**
 * Imagen redondeada optimizada
 */
@Composable
fun OptimizedRoundedImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    contentDescription: String? = null,
    placeholder: ImageVector = Icons.Default.Person,
    error: ImageVector = Icons.Default.Error,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val context = LocalContext.current
    val imageLoader = remember { ImageLoaderConfig.createOptimizedImageLoader(context) }
    
    Box(
        modifier = modifier
            .size(size)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null && imageUrl.isNotEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(false)
                    .build(),
                imageLoader = imageLoader,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onError = {
                    // En caso de error, mostrar icono de error
                }
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
 * Imagen de incidente optimizada
 */
@Composable
fun OptimizedIncidentImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    placeholder: ImageVector = Icons.Default.Image,
    error: ImageVector = Icons.Default.BrokenImage,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val context = LocalContext.current
    val imageLoader = remember { ImageLoaderConfig.createOptimizedImageLoader(context) }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null && imageUrl.isNotEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(false)
                    .build(),
                imageLoader = imageLoader,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onError = {
                    // En caso de error, mostrar icono de error
                }
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
