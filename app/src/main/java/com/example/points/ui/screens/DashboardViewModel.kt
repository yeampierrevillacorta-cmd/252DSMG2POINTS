package com.example.points.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.points.PointsApplication
import com.example.points.data.model.IncidentesPorTipo
import com.example.points.data.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val datosDashboard: List<IncidentesPorTipo> = listOf(),
    val flag_error_dashboard: Boolean = false,
)

class DashboardViewModel(private val dashboardRepository: DashboardRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun cargarDashboard() {
        viewModelScope.launch {
            val result = dashboardRepository.getAllIncidents()
            result.onSuccess { incidents ->
                // Agrupar incidentes por tipo
                val resumen = incidents
                    .filter { !it.tipo.isNullOrBlank() } // excluir nulos o vacíos
                    .groupBy { it.tipo }
                    .map { (tipo, lista) ->
                        IncidentesPorTipo(tipo, lista.size)
                    }
                // Actualizar el estado de IU
                _uiState.value = _uiState.value.copy(
                    datosDashboard = resumen,
                    flag_error_dashboard = false
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    flag_error_dashboard = true
                )
            }
        }
    }

    fun resetFlags() {
        _uiState.value = _uiState.value.copy(
            flag_error_dashboard = false
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                        as PointsApplication
                    )
                val dashboardRepository = application.container.dashboardRepository
                DashboardViewModel(dashboardRepository = dashboardRepository)
            }
        }
    }
}

