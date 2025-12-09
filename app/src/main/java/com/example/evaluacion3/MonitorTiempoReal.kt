package com.example.evaluacion3

import android.app.Activity
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class MonitorTiempoReal : ComponentActivity() {
    private var stopThread = false
    private var dataReadThread: Thread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activity = this as Activity

        setContent {
            var temp by remember { mutableStateOf(0.0f) }
            var hum by remember { mutableStateOf(0.0f) }
            var ultimaLectura by remember { mutableStateOf("--:--:--") }
            var estado by remember { mutableStateOf("Esperando datos...") }
            var contador by remember { mutableStateOf(0) }
            //var lecturaEnProceso by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                while (true) {
                    if (btGlobalSocket?.isConnected == true) {
                        try {
                            btGlobalSocket?.outputStream?.write("@T".toByteArray(Charsets.US_ASCII))
                            delay(100)

                            val datos = leerDatosSensor(btGlobalSocket)
                            if (datos != null) {
                                temp = datos.first
                                hum = datos.second

                                val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                ultimaLectura = sdf.format(Date())
                                contador++
                                estado = "Lectura exitosa #$contador"
                                //println("Temp: $temp, Hum: $hum")

                                HistorialManager.agregarLectura(temp, hum)
                            } else {
                                estado = "Error en lectura"
                            }
                        } catch (e: Exception) {
                            estado = "Error: ${e.message}"
                        }
                    } else {
                        estado = "Bluetooth desconectado"
                    }

                    delay(3000)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(240, 245, 255))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Monitor en Tiempo Real",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(33, 150, 243),
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            temp > 30f -> Color(255, 200, 200)
                            temp < 15f -> Color(200, 220, 255)
                            else -> Color(200, 255, 200)
                        }
                    ),
                    elevation = CardDefaults.cardElevation(8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🌡️ TEMPERATURA",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format("%.1f°C", temp),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(213, 0, 0)
                        )
                        Text(
                            text = obtenerEstadoTemperatura(temp),
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            hum > 70f -> Color(200, 220, 255)
                            hum < 30f -> Color(255, 240, 200)
                            else -> Color(220, 255, 220)
                        }
                    ),
                    elevation = CardDefaults.cardElevation(8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "💧 HUMEDAD",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format("%.1f%%", hum),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(3, 169, 244)
                        )
                        Text(
                            text = obtenerEstadoHumedad(hum),
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "📡 Estado: $estado",
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🕐 Última actualización: $ultimaLectura",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "📊 Lecturas realizadas: $contador",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            try {
                                btGlobalSocket?.outputStream?.write("@T".toByteArray(Charsets.US_ASCII))
                            } catch (e: Exception) {
                                Toast.makeText(activity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(76, 175, 80))
                    ) {
                        Text("🔄 Actualizar")
                    }

                    Button(
                        onClick = { activity.finish() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(244, 67, 54))
                    ) {
                        Text("⬅️ Volver")
                    }
                }
            }
        }
    }

    private fun leerDatosSensor(socket: BluetoothSocket?): Pair<Float, Float>? {
        if (socket?.isConnected != true) return null

        try {
            Thread.sleep(200)

            if (socket.inputStream.available() > 0) {
                val buffer = ByteArray(32)
                val bytesRead = socket.inputStream.read(buffer)
                val response = String(buffer, 0, bytesRead, Charsets.US_ASCII)
                //println("Respuesta BT: $response")

                val tempMatch = Regex("T:([0-9.]+)").find(response)
                val humMatch = Regex("H:([0-9.]+)").find(response)

                if (tempMatch != null && humMatch != null) {
                    val temp = tempMatch.groupValues[1].toFloat()
                    val hum = humMatch.groupValues[1].toFloat()
                    return Pair(temp, hum)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    private fun obtenerEstadoTemperatura(temp: Float): String {
        return when {
            temp < 10f -> "Muy frío"
            temp < 18f -> "Frío"
            temp < 25f -> "Confortable"
            temp < 30f -> "Cálido"
            else -> "Calor"
        }
    }

    private fun obtenerEstadoHumedad(hum: Float): String {
        return when {
            hum < 30f -> "Ambiente seco"
            hum < 50f -> "Humedad baja"
            hum < 70f -> "Humedad óptima"
            else -> "Muy húmedo"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopThread = true
        dataReadThread?.interrupt()
    }
}