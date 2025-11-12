package com.example.points.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.points.data.model.DatosPorMes
import com.github.tehras.charts.bar.BarChart
import com.github.tehras.charts.bar.BarChartData
import com.github.tehras.charts.bar.renderer.label.SimpleValueDrawer

@Composable
fun DashboardMensualScreen(data: List<DatosPorMes>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Distribución por Mes",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        BarrasMensuales(data)
        Spacer(modifier = Modifier.height(16.dp))
        LeyendaMensual()
    }
}

@Composable
fun BarrasMensuales(data: List<DatosPorMes>) {
    val barras = ArrayList<BarChartData.Bar>()
    
    // Colores para cada tipo de dato
    val colorIncidentes = androidx.compose.ui.graphics.Color(0xFFF44336) // Rojo
    val colorEventos = androidx.compose.ui.graphics.Color(0xFF2196F3) // Azul
    val colorPOIs = androidx.compose.ui.graphics.Color(0xFF4CAF50) // Verde
    
    // Crear barras agrupadas por mes
    // Para cada mes, creamos 3 barras (incidentes, eventos, POIs)
    data.forEach { datosMes ->
        // Barra de incidentes
        barras.add(
            BarChartData.Bar(
                label = "${datosMes.mes}\nIncidentes",
                value = datosMes.incidentes.toFloat(),
                color = colorIncidentes
            )
        )
        // Barra de eventos
        barras.add(
            BarChartData.Bar(
                label = "${datosMes.mes}\nEventos",
                value = datosMes.eventos.toFloat(),
                color = colorEventos
            )
        )
        // Barra de POIs
        barras.add(
            BarChartData.Bar(
                label = "${datosMes.mes}\nPOIs",
                value = datosMes.pois.toFloat(),
                color = colorPOIs
            )
        )
    }
    
    if (barras.isNotEmpty()) {
        BarChart(
            modifier = Modifier
                .padding(30.dp, 20.dp)
                .height(400.dp),
            labelDrawer = SimpleValueDrawer(
                drawLocation = SimpleValueDrawer.DrawLocation.XAxis
            ),
            barChartData = BarChartData(
                bars = barras
            )
        )
    } else {
        Text(
            text = "No hay datos disponibles",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun LeyendaMensual() {
    val colorIncidentes = androidx.compose.ui.graphics.Color(0xFFF44336)
    val colorEventos = androidx.compose.ui.graphics.Color(0xFF2196F3)
    val colorPOIs = androidx.compose.ui.graphics.Color(0xFF4CAF50)
    
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Leyenda:",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        ItemLeyendaMensual("Incidentes", colorIncidentes)
        ItemLeyendaMensual("Eventos", colorEventos)
        ItemLeyendaMensual("POIs", colorPOIs)
    }
}

@Composable
fun ItemLeyendaMensual(texto: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(texto)
    }
}

