package com.example.evaluacion3

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class HistorialDatos : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activity = this as Activity

        setContent {
            val lecturas = remember { mutableStateOf(HistorialManager.obtenerLecturas()) }

            LaunchedEffect(Unit) {
                while (true) {
                    kotlinx.coroutines.delay(1000)
                    lecturas.value = HistorialManager.obtenerLecturas()
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(240, 245, 255))
                    .padding(16.dp)
            ) {
                Text(
                    text = "📈 Historial de Datos",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(33, 150, 243),
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                if (lecturas.value.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "📊 Estadísticas Generales",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "🌡️ Temperatura",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(213, 0, 0)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Promedio: ${String.format("%.1f°C", HistorialManager.obtenerPromedioTemperatura())}",
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "Máxima: ${String.format("%.1f°C", HistorialManager.obtenerTemperaturaMaxima())}",
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "Mínima: ${String.format("%.1f°C", HistorialManager.obtenerTemperaturaMinima())}",
                                        fontSize = 12.sp
                                    )
                                }

                                Divider(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(80.dp),
                                    color = Color.LightGray
                                )

                                Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                                    Text(
                                        text = "💧 Humedad",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(3, 169, 244)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Promedio: ${String.format("%.1f%%", HistorialManager.obtenerPromedioHumedad())}",
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "Máxima: ${String.format("%.1f%%", HistorialManager.obtenerHumedadMaxima())}",
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "Mínima: ${String.format("%.1f%%", HistorialManager.obtenerHumedadMinima())}",
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Total de lecturas: ${HistorialManager.cantidadLecturas()}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Últimas Lecturas",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = {
                                HistorialManager.limpiarHistorial()
                                lecturas.value = emptyList()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(244, 67, 54)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("🗑️ Limpiar", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(lecturas.value) { lectura ->
                            LecturaItem(lectura)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📭",
                                fontSize = 64.sp
                            )
                            Text(
                                text = "No hay datos registrados",
                                fontSize = 18.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "Inicie el monitor para comenzar a recopilar datos",
                                fontSize = 14.sp,
                                color = Color.LightGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { activity.finish() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(156, 39, 176))
                ) {
                    Text("⬅️ Volver al Menú Principal", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun LecturaItem(lectura: LecturaSensor) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lectura.fecha,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .background(Color(255, 240, 240), RoundedCornerShape(4.dp))
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🌡️",
                    fontSize = 16.sp
                )
                Text(
                    text = String.format("%.1f°C", lectura.temperatura),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(213, 0, 0)
                )
            }

            Column(
                modifier = Modifier
                    .background(Color(240, 248, 255), RoundedCornerShape(4.dp))
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "💧",
                    fontSize = 16.sp
                )
                Text(
                    text = String.format("%.1f%%", lectura.humedad),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(3, 169, 244)
                )
            }
        }
    }
}