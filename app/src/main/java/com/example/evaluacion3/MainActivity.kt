package com.example.evaluacion3

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.getSystemService
import java.util.UUID

// Socket Bluetooth global para compartir entre actividades
var btGlobalSocket: BluetoothSocket? = null

class MainActivity : ComponentActivity() {
    private var btsocket: BluetoothSocket? = null
    private var stopThread: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context: Context = LocalContext.current
            val activity: Activity = this

            var statusBT by remember { mutableStateOf("Bluetooth: Desconectado") }
            var colorBT by remember { mutableStateOf(Color(255, 150, 150)) }
            //var statusBT by remember { mutableStateOf("BT: Off") }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(240, 245, 255))
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "EVALUACIÓN 3",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(33, 150, 243)
                )
                Text(
                    text = "Monitor de Temperatura y Humedad",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 40.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = colorBT),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = statusBT,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val connectionStatus = conectarDispositivoABluetooth(context, activity)
                        //println("Estado conexion: $connectionStatus")

                        if (connectionStatus == false || btsocket?.isConnected == false) {
                            statusBT = "Bluetooth: Desconectado"
                            colorBT = Color(255, 150, 150)
                            Toast.makeText(context, "No se pudo conectar", Toast.LENGTH_SHORT).show()
                        } else {
                            statusBT = "Bluetooth: Conectado"
                            colorBT = Color(150, 255, 150)
                            Toast.makeText(context, "Conectado exitosamente", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(76, 175, 80))
                ) {
                    Text(" CONECTAR BLUETOOTH", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = "Opciones Disponibles:",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = {
                        if (btsocket?.isConnected == true) {
                            stopThread = true
                            val intent1 = Intent(context, MonitorTiempoReal::class.java)
                            btGlobalSocket = btsocket
                            context.startActivity(intent1)
                        } else {
                            Toast.makeText(context, "Conecte Bluetooth primero", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(33, 150, 243))
                ) {
                    Text(" Monitor en Tiempo Real", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        stopThread = true
                        val intent2 = Intent(context, HistorialDatos::class.java)
                        context.startActivity(intent2)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(156, 39, 176))
                ) {
                    Text(" Historial de Datos", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        stopThread = true
                        val intent3 = Intent(context, ConfiguracionApp::class.java)
                        context.startActivity(intent3)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(255, 152, 0))
                ) {
                    Text(" Configuración / ThingSpeak", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "Sensor: DHT11/DHT22",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Protocolo: HC-05 Bluetooth",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }

    @RequiresPermission(allOf = ["android.permission.BLUETOOTH_SCAN", "android.permission.BLUETOOTH_CONNECT"])
    fun conectarDispositivoABluetooth(con: Context, act: Activity): Boolean {
        val bluetoothManager: BluetoothManager? = getSystemService(con, BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

        if (bluetoothAdapter == null) {
            Toast.makeText(con, "Bluetooth no soportado", Toast.LENGTH_SHORT).show()
            return false
        }

        if (btsocket?.isConnected == true) {
            btsocket?.close()
            return false
        }

        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(act, enableBtIntent, 1, null)
        }

        //var deviceCount = 0
        bluetoothAdapter.bondedDevices.forEach { device ->
            //println("Dispositivo: ${device.name} - ${device.address}")
            //deviceCount++

            if (device.name == "HC-05" || device.name == "HC-05-1") {
                val btdev: BluetoothDevice = bluetoothAdapter.getRemoteDevice(device.address)
                val btuuid: String = btdev.uuids[0].toString()
                val btuuid1: UUID = UUID.fromString(btuuid)

                btsocket = device.createRfcommSocketToServiceRecord(btuuid1)

                try {
                    btsocket?.connect()
                    btGlobalSocket = btsocket

                    Toast.makeText(
                        con,
                        "Conectado a ${device.name}",
                        Toast.LENGTH_LONG
                    ).show()
                    return true

                } catch (e: Exception) {
                    //println("Error conexión: ${e.message}")
                    Toast.makeText(
                        con,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    return false
                }
            }
        }

        Toast.makeText(con, "Dispositivo HC-05 no encontrado", Toast.LENGTH_LONG).show()
        return false
    }
}
