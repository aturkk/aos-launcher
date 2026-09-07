package com.aos.core.data.repository

import com.aos.core.data.preferences.UserPreferencesDataStore
import com.aos.core.domain.model.IconShapeOption
import com.aos.core.domain.model.PageTransitionEffect
import com.aos.core.domain.model.SearchEngineOption
import com.aos.core.domain.model.ThemeConfig
import com.aos.core.domain.repository.UserPreferences
import com.aos.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: UserPreferencesDataStore
) : UserPreferencesRepository {

    override val userPreferences: Flow<UserPreferences> = dataStore.userPreferences

    override suspend fun setGridDimensions(rows: Int, columns: Int) {
        dataStore.setGridDimensions(rows, columns)
    }

    override suspend fun setShowAppLabels(show: Boolean) {
        dataStore.setShowAppLabels(show)
    }

    override suspend fun setDoubleTapToSleep(enabled: Boolean) {
        dataStore.setDoubleTapToSleep(enabled)
    }

    override suspend fun updateThemeConfig(config: ThemeConfig) {
        dataStore.updateThemeConfig(config)
    }

    override suspend fun setIconShape(shape: IconShapeOption) {
        dataStore.setIconShape(shape)
    }

    override suspend fun setPageTransition(effect: PageTransitionEffect) {
        dataStore.setPageTransition(effect)
    }

    override suspend fun setIconPack(packageName: String?) {
        dataStore.setIconPack(packageName)
    }

    override suspend fun setParallaxEnabled(enabled: Boolean) {
        dataStore.setParallaxEnabled(enabled)
    }

    override suspend fun setShowNotificationBadges(show: Boolean) {
        dataStore.setShowNotificationBadges(show)
    }

    override suspend fun setSearchEngine(engine: SearchEngineOption) {
        dataStore.setSearchEngine(engine)
    }

    override suspend fun setMathCalculatorEnabled(enabled: Boolean) {
        dataStore.setMathCalculatorEnabled(enabled)
    }

    override suspend fun setContactsSearchEnabled(enabled: Boolean) {
        dataStore.setContactsSearchEnabled(enabled)
    }

    override suspend fun setAiSearchChipsEnabled(enabled: Boolean) {
        dataStore.setAiSearchChipsEnabled(enabled)
    }

    override suspend fun setNewsFeedEnabled(enabled: Boolean) {
        dataStore.setNewsFeedEnabled(enabled)
    }

    override suspend fun setWidgetPageEnabled(enabled: Boolean) {
        dataStore.setWidgetPageEnabled(enabled)
    }

    override suspend fun setHomeLayoutMode(mode: com.aos.core.domain.model.HomeLayoutMode) {
        dataStore.setHomeLayoutMode(mode.name)
    }

    override suspend fun setSearchBarAtBottom(atBottom: Boolean) {
        dataStore.setSearchBarAtBottom(atBottom)
    }

    override suspend fun setClockWidgetEnabled(enabled: Boolean) {
        dataStore.setClockWidgetEnabled(enabled)
    }

    override suspend fun setSmartContextCardEnabled(enabled: Boolean) {
        dataStore.setSmartContextCardEnabled(enabled)
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.setOnboardingCompleted(completed)
    }

    override suspend fun setHideStatusBar(hide: Boolean) {
        dataStore.setHideStatusBar(hide)
    }

    override suspend fun setHideNavigationBar(hide: Boolean) {
        dataStore.setHideNavigationBar(hide)
    }

    override suspend fun setBlurDepth(depth: Float) {
        dataStore.setBlurDepth(depth)
    }
}
