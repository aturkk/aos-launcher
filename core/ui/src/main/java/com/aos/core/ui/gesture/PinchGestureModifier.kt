package com.aos.core.ui.gesture

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.launcherGestures(
    onDoubleTap: () -> Unit = {},
    onPinchIn: () -> Unit = {}
): Modifier = this
    .pointerInput(Unit) {
        detectTapGestures(
            onDoubleTap = { onDoubleTap() }
        )
    }
    .pointerInput(Unit) {
        detectTransformGestures { _, _, zoom, _ ->
            if (zoom < 0.82f) {
                onPinchIn()
            }
        }
    }
