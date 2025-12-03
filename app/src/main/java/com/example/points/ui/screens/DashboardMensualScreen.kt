package com.example.points.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import com.example.points.data.model.DatosPorMes
import com.github.tehras.charts.bar.BarChart
import com.github.tehras.charts.bar.BarChartData
import com.github.tehras.charts.bar.renderer.label.SimpleValueDrawer

@Composable
fun DashboardMensualScreen(data: List<DatosPorMes>) {
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
                text = "Distribución por Mes",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 20.dp),
                textAlign = TextAlign.Center
            )
            BarrasMensuales(data)
            Spacer(modifier = Modifier.height(20.dp))
            LeyendaMensual()
        }
    }
}

@Composable
fun BarrasMensuales(data: List<DatosPorMes>) {
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
    
    // Colores para cada tipo de dato
    val colorIncidentes = androidx.compose.ui.graphics.Color(0xFFF44336) // Rojo
    val colorEventos = androidx.compose.ui.graphics.Color(0xFF2196F3) // Azul
    val colorPOIs = androidx.compose.ui.graphics.Color(0xFF4CAF50) // Verde
    
    // Crear barras agrupadas por mes
    // Para cada mes, creamos 3 barras (incidentes, eventos, POIs)
    data.forEach { datosMes ->
        // Usar solo el mes sin año para simplificar
        val mesCorto = datosMes.mes.split(" ").firstOrNull() ?: datosMes.mes
        
        // Barra de incidentes
        barras.add(
            BarChartData.Bar(
                label = "I",
                value = datosMes.incidentes.toFloat(),
                color = colorIncidentes
            )
        )
        // Barra de eventos
        barras.add(
            BarChartData.Bar(
                label = "E",
                value = datosMes.eventos.toFloat(),
                color = colorEventos
            )
        )
        // Barra de POIs
        barras.add(
            BarChartData.Bar(
                label = "P",
                value = datosMes.pois.toFloat(),
                color = colorPOIs
            )
        )
    }
    
    // Crear un diseño horizontal scrollable para evitar sobreposición
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Mostrar los meses como etiquetas separadas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
        ) {
            data.forEach { datosMes ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(80.dp)
                        .padding(horizontal = 2.dp)
                ) {
                    Text(
                        text = datosMes.mes,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        modifier = Modifier.padding(bottom = 4.dp)
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
        
        // Etiquetas de los meses debajo del gráfico
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
        ) {
            data.forEachIndexed { index, datosMes ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(80.dp)
                        .padding(horizontal = 2.dp)
                ) {
                    Text(
                        text = datosMes.mes,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "I:${datosMes.incidentes} E:${datosMes.eventos} P:${datosMes.pois}",
                        fontSize = 9.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun LeyendaMensual() {
    val colorIncidentes = androidx.compose.ui.graphics.Color(0xFFF44336)
    val colorEventos = androidx.compose.ui.graphics.Color(0xFF2196F3)
    val colorPOIs = androidx.compose.ui.graphics.Color(0xFF4CAF50)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ItemLeyendaMensual("Incidentes", colorIncidentes)
        Spacer(modifier = Modifier.width(24.dp))
        ItemLeyendaMensual("Eventos", colorEventos)
        Spacer(modifier = Modifier.width(24.dp))
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

