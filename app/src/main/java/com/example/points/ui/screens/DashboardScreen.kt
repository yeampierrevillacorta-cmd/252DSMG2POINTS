package com.example.points.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.points.data.model.IncidentesPorTipo

@Composable
fun DashboardScreen() {
    val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarDashboard()
    }
    val datos: List<IncidentesPorTipo> = uiState.datosDashboard
    // llamar a los screen con diseño estadístico
    BarrasScreen(datos)
    PieScreen(datos)
}

