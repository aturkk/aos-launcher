package com.aos.feature.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aos.core.domain.model.AppUpdateInfo
import com.aos.core.domain.model.CrashReport
import com.aos.core.domain.model.UpdateStatus
import java.io.File

@Composable
fun UpdatesAndAboutSettingsContent(
    updateStatus: UpdateStatus,
    isAutoUpdateEnabled: Boolean,
    crashReports: List<CrashReport>,
    onCheckForUpdates: () -> Unit,
    onDownloadAndInstall: (AppUpdateInfo) -> Unit,
    onInstallApk: (File) -> Unit,
    onToggleAutoUpdate: (Boolean) -> Unit,
    onClearCrashReports: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // GitHub Auto-Update Section
        AppUpdateSection(
            updateStatus = updateStatus,
            isAutoUpdateEnabled = isAutoUpdateEnabled,
            onCheckForUpdates = onCheckForUpdates,
            onDownloadAndInstall = onDownloadAndInstall,
            onInstallApk = onInstallApk,
            onToggleAutoUpdate = onToggleAutoUpdate
        )

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(24.dp))

        // About & Diagnostics Section
        AboutAndDiagnosticsSection(
            crashReports = crashReports,
            onClearCrashReports = onClearCrashReports
        )
    }
}
