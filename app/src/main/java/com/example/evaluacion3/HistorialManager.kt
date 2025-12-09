package com.example.evaluacion3

import java.text.SimpleDateFormat
import java.util.*

data class LecturaSensor(
    val temperatura: Float,
    val humedad: Float,
    val timestamp: Long,
    val fecha: String
)

object HistorialManager {
    private val lecturas = mutableListOf<LecturaSensor>()
    private val maxLecturas = 100

    fun agregarLectura(temperatura: Float, humedad: Float) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val fechaFormateada = sdf.format(Date())

        val lectura = LecturaSensor(
            temperatura = temperatura,
            humedad = humedad,
            timestamp = System.currentTimeMillis(),
            fecha = fechaFormateada
        )

        lecturas.add(0, lectura)

        if (lecturas.size > maxLecturas) {
            lecturas.removeAt(lecturas.size - 1)
        }
    }

    fun obtenerLecturas(): List<LecturaSensor> {
        return lecturas.toList()
    }

    fun obtenerUltimaLectura(): LecturaSensor? {
        return lecturas.firstOrNull()
    }

    fun obtenerPromedioTemperatura(): Float {
        if (lecturas.isEmpty()) return 0f
        return lecturas.map { it.temperatura }.average().toFloat()
    }

    fun obtenerPromedioHumedad(): Float {
        if (lecturas.isEmpty()) return 0f
        return lecturas.map { it.humedad }.average().toFloat()
    }

    fun obtenerTemperaturaMaxima(): Float {
        if (lecturas.isEmpty()) return 0f
        return lecturas.maxOf { it.temperatura }
    }

    fun obtenerTemperaturaMinima(): Float {
        if (lecturas.isEmpty()) return 0f
        return lecturas.minOf { it.temperatura }
    }

    fun obtenerHumedadMaxima(): Float {
        if (lecturas.isEmpty()) return 0f
        return lecturas.maxOf { it.humedad }
    }

    fun obtenerHumedadMinima(): Float {
        if (lecturas.isEmpty()) return 0f
        return lecturas.minOf { it.humedad }
    }

    fun limpiarHistorial() {
        lecturas.clear()
    }

    fun cantidadLecturas(): Int {
        return lecturas.size
    }
}