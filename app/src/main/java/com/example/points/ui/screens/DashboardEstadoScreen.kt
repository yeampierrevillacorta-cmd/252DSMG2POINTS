package com.example.points.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.points.data.model.DatosPorEstado
import com.github.tehras.charts.bar.BarChart
import com.github.tehras.charts.bar.BarChartData
import com.github.tehras.charts.bar.renderer.label.SimpleValueDrawer

@Composable
fun DashboardEstadoScreen(data: List<DatosPorEstado>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Distribución por Estado",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 20.dp),
                textAlign = TextAlign.Center
            )
            BarrasPorEstado(data)
            Spacer(modifier = Modifier.height(20.dp))
            LeyendaEstados()
        }
    }
}

@Composable
fun BarrasPorEstado(data: List<DatosPorEstado>) {
    if (data.isEmpty()) {
        Text(
            text = "No hay datos disponibles",
            modifier = Modifier.padding(16.dp),
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        return
    }
    
    val barras = ArrayList<BarChartData.Bar>()
    
    // Colores para cada estado
    val colorAtendido = androidx.compose.ui.graphics.Color(0xFF4CAF50) // Verde
    val colorDenegado = androidx.compose.ui.graphics.Color(0xFFF44336) // Rojo
    val colorEnRevision = androidx.compose.ui.graphics.Color(0xFFFF9800) // Naranja
    
    // Crear barras agrupadas por tipo
    data.forEach { datosEstado ->
        // Usar solo la primera letra del tipo para simplificar
        val tipoCorto = when (datosEstado.tipo) {
            "Incidentes" -> "I"
            "Eventos" -> "E"
            "POIs" -> "P"
            else -> datosEstado.tipo.first().toString()
        }
        
        // Barra de atendidos
        barras.add(
            BarChartData.Bar(
                label = "A",
                value = datosEstado.atendido.toFloat(),
                color = colorAtendido
            )
        )
        // Barra de denegados
        barras.add(
            BarChartData.Bar(
                label = "D",
                value = datosEstado.denegado.toFloat(),
                color = colorDenegado
            )
        )
        // Barra de en revisión
        barras.add(
            BarChartData.Bar(
                label = "R",
                value = datosEstado.enRevision.toFloat(),
                color = colorEnRevision
            )
        )
    }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Mostrar los tipos como etiquetas separadas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
        ) {
            data.forEach { datosEstado ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = datosEstado.tipo,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 4.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        // Gráfico de barras
        BarChart(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 140.dp)
                .height(500.dp),
            labelDrawer = SimpleValueDrawer(
                drawLocation = SimpleValueDrawer.DrawLocation.XAxis
            ),
            barChartData = BarChartData(
                bars = barras
            )
        )
        
        // Etiquetas de los tipos debajo del gráfico
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
        ) {
            data.forEach { datosEstado ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = datosEstado.tipo,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "A:${datosEstado.atendido} D:${datosEstado.denegado} R:${datosEstado.enRevision}",
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun LeyendaEstados() {
    val colorAtendido = androidx.compose.ui.graphics.Color(0xFF4CAF50)
    val colorDenegado = androidx.compose.ui.graphics.Color(0xFFF44336)
    val colorEnRevision = androidx.compose.ui.graphics.Color(0xFFFF9800)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ItemLeyenda("Atendido", colorAtendido)
        Spacer(modifier = Modifier.width(24.dp))
        ItemLeyenda("Denegado", colorDenegado)
        Spacer(modifier = Modifier.width(24.dp))
        ItemLeyenda("En Revisión", colorEnRevision)
    }
}

@Composable
fun ItemLeyenda(texto: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = texto,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

