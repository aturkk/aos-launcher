package com.aos.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_launch_events")
data class AppLaunchEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val packageName: String,
    val timestamp: Long,
    val hourOfDay: Int,
    val dayOfWeek: Int
)
