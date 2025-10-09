package com.example.points.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.Flow

data class LocationState(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracy: Float? = null,
    val isLocationEnabled: Boolean = false,
    val hasPermission: Boolean = false,
    val error: String? = null
)

class LocationService(private val context: Context) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val _locationState = MutableStateFlow(LocationState())
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()
    
    private val locationListener = LocationListener { location ->
        _locationState.value = _locationState.value.copy(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            isLocationEnabled = true,
            error = null
        )
    }
    
    fun checkPermissions(): Boolean {
        val hasFineLocation = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val hasCoarseLocation = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        val hasPermission = hasFineLocation || hasCoarseLocation
        
        _locationState.value = _locationState.value.copy(hasPermission = hasPermission)
        
        return hasPermission
    }
    
    fun isLocationEnabled(): Boolean {
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        
        val isEnabled = isGpsEnabled || isNetworkEnabled
        
        _locationState.value = _locationState.value.copy(isLocationEnabled = isEnabled)
        
        return isEnabled
    }
    
    fun startLocationUpdates(): Flow<LocationState> = callbackFlow {
        if (!checkPermissions()) {
            _locationState.value = _locationState.value.copy(
                error = "Permisos de ubicación no concedidos"
            )
            trySend(_locationState.value)
            close()
            return@callbackFlow
        }
        
        if (!isLocationEnabled()) {
            _locationState.value = _locationState.value.copy(
                error = "Ubicación deshabilitada en el dispositivo"
            )
            trySend(_locationState.value)
            close()
            return@callbackFlow
        }
        
        try {
            // Intentar obtener la última ubicación conocida primero
            val lastKnownLocation = getLastKnownLocation()
            if (lastKnownLocation != null) {
                _locationState.value = _locationState.value.copy(
                    latitude = lastKnownLocation.latitude,
                    longitude = lastKnownLocation.longitude,
                    accuracy = lastKnownLocation.accuracy,
                    error = null
                )
                trySend(_locationState.value)
            }
            
            // Solicitar actualizaciones de ubicación
            val hasFineLocation = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            
            if (hasFineLocation) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000L, // 10 segundos
                    10f, // 10 metros
                    locationListener
                )
            } else {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    10000L, // 10 segundos
                    10f, // 10 metros
                    locationListener
                )
            }
            
            // Enviar actualizaciones del estado
            _locationState.collect { state ->
                trySend(state)
            }
            
        } catch (e: SecurityException) {
            _locationState.value = _locationState.value.copy(
                error = "Error de seguridad al acceder a la ubicación: ${e.message}"
            )
            trySend(_locationState.value)
        } catch (e: Exception) {
            _locationState.value = _locationState.value.copy(
                error = "Error al obtener ubicación: ${e.message}"
            )
            trySend(_locationState.value)
        }
        
        awaitClose {
            try {
                locationManager.removeUpdates(locationListener)
            } catch (e: SecurityException) {
                // Ignorar error de seguridad al remover listener
            }
        }
    }
    
    private fun getLastKnownLocation(): Location? {
        return try {
            val hasFineLocation = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            
            if (hasFineLocation) {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } else {
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }
        } catch (e: SecurityException) {
            null
        }
    }
    
    fun stopLocationUpdates() {
        try {
            locationManager.removeUpdates(locationListener)
        } catch (e: SecurityException) {
            // Ignorar error de seguridad
        }
    }
    
    fun getCurrentLocation(): LocationState {
        // Intentar obtener ubicación si no la tenemos
        if (_locationState.value.latitude == null || _locationState.value.longitude == null) {
            requestLocationUpdates()
        }
        return _locationState.value
    }
    
    // Función para forzar la obtención de ubicación
    fun forceLocationUpdate(): LocationState {
        requestLocationUpdates()
        return _locationState.value
    }
    
    // Función para solicitar actualizaciones de ubicación
    private fun requestLocationUpdates() {
        try {
            val hasFineLocation = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            
            if (hasFineLocation) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000L, // 5 segundos
                    5f, // 5 metros
                    locationListener
                )
            } else {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    5000L, // 5 segundos
                    5f, // 5 metros
                    locationListener
                )
            }
        } catch (e: SecurityException) {
            _locationState.value = _locationState.value.copy(
                error = "Permisos de ubicación no otorgados"
            )
        }
    }
    
    // Función para calcular distancia entre dos puntos
    fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0].toDouble() / 1000.0 // Convertir a kilómetros
    }
    
    // Función para obtener sugerencias basadas en ubicación
    fun getLocationBasedSuggestions(
        currentLat: Double,
        currentLon: Double,
        radiusKm: Double = 5.0
    ): List<String> {
        val suggestions = mutableListOf<String>()
        
        // Sugerencias basadas en la hora del día
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        
        when (hour) {
            in 6..10 -> {
                suggestions.addAll(listOf(
                    "Cafeterías cercanas",
                    "Desayunos",
                    "Panaderías",
                    "Parques para caminar"
                ))
            }
            in 11..14 -> {
                suggestions.addAll(listOf(
                    "Restaurantes para almorzar",
                    "Comida rápida",
                    "Mercados",
                    "Parques para picnic"
                ))
            }
            in 15..18 -> {
                suggestions.addAll(listOf(
                    "Cafés",
                    "Librerías",
                    "Museos",
                    "Centros comerciales"
                ))
            }
            in 19..22 -> {
                suggestions.addAll(listOf(
                    "Restaurantes para cenar",
                    "Bares",
                    "Cines",
                    "Teatros"
                ))
            }
            else -> {
                suggestions.addAll(listOf(
                    "Farmacias 24 horas",
                    "Hospitales",
                    "Gasolineras",
                    "Estaciones de servicio"
                ))
            }
        }
        
        // Agregar sugerencias basadas en el día de la semana
        val dayOfWeek = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)
        when (dayOfWeek) {
            java.util.Calendar.SATURDAY, java.util.Calendar.SUNDAY -> {
                suggestions.addAll(listOf(
                    "Parques familiares",
                    "Actividades recreativas",
                    "Eventos del fin de semana",
                    "Mercados locales"
                ))
            }
            else -> {
                suggestions.addAll(listOf(
                    "Oficinas públicas",
                    "Bancos",
                    "Servicios gubernamentales",
                    "Estaciones de transporte"
                ))
            }
        }
        
        return suggestions.distinct()
    }
}
