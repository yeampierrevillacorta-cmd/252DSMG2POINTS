package com.example.points.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.points.data.model.IncidentesPorTipo

@Composable
fun DashboardScreen() {
    val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarDashboard()
        viewModel.cargarDatosMensuales()
        viewModel.cargarDatosPorEstado()
    }
    
    val datos: List<IncidentesPorTipo> = uiState.datosDashboard
    
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        // Gráficos originales
        BarrasScreen(datos)
        PieScreen(datos)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Nuevo gráfico mensual
        DashboardMensualScreen(uiState.datosPorMes)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Nuevo gráfico por estado
        DashboardEstadoScreen(uiState.datosPorEstado)
    }
}

