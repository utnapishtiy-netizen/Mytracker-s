package com.example.pixeltracker.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

class StepCounterManager(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private val _stepCount = MutableStateFlow(0)
    val stepCount: StateFlow<Int> = _stepCount

    private var initialSensorSteps = -1
    private var lastRecordedDate: String = LocalDate.now().toString()

    fun startListening() {
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    fun refresh() {
        val current = _stepCount.value
        _stepCount.value = current
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalStepsFromBoot = event.values[0].toInt()
            val currentDate = LocalDate.now().toString()

            if (currentDate != lastRecordedDate) {
                lastRecordedDate = currentDate
                initialSensorSteps = totalStepsFromBoot
            }

            if (initialSensorSteps == -1) {
                initialSensorSteps = totalStepsFromBoot
            }

            val stepsToday = maxOf(0, totalStepsFromBoot - initialSensorSteps)
            _stepCount.value = stepsToday
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
