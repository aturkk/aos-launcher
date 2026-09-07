package com.aos.core.data.repository

import com.aos.core.data.diagnostic.AosCrashReporter
import com.aos.core.domain.model.CrashReport
import com.aos.core.domain.repository.CrashDiagnosticsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrashDiagnosticsRepositoryImpl @Inject constructor() : CrashDiagnosticsRepository {
    override fun getCrashReports(): List<CrashReport> = AosCrashReporter.getReports()
    override fun clearCrashReports() = AosCrashReporter.clearReports()
}
