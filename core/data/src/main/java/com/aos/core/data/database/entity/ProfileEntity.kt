package com.aos.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val type: String,
    val isActive: Boolean = false,
    val blockedPackagesJson: String = "[]",
    val allowedPackagesJson: String = "[]",
    val pinCode: String? = null,
    val isScheduleEnabled: Boolean = false,
    val startHour: Int = 9,
    val startMinute: Int = 0,
    val endHour: Int = 18,
    val endMinute: Int = 0,
    val iconName: String = "default"
)
