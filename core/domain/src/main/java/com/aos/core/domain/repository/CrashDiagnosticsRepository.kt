package com.aos.core.domain.repository

import com.aos.core.domain.model.CrashReport

interface CrashDiagnosticsRepository {
    fun getCrashReports(): List<CrashReport>
    fun clearCrashReports()
}
