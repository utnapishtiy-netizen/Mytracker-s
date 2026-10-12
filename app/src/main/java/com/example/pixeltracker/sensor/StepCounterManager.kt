package com.example.pixeltracker.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StepCounterManager(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private val _stepCount = MutableStateFlow(0)
    val stepCount: StateFlow<Int> = _stepCount

    // Пройденная дистанция в километрах, вычисляемая на основе системных шагов
    private val _distanceKm = MutableStateFlow(0.0f)
    val distanceKm: StateFlow<Float> = _distanceKm

    private var initialSteps = -1

    fun startListening() {
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalStepsFromSensor = event.values[0].toInt()
            if (initialSteps == -1) {
                initialSteps = totalStepsFromSensor
            }
            val currentSteps = (totalStepsFromSensor - initialSteps).coerceAtLeast(0)
            _stepCount.value = currentSteps
            
            // Расчет дистанции по стандарту Android (средняя длина шага ~0.75 метра)
            val distanceMeters = currentSteps * 0.75f
            _distanceKm.value = String.format("%.2f", distanceMeters / 1000f).replace(',', '.').toFloat()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
