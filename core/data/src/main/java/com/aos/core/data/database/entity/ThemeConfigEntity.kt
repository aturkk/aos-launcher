package com.aos.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "theme_config")
data class ThemeConfigEntity(
    @PrimaryKey
    val id: Int = 1,
    val darkModeOption: String = "System",
    val useDynamicColors: Boolean = true,
    val primaryColorArgb: Long = 0xFF6200EE,
    val iconShape: String = "Squircle",
    val animationSpeedMultiplier: Float = 1.0f
)
