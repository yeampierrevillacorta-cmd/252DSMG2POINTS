package com.example.points.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import com.example.points.R
import com.example.points.models.EstadoIncidente
import com.example.points.models.TipoIncidente
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

object MarkerUtils {
    
    fun getIconForIncidentType(tipo: String): ImageVector {
        return when (tipo) {
            TipoIncidente.INSEGURIDAD.displayName -> Icons.Default.Warning
            TipoIncidente.ACCIDENTE_TRANSITO.displayName -> Icons.Default.DirectionsCar
            TipoIncidente.INCENDIO.displayName -> Icons.Default.LocalFireDepartment
            TipoIncidente.INUNDACION.displayName -> Icons.Default.Water
            TipoIncidente.VANDALISMO.displayName -> Icons.Default.ReportProblem
            TipoIncidente.SERVICIO_PUBLICO.displayName -> Icons.Default.Build
            else -> Icons.Default.Place
        }
    }
    
    fun getColorForIncidentType(tipo: String): Color {
        return when (tipo) {
            TipoIncidente.INSEGURIDAD.displayName -> Color(0xFFE53E3E) // Rojo
            TipoIncidente.ACCIDENTE_TRANSITO.displayName -> Color(0xFFFF8C00) // Naranja
            TipoIncidente.INCENDIO.displayName -> Color(0xFFDC143C) // Rojo oscuro
            TipoIncidente.INUNDACION.displayName -> Color(0xFF1E90FF) // Azul
            TipoIncidente.VANDALISMO.displayName -> Color(0xFF9932CC) // Púrpura
            TipoIncidente.SERVICIO_PUBLICO.displayName -> Color(0xFF228B22) // Verde
            else -> Color(0xFF666666) // Gris
        }
    }
    
    fun getColorForIncidentStatus(estado: String): Color {
        return when (estado) {
            EstadoIncidente.PENDIENTE.displayName -> Color(0xFFFFA500) // Naranja
            EstadoIncidente.EN_REVISION.displayName -> Color(0xFF4169E1) // Azul
            EstadoIncidente.CONFIRMADO.displayName -> Color(0xFFDC143C) // Rojo
            EstadoIncidente.RECHAZADO.displayName -> Color(0xFF808080) // Gris
            EstadoIncidente.RESUELTO.displayName -> Color(0xFF32CD32) // Verde
            else -> Color(0xFF666666)
        }
    }
    
    fun createCustomMarkerIcon(
        context: Context,
        tipo: String,
        estado: String = EstadoIncidente.PENDIENTE.displayName
    ): BitmapDescriptor {
        val size = 120
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Color basado en el tipo de incidente
        val mainColor = getColorForIncidentType(tipo).toArgb()
        val statusColor = getColorForIncidentStatus(estado).toArgb()
        
        val paint = Paint().apply {
            isAntiAlias = true
        }
        
        // Dibujar círculo principal
        paint.color = mainColor
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4, paint)
        
        // Dibujar borde del estado
        paint.color = statusColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 8f
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4, paint)
        
        // Dibujar círculo blanco interior para el icono
        paint.color = android.graphics.Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawCircle(size / 2f, size / 2f, size / 3f, paint)
        
        // Dibujar texto del icono (primera letra del tipo)
        paint.color = mainColor
        paint.textSize = 32f
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true
        
        val text = getIconTextForType(tipo)
        val textBounds = Rect()
        paint.getTextBounds(text, 0, text.length, textBounds)
        
        canvas.drawText(
            text,
            size / 2f,
            size / 2f + textBounds.height() / 2f,
            paint
        )
        
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
    
    private fun getIconTextForType(tipo: String): String {
        return when (tipo) {
            TipoIncidente.INSEGURIDAD.displayName -> "⚠"
            TipoIncidente.ACCIDENTE_TRANSITO.displayName -> "🚗"
            TipoIncidente.INCENDIO.displayName -> "🔥"
            TipoIncidente.INUNDACION.displayName -> "💧"
            TipoIncidente.VANDALISMO.displayName -> "⚡"
            TipoIncidente.SERVICIO_PUBLICO.displayName -> "🔧"
            else -> "📍"
        }
    }
    
    fun getIncidentTypeDisplayName(tipo: String): String {
        return TipoIncidente.values().find { it.displayName == tipo }?.displayName ?: tipo
    }
    
    fun getIncidentStatusDisplayName(estado: String): String {
        return EstadoIncidente.values().find { it.displayName == estado }?.displayName ?: estado
    }
}
