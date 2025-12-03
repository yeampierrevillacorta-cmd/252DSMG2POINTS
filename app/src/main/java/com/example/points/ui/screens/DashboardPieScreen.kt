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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.points.data.model.IncidentesPorTipo
import com.github.tehras.charts.piechart.PieChart
import com.github.tehras.charts.piechart.PieChartData
import com.github.tehras.charts.piechart.renderer.SimpleSliceDrawer

@Composable
fun PieScreen(data: List<IncidentesPorTipo>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = "Gráfico Pie",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Pie(data)
    }
}

@Composable
fun Pie(data: List<IncidentesPorTipo>) {
    if (data.isEmpty()) {
        Text(
            text = "No hay datos para mostrar",
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.Center
        )
        return
    }
    
    val datos = data
    val slices = ArrayList<PieChartData.Slice>()
    val total = datos.sumOf { it.cantidad.toDouble() }.toFloat()
    
    if (total <= 0) {
        Text(
            text = "No hay datos válidos para mostrar",
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.Center
        )
        return
    }
    
    // Lista de colores predefinidos para asegurar consistencia
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
    
    // Crear un mapa de colores por índice para mantener consistencia
    val coloresAsignados = datos.mapIndexed { index, _ ->
        colores[index % colores.size]
    }

    datos.forEachIndexed { index, datosItem ->
        slices.add(
            PieChartData.Slice(
                value = datosItem.cantidad.toFloat(),
                color = coloresAsignados[index]
            )
        )
    }
    
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PieChart(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .height(300.dp),
            sliceDrawer = SimpleSliceDrawer(
                sliceThickness = 100f
            ),
            pieChartData = PieChartData(
                slices = slices
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        // Leyenda del Pie - usar el mismo color que el slice correspondiente
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            datos.forEachIndexed { index, it ->
                val porcentaje = if (total > 0) {
                    (it.cantidad / total * 100).toInt()
                } else {
                    0
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(vertical = 6.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(
                                coloresAsignados[index],
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${it.descripcion}: ${it.cantidad} (${porcentaje}%)",
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

