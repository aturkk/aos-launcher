package com.aos.core.domain.model

import java.io.File

data class AppUpdateInfo(
    val currentVersion: String,
    val latestVersion: String,
    val isUpdateAvailable: Boolean,
    val releaseNotes: String,
    val apkDownloadUrl: String,
    val apkSize: Long,
    val publishedAt: String
)

sealed interface UpdateStatus {
    data object Idle : UpdateStatus
    data object Checking : UpdateStatus
    data class UpdateAvailable(val updateInfo: AppUpdateInfo) : UpdateStatus
    data class UpToDate(val currentVersion: String) : UpdateStatus
    data class Downloading(val progressPercent: Int, val downloadedBytes: Long, val totalBytes: Long) : UpdateStatus
    data class ReadyToInstall(val apkFile: File, val updateInfo: AppUpdateInfo) : UpdateStatus
    data class Error(val message: String) : UpdateStatus
}
