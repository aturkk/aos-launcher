package com.aos.core.ui.animation

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import com.aos.core.domain.model.PageTransitionEffect
import kotlin.math.abs

fun Modifier.pageTransitionEffect(
    pageOffset: Float,
    effect: PageTransitionEffect
): Modifier = this.graphicsLayer {
    if (pageOffset == 0f || effect == PageTransitionEffect.Standard) {
        return@graphicsLayer
    }

    when (effect) {
        PageTransitionEffect.Cube -> {
            cameraDistance = 16f * density
            transformOrigin = TransformOrigin(
                pivotFractionX = if (pageOffset > 0f) 0f else 1f,
                pivotFractionY = 0.5f
            )
            rotationY = pageOffset * 90f
            alpha = (1f - abs(pageOffset) * 0.25f).coerceIn(0f, 1f)
        }

        PageTransitionEffect.Depth -> {
            val scale = (1f - abs(pageOffset) * 0.25f).coerceIn(0.6f, 1f)
            scaleX = scale
            scaleY = scale
            alpha = (1f - abs(pageOffset) * 0.5f).coerceIn(0f, 1f)
        }

        PageTransitionEffect.Flip -> {
            cameraDistance = 16f * density
            rotationY = pageOffset * 180f
            alpha = if (abs(pageOffset) > 0.5f) 0f else 1f
        }

        PageTransitionEffect.Accordion -> {
            transformOrigin = TransformOrigin(
                pivotFractionX = if (pageOffset > 0f) 0f else 1f,
                pivotFractionY = 0.5f
            )
            scaleX = (1f - abs(pageOffset) * 0.5f).coerceIn(0.2f, 1f)
            alpha = (1f - abs(pageOffset) * 0.3f).coerceIn(0f, 1f)
        }

        PageTransitionEffect.Standard -> {
            // Default
        }
    }
}
