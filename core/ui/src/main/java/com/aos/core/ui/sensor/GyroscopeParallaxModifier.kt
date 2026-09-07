package com.aos.core.ui.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.parallaxSensorEffect(
    enabled: Boolean = true,
    maxOffsetDp: Dp = 10.dp
): Modifier = composed {
    if (!enabled) return@composed this

    val context = LocalContext.current
    var tiltX by remember { mutableFloatStateOf(0f) }
    var tiltY by remember { mutableFloatStateOf(0f) }

    val animatedTiltX by animateFloatAsState(
        targetValue = tiltX,
        animationSpec = tween(durationMillis = 150),
        label = "parallaxX"
    )
    val animatedTiltY by animateFloatAsState(
        targetValue = tiltY,
        animationSpec = tween(durationMillis = 150),
        label = "parallaxY"
    )

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null && event.values.size >= 2) {
                    // Normalize accelerometer readings (-9.8 to 9.8) to -1f..1f
                    val rawX = -event.values[0] / 9.8f
                    val rawY = event.values[1] / 9.8f
                    tiltX = rawX.coerceIn(-1f, 1f)
                    tiltY = rawY.coerceIn(-1f, 1f)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager?.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    this.offset(
        x = maxOffsetDp * animatedTiltX,
        y = maxOffsetDp * animatedTiltY
    )
}
