package com.aos.core.domain.model

data class NotificationRecord(
    val id: Long = 0L,
    val packageName: String,
    val appName: String,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
