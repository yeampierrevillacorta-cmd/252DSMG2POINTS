package com.example.points.utils

import android.content.Context
import android.location.Geocoder
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Utilidades para geocodificación inversa (obtener dirección desde coordenadas)
 */
object GeocodingUtils {
    
    /**
     * Obtiene la dirección real de unas coordenadas usando geocodificación inversa
     * @param context Contexto de la aplicación
     * @param latitude Latitud
     * @param longitude Longitud
     * @return Dirección obtenida o null si hay error
     */
    suspend fun getAddressFromCoordinates(
        context: Context,
        latitude: Double,
        longitude: Double
    ): String? = withContext(Dispatchers.IO) {
        try {
            if (!Geocoder.isPresent()) {
                Log.w("GeocodingUtils", "Geocoder no está disponible")
                return@withContext null
            }
            
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                val addressLines = mutableListOf<String>()
                
                // Construir dirección completa
                for (i in 0..address.maxAddressLineIndex) {
                    address.getAddressLine(i)?.let { addressLines.add(it) }
                }
                
                // Si hay líneas de dirección, usar la primera (más completa)
                if (addressLines.isNotEmpty()) {
                    val fullAddress = addressLines[0]
                    Log.d("GeocodingUtils", "Dirección obtenida: $fullAddress")
                    return@withContext fullAddress
                }
                
                // Si no hay líneas completas, construir desde componentes
                val components = mutableListOf<String>()
                address.thoroughfare?.let { components.add(it) }
                address.subThoroughfare?.let { components.add(it) }
                address.locality?.let { components.add(it) }
                address.adminArea?.let { components.add(it) }
                address.countryName?.let { components.add(it) }
                
                if (components.isNotEmpty()) {
                    val constructedAddress = components.joinToString(", ")
                    Log.d("GeocodingUtils", "Dirección construida: $constructedAddress")
                    return@withContext constructedAddress
                }
            }
            
            Log.w("GeocodingUtils", "No se encontró dirección para las coordenadas: $latitude, $longitude")
            null
        } catch (e: Exception) {
            Log.e("GeocodingUtils", "Error en geocodificación inversa", e)
            null
        }
    }
    
    /**
     * Obtiene una dirección formateada de manera más legible
     */
    suspend fun getFormattedAddress(
        context: Context,
        latitude: Double,
        longitude: Double
    ): String {
        val address = getAddressFromCoordinates(context, latitude, longitude)
        return address ?: "Lat: ${String.format("%.6f", latitude)}, Lon: ${String.format("%.6f", longitude)}"
    }
}

