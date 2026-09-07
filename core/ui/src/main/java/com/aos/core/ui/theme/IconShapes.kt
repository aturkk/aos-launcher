package com.aos.core.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.aos.core.domain.model.IconShapeOption

val TeardropShape = RoundedCornerShape(
    topStart = 24.dp,
    topEnd = 24.dp,
    bottomEnd = 4.dp,
    bottomStart = 24.dp
)

val RoundedSquareShape = RoundedCornerShape(12.dp)

fun getIconShape(option: IconShapeOption): Shape = when (option) {
    IconShapeOption.Squircle -> SquircleShape
    IconShapeOption.Circle -> CircleShape
    IconShapeOption.RoundedSquare -> RoundedSquareShape
    IconShapeOption.Teardrop -> TeardropShape
}
