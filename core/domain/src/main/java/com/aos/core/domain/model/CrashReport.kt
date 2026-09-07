package com.aos.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CrashReport(
    val id: String,
    val timestamp: Long,
    val exceptionType: String,
    val message: String,
    val stackTrace: String,
    val deviceInfo: String
)
