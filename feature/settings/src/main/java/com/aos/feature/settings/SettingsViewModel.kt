package com.aos.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aos.core.common.result.Result
import com.aos.core.domain.model.DarkModeOption
import com.aos.core.domain.model.IconPack
import com.aos.core.domain.model.IconShapeOption
import com.aos.core.domain.model.PageTransitionEffect
import com.aos.core.domain.model.Profile
import com.aos.core.domain.model.SearchEngineOption
import com.aos.core.domain.model.SyncStatus
import com.aos.core.domain.model.AosPlugin
import com.aos.core.domain.repository.AiSuggestionRepository
import com.aos.core.domain.repository.BackupRepository
import com.aos.core.domain.repository.IconPackRepository
import com.aos.core.domain.repository.PluginRepository
import com.aos.core.domain.repository.ProfileRepository
import com.aos.core.domain.repository.UserPreferences
import com.aos.core.domain.repository.UserPreferencesRepository
import com.aos.core.domain.model.NotificationRecord
import com.aos.core.domain.repository.NotificationHistoryRepository
import com.aos.core.domain.model.CrashReport
import com.aos.core.domain.repository.CrashDiagnosticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val iconPackRepository: IconPackRepository,
    private val profileRepository: ProfileRepository,
    private val aiSuggestionRepository: AiSuggestionRepository,
    private val backupRepository: BackupRepository,
    private val pluginRepository: PluginRepository,
    private val notificationHistoryRepository: NotificationHistoryRepository,
    private val crashDiagnosticsRepository: CrashDiagnosticsRepository
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = userPreferencesRepository.userPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    val notificationHistory: StateFlow<List<NotificationRecord>> = notificationHistoryRepository.getNotificationHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val plugins: StateFlow<List<AosPlugin>> = pluginRepository.getPlugins()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    val syncStatus: StateFlow<SyncStatus> = backupRepository.getSyncStatus()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SyncStatus.Idle
        )


    val profiles: StateFlow<List<Profile>> = profileRepository.getProfiles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isAiEnabled: StateFlow<Boolean> = aiSuggestionRepository.isAiSuggestionsEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    private val _installedIconPacks = MutableStateFlow<List<IconPack>>(emptyList())
    val installedIconPacks: StateFlow<List<IconPack>> = _installedIconPacks.asStateFlow()

    private val _crashReports = MutableStateFlow<List<CrashReport>>(emptyList())
    val crashReports: StateFlow<List<CrashReport>> = _crashReports.asStateFlow()

    init {
        loadInstalledIconPacks()
        loadCrashReports()
    }

    fun loadCrashReports() {
        _crashReports.value = crashDiagnosticsRepository.getCrashReports()
    }

    fun clearCrashReports() {
        crashDiagnosticsRepository.clearCrashReports()
        _crashReports.value = emptyList()
    }

    fun loadInstalledIconPacks() {
        viewModelScope.launch {
            _installedIconPacks.value = iconPackRepository.getInstalledIconPacks()
        }
    }

    fun hasUsageStatsPermission(): Boolean {
        return aiSuggestionRepository.hasUsagePermission()
    }

    fun setAiEnabled(enabled: Boolean) {
        viewModelScope.launch {
            aiSuggestionRepository.setAiSuggestionsEnabled(enabled)
        }
    }

    fun clearAiData() {
        viewModelScope.launch {
            aiSuggestionRepository.clearLearningData()
        }
    }

    fun switchProfile(profileId: Long) {
        viewModelScope.launch {
            profileRepository.switchProfile(profileId)
        }
    }

    fun updateProfile(profile: Profile) {
        viewModelScope.launch {
            profileRepository.updateProfile(profile)
        }
    }

    fun setGridDimensions(rows: Int, cols: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setGridDimensions(rows, cols)
        }
    }

    fun setShowAppLabels(show: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setShowAppLabels(show)
        }
    }

    fun setDoubleTapToSleep(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setDoubleTapToSleep(enabled)
        }
    }

    fun setIconShape(shape: IconShapeOption) {
        viewModelScope.launch {
            userPreferencesRepository.setIconShape(shape)
        }
    }

    fun setPageTransition(effect: PageTransitionEffect) {
        viewModelScope.launch {
            userPreferencesRepository.setPageTransition(effect)
        }
    }

    fun setIconPack(packageName: String?) {
        viewModelScope.launch {
            userPreferencesRepository.setIconPack(packageName)
        }
    }

    fun setDarkMode(mode: DarkModeOption) {
        viewModelScope.launch {
            val current = preferences.value.themeConfig
            userPreferencesRepository.updateThemeConfig(current.copy(darkMode = mode))
        }
    }

    fun setDynamicColors(enabled: Boolean) {
        viewModelScope.launch {
            val current = preferences.value.themeConfig
            userPreferencesRepository.updateThemeConfig(current.copy(useDynamicColors = enabled))
        }
    }

    fun setParallaxEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setParallaxEnabled(enabled)
        }
    }

    fun setShowNotificationBadges(show: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setShowNotificationBadges(show)
        }
    }

    fun setSearchEngine(engine: SearchEngineOption) {
        viewModelScope.launch {
            userPreferencesRepository.setSearchEngine(engine)
        }
    }

    suspend fun exportBackup(password: String): Result<ByteArray> {
        return backupRepository.exportEncryptedBackup(password)
    }

    suspend fun importBackup(data: ByteArray, password: String): Result<Unit> {
        return backupRepository.importEncryptedBackup(data, password)
    }

    fun triggerCloudSync() {
        viewModelScope.launch {
            backupRepository.syncWithCloud()
        }
    }

    fun togglePlugin(pluginId: String, enabled: Boolean) {
        viewModelScope.launch {
            pluginRepository.togglePlugin(pluginId, enabled)
        }
    }

    fun clearNotificationHistory() {
        viewModelScope.launch {
            notificationHistoryRepository.clearHistory()
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            notificationHistoryRepository.deleteNotification(id)
        }
    }
}
