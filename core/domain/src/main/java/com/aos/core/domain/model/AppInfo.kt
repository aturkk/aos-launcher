package com.aos.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AppInfo(
    val packageName: String,
    val activityName: String,
    val label: String,
    val installTime: Long = 0L,
    val isSystemApp: Boolean = false,
    val category: AppCategory = AppCategory.Other
)
