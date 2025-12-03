package com.example.points.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.points.models.Notification
import com.example.points.models.TipoNotificacion
import com.example.points.repository.NotificationRepository
import com.example.points.worker.AlertWorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Context
import android.util.Log

data class AlertsUiState(
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val alertsEnabled: Boolean = false,
    val radiusKm: Double = 5.0,
    val enableIncidents: Boolean = true,
    val enableEvents: Boolean = true,
    val selectedFilter: TipoNotificacion? = null,
    val showOnlyUnread: Boolean = false
)

class AlertsViewModel(
    private val context: Context
) : ViewModel() {
    
    private val notificationRepository = NotificationRepository()
    private val alertWorkManager = AlertWorkManager(context)
    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    
    private val _uiState = MutableStateFlow(AlertsUiState())
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()
    
    init {
        loadNotifications()
        checkAlertsStatus()
    }
    
    fun loadNotifications() {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                notificationRepository.getUserNotifications(userId).collect { notifications ->
                    val filtered = applyFilters(notifications)
                    _uiState.value = _uiState.value.copy(
                        notifications = filtered,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("AlertsViewModel", "Error loading notifications", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar notificaciones: ${e.message}"
                )
            }
        }
    }
    
    private fun applyFilters(notifications: List<Notification>): List<Notification> {
        var filtered = notifications
        
        // Filtrar por tipo
        _uiState.value.selectedFilter?.let { filter ->
            filtered = filtered.filter { it.tipo == filter }
        }
        
        // Filtrar solo no leídas
        if (_uiState.value.showOnlyUnread) {
            filtered = filtered.filter { !it.leida }
        }
        
        return filtered
    }
    
    fun setFilter(filter: TipoNotificacion?) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
        loadNotifications()
    }
    
    fun toggleShowOnlyUnread() {
        _uiState.value = _uiState.value.copy(
            showOnlyUnread = !_uiState.value.showOnlyUnread
        )
        loadNotifications()
    }
    
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
        }
    }
    
    fun markAllAsRead() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            notificationRepository.markAllAsRead(userId)
        }
    }
    
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(notificationId)
        }
    }
    
    fun enableAlerts(radiusKm: Double, enableIncidents: Boolean, enableEvents: Boolean) {
        viewModelScope.launch {
            try {
                alertWorkManager.schedulePeriodicAlerts(
                    radiusKm = radiusKm,
                    enableIncidents = enableIncidents,
                    enableEvents = enableEvents
                )
                _uiState.value = _uiState.value.copy(
                    alertsEnabled = true,
                    radiusKm = radiusKm,
                    enableIncidents = enableIncidents,
                    enableEvents = enableEvents
                )
            } catch (e: Exception) {
                Log.e("AlertsViewModel", "Error enabling alerts", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al activar alertas: ${e.message}"
                )
            }
        }
    }
    
    fun disableAlerts() {
        viewModelScope.launch {
            try {
                alertWorkManager.cancelPeriodicAlerts()
                _uiState.value = _uiState.value.copy(alertsEnabled = false)
            } catch (e: Exception) {
                Log.e("AlertsViewModel", "Error disabling alerts", e)
            }
        }
    }
    
    fun checkAlertsNow() {
        viewModelScope.launch {
            try {
                val state = _uiState.value
                alertWorkManager.checkAlertsNow(
                    radiusKm = state.radiusKm,
                    enableIncidents = state.enableIncidents,
                    enableEvents = state.enableEvents
                )
            } catch (e: Exception) {
                Log.e("AlertsViewModel", "Error checking alerts now", e)
            }
        }
    }
    
    private fun checkAlertsStatus() {
        viewModelScope.launch {
            try {
                val isEnabled = alertWorkManager.isAlertsEnabled()
                _uiState.value = _uiState.value.copy(alertsEnabled = isEnabled)
            } catch (e: Exception) {
                Log.e("AlertsViewModel", "Error checking alerts status", e)
            }
        }
    }
    
    fun updateRadius(radiusKm: Double) {
        _uiState.value = _uiState.value.copy(radiusKm = radiusKm)
        if (_uiState.value.alertsEnabled) {
            enableAlerts(
                radiusKm = radiusKm,
                enableIncidents = _uiState.value.enableIncidents,
                enableEvents = _uiState.value.enableEvents
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

