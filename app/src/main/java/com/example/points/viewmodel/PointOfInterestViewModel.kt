package com.example.points.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.points.models.PointOfInterest
import com.example.points.models.CategoriaPOI
import com.example.points.models.EstadoPOI
import com.example.points.repository.PointOfInterestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import android.util.Log

data class POIUIState(
    val isLoading: Boolean = false,
    val pois: List<PointOfInterest> = emptyList(),
    val filteredPOIs: List<PointOfInterest> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: CategoriaPOI? = null,
    val showOnlyNearby: Boolean = false,
    val userLocation: Pair<Double, Double>? = null,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false
)

class PointOfInterestViewModel : ViewModel() {
    private val repository = PointOfInterestRepository()
    
    private val _uiState = MutableStateFlow(POIUIState())
    val uiState: StateFlow<POIUIState> = _uiState.asStateFlow()
    
    // Control de concurrencia para evitar múltiples cargas simultáneas
    private var currentLoadJob: Job? = null
    
    init {
        loadAllPOIs()
        // Cargar POIs de prueba temporalmente para debug
        loadTestPOIs()
    }
    
    // Método temporal para cargar POIs de prueba
    private fun loadTestPOIs() {
        viewModelScope.launch {
            try {
                val testPOIs = listOf(
                    PointOfInterest(
                        id = "test1",
                        nombre = "Restaurante Test",
                        descripcion = "Restaurante de prueba",
                        categoria = CategoriaPOI.COMIDA,
                        direccion = "Calle Test 123",
                        ubicacion = com.example.points.models.Ubicacion(lat = 40.4168, lon = -3.7038), // Madrid
                        telefono = "123456789",
                        email = "test@test.com",
                        sitioWeb = "https://test.com",
                        imagenes = emptyList(),
                        calificacion = 4.5,
                        totalCalificaciones = 10,
                        estado = EstadoPOI.APROBADO,
                        fechaCreacion = com.google.firebase.Timestamp.now(),
                        fechaActualizacion = com.google.firebase.Timestamp.now(),
                        usuarioId = "test_user",
                        horarios = emptyList(),
                        caracteristicas = emptyList()
                    ),
                    PointOfInterest(
                        id = "test2",
                        nombre = "Parque Test",
                        descripcion = "Parque de prueba",
                        categoria = CategoriaPOI.PARQUES,
                        direccion = "Parque Test 456",
                        ubicacion = com.example.points.models.Ubicacion(lat = 40.4200, lon = -3.7100), // Madrid
                        telefono = null,
                        email = null,
                        sitioWeb = null,
                        imagenes = emptyList(),
                        calificacion = 4.0,
                        totalCalificaciones = 5,
                        estado = EstadoPOI.APROBADO,
                        fechaCreacion = com.google.firebase.Timestamp.now(),
                        fechaActualizacion = com.google.firebase.Timestamp.now(),
                        usuarioId = "test_user",
                        horarios = emptyList(),
                        caracteristicas = emptyList()
                    )
                )
                
                Log.d("POIViewModel", "Cargando POIs de prueba: ${testPOIs.size}")
                _uiState.value = _uiState.value.copy(
                    pois = testPOIs,
                    filteredPOIs = applyFilters(testPOIs),
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e("POIViewModel", "Error cargando POIs de prueba", e)
            }
        }
    }
    
    fun loadAllPOIs() {
        // Cancelar carga anterior si existe
        currentLoadJob?.cancel()
        
        currentLoadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                Log.d("POIViewModel", "Iniciando carga de POIs...")
                repository.getAllApprovedPOIs().collect { pois ->
                    Log.d("POIViewModel", "POIs recibidos: ${pois.size}")
                    pois.forEach { poi ->
                        Log.d("POIViewModel", "POI: ${poi.nombre} - Estado: ${poi.estado.displayName} - Coord: (${poi.ubicacion.lat}, ${poi.ubicacion.lon})")
                    }
                    _uiState.value = _uiState.value.copy(
                        pois = pois,
                        filteredPOIs = applyFilters(pois),
                        isLoading = false
                    )
                }
            } catch (e: CancellationException) {
                // No mostrar error para cancelaciones normales
                Log.d("POIViewModel", "POI loading was cancelled")
            } catch (e: Exception) {
                Log.e("POIViewModel", "Error loading POIs", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar los puntos de interés: ${e.message}"
                )
            }
        }
    }
    
    fun loadPOIsByCategory(categoria: CategoriaPOI) {
        // Cancelar carga anterior si existe
        currentLoadJob?.cancel()
        
        currentLoadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                repository.getPOIsByCategory(categoria).collect { pois ->
                    _uiState.value = _uiState.value.copy(
                        pois = pois,
                        filteredPOIs = applyFilters(pois),
                        isLoading = false
                    )
                }
            } catch (e: CancellationException) {
                // No mostrar error para cancelaciones normales
                Log.d("POIViewModel", "POI loading by category was cancelled")
            } catch (e: Exception) {
                Log.e("POIViewModel", "Error loading POIs by category", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar los puntos de interés: ${e.message}"
                )
            }
        }
    }
    
    fun searchPOIs(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        
        if (query.isBlank()) {
            loadAllPOIs()
        } else {
            // Cancelar carga anterior si existe
            currentLoadJob?.cancel()
            
            currentLoadJob = viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                try {
                    repository.searchPOIs(query).collect { pois ->
                        _uiState.value = _uiState.value.copy(
                            pois = pois,
                            filteredPOIs = applyFilters(pois),
                            isLoading = false
                        )
                    }
                } catch (e: CancellationException) {
                    // No mostrar error para cancelaciones normales
                    Log.d("POIViewModel", "POI search was cancelled")
                } catch (e: Exception) {
                    Log.e("POIViewModel", "Error searching POIs", e)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al buscar puntos de interés: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun loadNearbyPOIs(lat: Double, lon: Double, radiusKm: Double = 5.0) {
        // Cancelar carga anterior si existe
        currentLoadJob?.cancel()
        
        currentLoadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                errorMessage = null,
                userLocation = Pair(lat, lon)
            )
            
            try {
                repository.getNearbyPOIs(lat, lon, radiusKm).collect { pois ->
                    _uiState.value = _uiState.value.copy(
                        pois = pois,
                        filteredPOIs = applyFilters(pois),
                        isLoading = false
                    )
                }
            } catch (e: CancellationException) {
                // No mostrar error para cancelaciones normales
                Log.d("POIViewModel", "POI loading nearby was cancelled")
            } catch (e: Exception) {
                Log.e("POIViewModel", "Error loading nearby POIs", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar puntos cercanos: ${e.message}"
                )
            }
        }
    }
    
    fun setCategoryFilter(categoria: CategoriaPOI?) {
        _uiState.value = _uiState.value.copy(selectedCategory = categoria)
        _uiState.value = _uiState.value.copy(filteredPOIs = applyFilters(_uiState.value.pois))
    }
    
    fun toggleNearbyFilter() {
        val newShowOnlyNearby = !_uiState.value.showOnlyNearby
        _uiState.value = _uiState.value.copy(showOnlyNearby = newShowOnlyNearby)
        _uiState.value = _uiState.value.copy(filteredPOIs = applyFilters(_uiState.value.pois))
    }
    
    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            selectedCategory = null,
            showOnlyNearby = false
        )
        loadAllPOIs()
    }
    
    private fun applyFilters(pois: List<PointOfInterest>): List<PointOfInterest> {
        var filtered = pois
        
        // Filtrar por categoría
        _uiState.value.selectedCategory?.let { categoria ->
            filtered = filtered.filter { it.categoria == categoria }
        }
        
        // Filtrar por búsqueda
        if (_uiState.value.searchQuery.isNotBlank()) {
            val query = _uiState.value.searchQuery.lowercase()
            filtered = filtered.filter { poi ->
                poi.nombre.lowercase().contains(query) ||
                poi.descripcion.lowercase().contains(query) ||
                poi.direccion.lowercase().contains(query)
            }
        }
        
        // Filtrar por cercanía (si se tiene ubicación del usuario)
        if (_uiState.value.showOnlyNearby && _uiState.value.userLocation != null) {
            val userLocation = _uiState.value.userLocation!!
            val (userLat, userLon) = userLocation
            filtered = filtered.filter { poi ->
                val distance = calculateDistance(userLat, userLon, poi.ubicacion.lat, poi.ubicacion.lon)
                distance <= 5.0 // 5 km de radio
            }.sortedBy { poi ->
                calculateDistance(userLat, userLon, poi.ubicacion.lat, poi.ubicacion.lon)
            }
        }
        
        return filtered
    }
    
    fun submitPOI(poi: PointOfInterest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)
            
            try {
                val result = repository.createPOI(poi)
                result.fold(
                    onSuccess = { poiId ->
                        Log.d("POIViewModel", "POI submitted successfully: $poiId")
                        _uiState.value = _uiState.value.copy(
                            isSubmitting = false,
                            submitSuccess = true
                        )
                        // Recargar la lista después de un breve delay
                        kotlinx.coroutines.delay(1000)
                        loadAllPOIs()
                    },
                    onFailure = { error ->
                        Log.e("POIViewModel", "Error submitting POI", error)
                        _uiState.value = _uiState.value.copy(
                            isSubmitting = false,
                            errorMessage = "Error al enviar el punto de interés: ${error.message}"
                        )
                    }
                )
            } catch (e: CancellationException) {
                // No mostrar error para cancelaciones normales
                Log.d("POIViewModel", "POI submission was cancelled")
                _uiState.value = _uiState.value.copy(isSubmitting = false)
            } catch (e: Exception) {
                Log.e("POIViewModel", "Error submitting POI", e)
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = "Error al enviar el punto de interés: ${e.message}"
                )
            }
        }
    }
    
    fun clearSubmitSuccess() {
        _uiState.value = _uiState.value.copy(submitSuccess = false)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    // Obtener POI por ID
    suspend fun getPOIById(poiId: String): Result<PointOfInterest?> {
        return try {
            repository.getPOIById(poiId)
        } catch (e: Exception) {
            Log.e("POIViewModel", "Error getting POI by ID", e)
            Result.failure(e)
        }
    }
    
    // Actualizar ubicación del usuario
    fun updateUserLocation(lat: Double, lon: Double) {
        _uiState.value = _uiState.value.copy(userLocation = Pair(lat, lon))
    }
    
    override fun onCleared() {
        super.onCleared()
        // Cancelar cualquier trabajo pendiente cuando el ViewModel se destruye
        currentLoadJob?.cancel()
    }
    
    // Calcular distancia entre dos puntos (fórmula de Haversine)
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // Radio de la Tierra en kilómetros
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        
        return earthRadius * c
    }
}
