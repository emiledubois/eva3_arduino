package com.example.evaluacion3

import android.app.Activity
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
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.concurrent.thread

class ConfiguracionApp : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val activity = this as Activity

        setContent {
            var apiKey by remember { mutableStateOf("") }
            var intervalo by remember { mutableStateOf(60) }
            var autoEnvio by remember { mutableStateOf(false) }
            var estadoEnvio by remember { mutableStateOf("Listo para enviar") }
            var ultimoEnvio by remember { mutableStateOf("Nunca") }
            //var contadorEnvios by remember { mutableStateOf(0) }

            LaunchedEffect(autoEnvio, intervalo) {
                while (autoEnvio) {
                    val lectura = HistorialManager.obtenerUltimaLectura()
                    if (lectura != null && apiKey.isNotBlank()) {
                        val resultado = enviarDatosThingSpeak(
                            apiKey,
                            lectura.temperatura,
                            lectura.humedad
                        )

                        estadoEnvio = if (resultado) {
                            ultimoEnvio = lectura.fecha
                            "✅ Datos enviados correctamente"
                        } else {
                            "❌ Error al enviar datos"
                        }
                    }

                    kotlinx.coroutines.delay((intervalo * 1000).toLong())
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(240, 245, 255))
                    .padding(16.dp)
            ) {
                Text(
                    text = "⚙️ Configuración",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(33, 150, 243),
                    modifier = Modifier.padding(vertical = 16.dp)
                )

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
                            text = "☁️ ThingSpeak IoT",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Text(
                            text = "API Key de ThingSpeak:",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        TextField(
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Ingrese su API Key") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(250, 250, 250),
                                unfocusedContainerColor = Color(250, 250, 250)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Intervalo de envío (segundos):",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Slider(
                                value = intervalo.toFloat(),
                                onValueChange = { intervalo = it.toInt() },
                                valueRange = 15f..300f,
                                steps = 18,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "$intervalo seg",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Envío automático:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Switch(
                                checked = autoEnvio,
                                onCheckedChange = {
                                    if (apiKey.isBlank()) {
                                        Toast.makeText(
                                            activity,
                                            "Configure la API Key primero",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        autoEnvio = it
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Estado: $estadoEnvio",
                            fontSize = 12.sp,
                            color = if (estadoEnvio.contains("✅")) Color(76, 175, 80) else Color.Gray
                        )
                        Text(
                            text = "Último envío: $ultimoEnvio",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (apiKey.isBlank()) {
                                    Toast.makeText(
                                        activity,
                                        "Configure la API Key primero",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }

                                val lectura = HistorialManager.obtenerUltimaLectura()
                                if (lectura == null) {
                                    Toast.makeText(
                                        activity,
                                        "No hay datos para enviar",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }

                                estadoEnvio = "⏳ Enviando..."
                                //println("Enviando T:${lectura.temperatura}, H:${lectura.humedad}")

                                thread {
                                    val resultado = enviarDatosThingSpeak(
                                        apiKey,
                                        lectura.temperatura,
                                        lectura.humedad
                                    )

                                    activity.runOnUiThread {
                                        estadoEnvio = if (resultado) {
                                            ultimoEnvio = lectura.fecha
                                            Toast.makeText(
                                                activity,
                                                "Datos enviados correctamente",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            "✅ Datos enviados correctamente"
                                        } else {
                                            Toast.makeText(
                                                activity,
                                                "Error al enviar datos",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            "❌ Error al enviar datos"
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(255, 152, 0))
                        ) {
                            Text("📤 Enviar Datos Ahora", fontSize = 16.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(255, 248, 225)),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "ℹ️ Información",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "• Obtenga su API Key en ThingSpeak.com",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "• El campo 1 envía Temperatura",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "• El campo 2 envía Humedad",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "• Requiere permiso de INTERNET en AndroidManifest.xml",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        autoEnvio = false
                        activity.finish()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(156, 39, 176))
                ) {
                    Text("⬅️ Volver al Menú Principal", fontSize = 16.sp)
                }
            }
        }
    }

    private fun enviarDatosThingSpeak(
        apiKey: String,
        temperatura: Float,
        humedad: Float
    ): Boolean {
        return try {
            val urlString = "https://api.thingspeak.com/update?api_key=$apiKey&field1=$temperatura&field2=$humedad"
            //println("URL: $urlString")
            val url = URL(urlString)
            val connection = url.openConnection() as HttpsURLConnection

            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.connect()

            val responseCode = connection.responseCode
            //println("Response code: $responseCode")

            if (responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                response.toIntOrNull() != null && response.toInt() > 0
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}