package com.aos.core.domain.model

import com.aos.core.domain.repository.UserPreferences

data class BackupPayload(
    val version: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val deviceName: String = "AOS Device",
    val pages: List<PageInfo> = emptyList(),
    val items: List<LauncherItem> = emptyList(),
    val profiles: List<Profile> = emptyList(),
    val themeConfig: ThemeConfig = ThemeConfig(),
    val userPreferences: UserPreferences = UserPreferences()
)

sealed class SyncStatus {
    object Idle : SyncStatus()
    object Syncing : SyncStatus()
    data class Success(val lastSyncTime: Long) : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}
