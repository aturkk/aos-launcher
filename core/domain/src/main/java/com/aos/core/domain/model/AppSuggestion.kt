package com.aos.core.domain.model

data class AppSuggestion(
    val packageName: String,
    val activityName: String = "",
    val label: String,
    val score: Float = 0f,
    val reason: String = "Önerilen"
)
