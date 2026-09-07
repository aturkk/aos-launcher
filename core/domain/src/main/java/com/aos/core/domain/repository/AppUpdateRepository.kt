package com.aos.core.domain.repository

import com.aos.core.common.result.Result
import com.aos.core.domain.model.AppUpdateInfo
import com.aos.core.domain.model.UpdateStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface AppUpdateRepository {
    val updateStatus: StateFlow<UpdateStatus>
    val isAutoUpdateCheckEnabled: Flow<Boolean>
    suspend fun setAutoUpdateCheckEnabled(enabled: Boolean)
    suspend fun checkForUpdates(): Result<AppUpdateInfo>
    suspend fun downloadAndInstallUpdate(updateInfo: AppUpdateInfo)
    fun installApk(apkFile: File)
    fun resetUpdateStatus()
}
