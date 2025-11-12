package com.example.points.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.points.data.model.IncidentesPorTipo
import com.example.points.utils.Utils
import com.github.tehras.charts.bar.BarChart
import com.github.tehras.charts.bar.BarChartData
import com.github.tehras.charts.bar.renderer.label.SimpleValueDrawer

@Composable
fun BarrasScreen(data: List<IncidentesPorTipo>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Gráfico de Barras")
        Barras(data)
    }
}

@Composable
fun Barras(data: List<IncidentesPorTipo>) {
    val datos = data
    var barras = ArrayList<BarChartData.Bar>()
    
    // Lista de colores predefinidos para asegurar consistencia con el gráfico Pie
    val colores = listOf(
        androidx.compose.ui.graphics.Color(0xFFF44336), // Rojo
        androidx.compose.ui.graphics.Color(0xFF4CAF50), // Verde
        androidx.compose.ui.graphics.Color(0xFFFFEB3B), // Amarillo
        androidx.compose.ui.graphics.Color(0xFF673AB7), // Morado
        androidx.compose.ui.graphics.Color(0xFF9C27B0), // Morado oscuro
        androidx.compose.ui.graphics.Color(0xFF03A9F4), // Azul
        androidx.compose.ui.graphics.Color(0xFFCDDC39), // Verde lima
        androidx.compose.ui.graphics.Color(0xFFE91E63), // Rosa
        androidx.compose.ui.graphics.Color(0xFF00BCD4), // Cian
        androidx.compose.ui.graphics.Color(0xFFFF9800), // Naranja
        androidx.compose.ui.graphics.Color(0xFF009688), // Verde azulado
    )
    
    datos.mapIndexed { index, datos ->
        barras.add(
            BarChartData.Bar(
                label = datos.descripcion,
                value = datos.cantidad.toFloat(),
                color = colores[index % colores.size]
            )
        )
    }
    BarChart(
        modifier = Modifier
            .padding(30.dp, 80.dp)
            .height(300.dp),
        labelDrawer = SimpleValueDrawer(
            drawLocation = SimpleValueDrawer.DrawLocation.XAxis
        ),
        barChartData = BarChartData(
            bars = barras
        )
    )
}

