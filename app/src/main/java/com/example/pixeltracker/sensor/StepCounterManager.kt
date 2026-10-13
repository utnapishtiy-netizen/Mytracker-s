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
    private val sharedPrefs = context.getSharedPreferences("step_counter_prefs", Context.MODE_PRIVATE)

    private val _stepCount = MutableStateFlow(0)
    val stepCount: StateFlow<Int> = _stepCount

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

            val savedDate = sharedPrefs.getString("saved_date", "")
            var initialSteps = sharedPrefs.getInt("initial_steps", -1)

            if (currentDate != savedDate || initialSteps == -1) {
                initialSteps = totalStepsFromBoot
                sharedPrefs.edit()
                    .putString("saved_date", currentDate)
                    .putInt("initial_steps", initialSteps)
                    .apply()
            }

            if (totalStepsFromBoot < initialSteps) {
                initialSteps = totalStepsFromBoot
                sharedPrefs.edit()
                    .putInt("initial_steps", initialSteps)
                    .apply()
            }

            val stepsToday = maxOf(0, totalStepsFromBoot - initialSteps)
            _stepCount.value = stepsToday
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
