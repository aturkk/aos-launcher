package com.aos.core.domain.repository

import com.aos.core.domain.model.IconShapeOption
import com.aos.core.domain.model.PageTransitionEffect
import com.aos.core.domain.model.SearchEngineOption
import com.aos.core.domain.model.ThemeConfig
import kotlinx.coroutines.flow.Flow

data class UserPreferences(
    val gridRows: Int = 5,
    val gridColumns: Int = 4,
    val showAppLabels: Boolean = true,
    val doubleTapToSleep: Boolean = true,
    val themeConfig: ThemeConfig = ThemeConfig(),
    val isOnboardingCompleted: Boolean = false
)

interface UserPreferencesRepository {
    val userPreferences: Flow<UserPreferences>
    suspend fun setGridDimensions(rows: Int, columns: Int)
    suspend fun setShowAppLabels(show: Boolean)
    suspend fun setDoubleTapToSleep(enabled: Boolean)
    suspend fun updateThemeConfig(config: ThemeConfig)
    suspend fun setIconShape(shape: IconShapeOption)
    suspend fun setPageTransition(effect: PageTransitionEffect)
    suspend fun setIconPack(packageName: String?)
    suspend fun setParallaxEnabled(enabled: Boolean)
    suspend fun setShowNotificationBadges(show: Boolean)
    suspend fun setSearchEngine(engine: SearchEngineOption)
    suspend fun setOnboardingCompleted(completed: Boolean)
}
