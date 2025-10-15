package com.example.points.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.points.models.Event
import com.example.points.models.CategoriaEvento
import com.example.points.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

data class EventUiState(
    val events: List<Event> = emptyList(),
    val upcomingEvents: List<Event> = emptyList(),
    val userEvents: List<Event> = emptyList(),
    val pendingEvents: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedCategory: CategoriaEvento? = null,
    val searchQuery: String = "",
    val showOnlyUpcoming: Boolean = true
)

class EventViewModel : ViewModel() {
    private val repository = EventRepository()
    
    private val _uiState = MutableStateFlow(EventUiState())
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()
    
    init {
        loadAllEvents()
        loadUpcomingEvents()
    }
    
    fun loadAllEvents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                repository.getAllApprovedEvents().collect { events ->
                    _uiState.value = _uiState.value.copy(
                        events = events,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error loading events", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar eventos: ${e.message}"
                )
            }
        }
    }
    
    fun loadUpcomingEvents() {
        viewModelScope.launch {
            try {
                repository.getUpcomingEvents().collect { events ->
                    _uiState.value = _uiState.value.copy(upcomingEvents = events)
                }
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error loading upcoming events", e)
            }
        }
    }
    
    fun loadUserEvents() {
        viewModelScope.launch {
            try {
                repository.getUserEvents().collect { events ->
                    _uiState.value = _uiState.value.copy(userEvents = events)
                }
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error loading user events", e)
            }
        }
    }
    
    fun loadPendingEvents() {
        viewModelScope.launch {
            try {
                repository.getPendingEvents().collect { events ->
                    _uiState.value = _uiState.value.copy(pendingEvents = events)
                }
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error loading pending events", e)
            }
        }
    }
    
    fun loadEventsByCategory(category: CategoriaEvento) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                selectedCategory = category,
                errorMessage = null
            )
            
            try {
                repository.getEventsByCategory(category).collect { events ->
                    _uiState.value = _uiState.value.copy(
                        events = events,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error loading events by category", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar eventos por categoría: ${e.message}"
                )
            }
        }
    }
    
    fun searchEvents(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                searchQuery = query,
                errorMessage = null
            )
            
            try {
                repository.searchEvents(query).collect { events ->
                    _uiState.value = _uiState.value.copy(
                        events = events,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error searching events", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al buscar eventos: ${e.message}"
                )
            }
        }
    }
    
    fun loadNearbyEvents(lat: Double, lon: Double, radiusKm: Double = 10.0) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                repository.getNearbyEvents(lat, lon, radiusKm).collect { events ->
                    _uiState.value = _uiState.value.copy(
                        events = events,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error loading nearby events", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar eventos cercanos: ${e.message}"
                )
            }
        }
    }
    
    fun createEvent(event: Event) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val result = repository.createEvent(event)
                result.fold(
                    onSuccess = { eventId ->
                        Log.d("EventViewModel", "Evento creado exitosamente: $eventId")
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        // Recargar eventos del usuario
                        loadUserEvents()
                    },
                    onFailure = { error ->
                        Log.e("EventViewModel", "Error creating event", error)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Error al crear evento: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error creating event", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al crear evento: ${e.message}"
                )
            }
        }
    }
    
    fun approveEvent(eventId: String, comentarios: String? = null) {
        viewModelScope.launch {
            try {
                val result = repository.approveEvent(eventId, comentarios)
                result.fold(
                    onSuccess = {
                        Log.d("EventViewModel", "Evento aprobado: $eventId")
                        loadPendingEvents()
                        loadAllEvents()
                    },
                    onFailure = { error ->
                        Log.e("EventViewModel", "Error approving event", error)
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Error al aprobar evento: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error approving event", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al aprobar evento: ${e.message}"
                )
            }
        }
    }
    
    fun rejectEvent(eventId: String, comentarios: String) {
        viewModelScope.launch {
            try {
                val result = repository.rejectEvent(eventId, comentarios)
                result.fold(
                    onSuccess = {
                        Log.d("EventViewModel", "Evento rechazado: $eventId")
                        loadPendingEvents()
                    },
                    onFailure = { error ->
                        Log.e("EventViewModel", "Error rejecting event", error)
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Error al rechazar evento: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error rejecting event", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al rechazar evento: ${e.message}"
                )
            }
        }
    }
    
    fun registerToEvent(eventId: String) {
        viewModelScope.launch {
            try {
                val result = repository.registerToEvent(eventId)
                result.fold(
                    onSuccess = {
                        Log.d("EventViewModel", "Usuario inscrito al evento: $eventId")
                        loadAllEvents()
                        loadUpcomingEvents()
                    },
                    onFailure = { error ->
                        Log.e("EventViewModel", "Error registering to event", error)
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Error al inscribirse: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error registering to event", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al inscribirse: ${e.message}"
                )
            }
        }
    }
    
    fun cancelEvent(eventId: String, motivo: String) {
        viewModelScope.launch {
            try {
                val result = repository.cancelEvent(eventId, motivo)
                result.fold(
                    onSuccess = {
                        Log.d("EventViewModel", "Evento cancelado: $eventId")
                        loadUserEvents()
                        loadAllEvents()
                    },
                    onFailure = { error ->
                        Log.e("EventViewModel", "Error canceling event", error)
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Error al cancelar evento: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error canceling event", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al cancelar evento: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            selectedCategory = null
        )
        loadAllEvents()
    }
    
    fun toggleShowOnlyUpcoming() {
        _uiState.value = _uiState.value.copy(
            showOnlyUpcoming = !_uiState.value.showOnlyUpcoming
        )
    }
}
