package com.example.points.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.points.models.Incident
import java.text.SimpleDateFormat
import java.util.*

object ShareUtils {
    
    /**
     * Genera el texto para compartir un incidente
     */
    fun generateShareText(incident: Incident): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val reportDate = incident.fechaHora.toDate()
        
        return buildString {
            append("🚨 ALERTA CIUDADANA - POINTS\n\n")
            append("📍 Tipo: ${incident.tipo}\n")
            append("📝 Descripción: ${incident.descripcion}\n")
            append("📅 Fecha: ${dateFormat.format(reportDate)}\n")
            append("📍 Ubicación: ${String.format("%.6f", incident.ubicacion.lat)}, ${String.format("%.6f", incident.ubicacion.lon)}\n")
            
            if (incident.ubicacion.direccion.isNotEmpty()) {
                append("🏠 Dirección: ${incident.ubicacion.direccion}\n")
            }
            
            append("⚠️ Estado: ${incident.estado.displayName}\n\n")
            append("📱 Reportado a través de POINTS App")
            
            // Agregar enlace a Google Maps
            val mapsUrl = "https://maps.google.com/?q=${incident.ubicacion.lat},${incident.ubicacion.lon}"
            append("\n🗺️ Ver en Google Maps: $mapsUrl")
        }
    }
    
    /**
     * Comparte un incidente vía WhatsApp
     */
    fun shareViaWhatsApp(context: Context, incident: Incident) {
        try {
            val shareText = generateShareText(incident)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                setPackage("com.whatsapp")
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            
            // Verificar si WhatsApp está instalado
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // Si WhatsApp no está instalado, abrir en el navegador web
                shareViaWhatsAppWeb(context, incident)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error al compartir vía WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Comparte un incidente vía WhatsApp Web
     */
    private fun shareViaWhatsAppWeb(context: Context, incident: Incident) {
        try {
            val shareText = generateShareText(incident)
            val encodedText = Uri.encode(shareText)
            val url = "https://api.whatsapp.com/send?text=$encodedText"
            
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Error al abrir WhatsApp Web", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Comparte un incidente vía Facebook
     */
    fun shareViaFacebook(context: Context, incident: Incident) {
        try {
            val shareText = generateShareText(incident)
            
            // Intentar abrir la app de Facebook
            val facebookIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                setPackage("com.facebook.katana")
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            
            if (facebookIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(facebookIntent)
            } else {
                // Si Facebook no está instalado, usar el compartir genérico
                shareGeneric(context, incident, "Facebook")
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error al compartir vía Facebook", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Comparte un incidente usando el selector genérico de Android
     */
    fun shareGeneric(context: Context, incident: Incident, platform: String? = null) {
        try {
            val shareText = generateShareText(incident)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
                putExtra(Intent.EXTRA_SUBJECT, "Alerta Ciudadana - ${incident.tipo}")
            }
            
            val title = if (platform != null) "Compartir vía $platform" else "Compartir Incidente"
            context.startActivity(Intent.createChooser(intent, title))
        } catch (e: Exception) {
            Toast.makeText(context, "Error al compartir", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Abre la ubicación del incidente en Google Maps
     */
    fun openInGoogleMaps(context: Context, incident: Incident) {
        try {
            val uri = Uri.parse("geo:${incident.ubicacion.lat},${incident.ubicacion.lon}?q=${incident.ubicacion.lat},${incident.ubicacion.lon}(${incident.tipo})")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
            }
            
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // Si Google Maps no está instalado, abrir en el navegador
                val webUri = Uri.parse("https://maps.google.com/?q=${incident.ubicacion.lat},${incident.ubicacion.lon}")
                val webIntent = Intent(Intent.ACTION_VIEW, webUri)
                context.startActivity(webIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error al abrir Google Maps", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Muestra un diálogo de opciones para compartir
     */
    fun showShareOptions(
        context: Context, 
        incident: Incident,
        onWhatsAppClick: () -> Unit = { shareViaWhatsApp(context, incident) },
        onFacebookClick: () -> Unit = { shareViaFacebook(context, incident) },
        onGenericClick: () -> Unit = { shareGeneric(context, incident) }
    ) {
        // Esta función será llamada desde los composables para mostrar las opciones
        onGenericClick()
    }
}
