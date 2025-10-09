package com.example.points.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.example.points.models.CategoriaPOI

object IconToBitmapUtils {
    
    fun getPOIBitmapDescriptor(context: Context, categoria: CategoriaPOI): BitmapDescriptor {
        return when (categoria) {
            CategoriaPOI.COMIDA -> createCustomMarker(context, Color.GREEN, "🍽️")
            CategoriaPOI.ENTRETENIMIENTO -> createCustomMarker(context, Color.MAGENTA, "🎭")
            CategoriaPOI.CULTURA -> createCustomMarker(context, Color.CYAN, "🏛️")
            CategoriaPOI.DEPORTE -> createCustomMarker(context, Color.RED, "⚽")
            CategoriaPOI.SALUD -> createCustomMarker(context, Color.RED, "🏥")
            CategoriaPOI.EDUCACION -> createCustomMarker(context, Color.BLUE, "🎓")
            CategoriaPOI.TRANSPORTE -> createCustomMarker(context, Color.CYAN, "🚌")
            CategoriaPOI.SERVICIOS -> createCustomMarker(context, Color.YELLOW, "🔧")
            CategoriaPOI.TURISMO -> createCustomMarker(context, Color.CYAN, "🗺️")
            CategoriaPOI.RECARGA_ELECTRICA -> createCustomMarker(context, Color.GREEN, "🔌")
            CategoriaPOI.PARQUES -> createCustomMarker(context, Color.GREEN, "🌳")
            CategoriaPOI.SHOPPING -> createCustomMarker(context, Color.MAGENTA, "🛍️")
            CategoriaPOI.OTRO -> createCustomMarker(context, Color.GRAY, "📍")
        }
    }
    
    private fun createCustomMarker(context: Context, color: Int, emoji: String): BitmapDescriptor {
        val size = 120
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Fondo circular
        val paint = Paint().apply {
            isAntiAlias = true
            this.color = Color.WHITE
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 10, paint)
        
        // Borde
        val borderPaint = Paint().apply {
            isAntiAlias = true
            this.color = color
            style = Paint.Style.STROKE
            strokeWidth = 8f
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 10, borderPaint)
        
        // Emoji
        val textPaint = Paint().apply {
            isAntiAlias = true
            textSize = 40f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(emoji, size / 2f, size / 2f + 15, textPaint)
        
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
