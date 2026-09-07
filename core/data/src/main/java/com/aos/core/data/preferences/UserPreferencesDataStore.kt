package com.aos.core.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aos.core.domain.model.DarkModeOption
import com.aos.core.domain.model.IconShapeOption
import com.aos.core.domain.model.PageTransitionEffect
import com.aos.core.domain.model.SearchEngineOption
import com.aos.core.domain.model.ThemeConfig
import com.aos.core.domain.repository.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val GRID_ROWS = intPreferencesKey("grid_rows")
        val GRID_COLUMNS = intPreferencesKey("grid_columns")
        val SHOW_APP_LABELS = booleanPreferencesKey("show_app_labels")
        val DOUBLE_TAP_SLEEP = booleanPreferencesKey("double_tap_sleep")
        val DARK_MODE = stringPreferencesKey("dark_mode")
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        val ICON_SHAPE = stringPreferencesKey("icon_shape")
        val PAGE_TRANSITION = stringPreferencesKey("page_transition")
        val ICON_PACK = stringPreferencesKey("icon_pack")
        val PARALLAX_ENABLED = booleanPreferencesKey("parallax_enabled")
        val SHOW_NOTIFICATION_BADGES = booleanPreferencesKey("show_notification_badges")
        val SEARCH_ENGINE = stringPreferencesKey("search_engine")
        val AI_SUGGESTIONS_ENABLED = booleanPreferencesKey("ai_suggestions_enabled")
        val HIDDEN_PACKAGES = stringSetPreferencesKey("hidden_packages")
        val VAULT_PIN = stringPreferencesKey("vault_pin")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val AUTO_UPDATE_CHECK = booleanPreferencesKey("auto_update_check")
    }

    val userPreferences: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        val rows = preferences[PreferencesKeys.GRID_ROWS] ?: 5
        val cols = preferences[PreferencesKeys.GRID_COLUMNS] ?: 4
        val showLabels = preferences[PreferencesKeys.SHOW_APP_LABELS] ?: true
        val doubleTap = preferences[PreferencesKeys.DOUBLE_TAP_SLEEP] ?: true
        val darkModeStr = preferences[PreferencesKeys.DARK_MODE] ?: DarkModeOption.System.name
        val dynamicColors = preferences[PreferencesKeys.DYNAMIC_COLORS] ?: true
        val iconShapeStr = preferences[PreferencesKeys.ICON_SHAPE] ?: IconShapeOption.Squircle.name
        val transitionStr = preferences[PreferencesKeys.PAGE_TRANSITION] ?: PageTransitionEffect.Cube.name
        val iconPack = preferences[PreferencesKeys.ICON_PACK]
        val parallax = preferences[PreferencesKeys.PARALLAX_ENABLED] ?: true
        val badges = preferences[PreferencesKeys.SHOW_NOTIFICATION_BADGES] ?: true
        val searchEngineStr = preferences[PreferencesKeys.SEARCH_ENGINE] ?: SearchEngineOption.Google.name
        val onboardingCompleted = preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false

        UserPreferences(
            gridRows = rows,
            gridColumns = cols,
            showAppLabels = showLabels,
            doubleTapToSleep = doubleTap,
            themeConfig = ThemeConfig(
                darkMode = runCatching { DarkModeOption.valueOf(darkModeStr) }.getOrDefault(DarkModeOption.System),
                useDynamicColors = dynamicColors,
                iconShape = runCatching { IconShapeOption.valueOf(iconShapeStr) }.getOrDefault(IconShapeOption.Squircle),
                pageTransition = runCatching { PageTransitionEffect.valueOf(transitionStr) }.getOrDefault(PageTransitionEffect.Cube),
                selectedIconPackPackage = iconPack,
                isParallaxEnabled = parallax,
                showNotificationBadges = badges,
                searchEngine = runCatching { SearchEngineOption.valueOf(searchEngineStr) }.getOrDefault(SearchEngineOption.Google)
            ),
            isOnboardingCompleted = onboardingCompleted
        )
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    val isAiSuggestionsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AI_SUGGESTIONS_ENABLED] ?: true
    }

    suspend fun setAiSuggestionsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AI_SUGGESTIONS_ENABLED] = enabled
        }
    }

    suspend fun setGridDimensions(rows: Int, columns: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GRID_ROWS] = rows
            preferences[PreferencesKeys.GRID_COLUMNS] = columns
        }
    }

    suspend fun setShowAppLabels(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_APP_LABELS] = show
        }
    }

    suspend fun setDoubleTapToSleep(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DOUBLE_TAP_SLEEP] = enabled
        }
    }

    suspend fun updateThemeConfig(config: ThemeConfig) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE] = config.darkMode.name
            preferences[PreferencesKeys.DYNAMIC_COLORS] = config.useDynamicColors
            preferences[PreferencesKeys.ICON_SHAPE] = config.iconShape.name
            preferences[PreferencesKeys.PAGE_TRANSITION] = config.pageTransition.name
            preferences[PreferencesKeys.PARALLAX_ENABLED] = config.isParallaxEnabled
            preferences[PreferencesKeys.SHOW_NOTIFICATION_BADGES] = config.showNotificationBadges
            preferences[PreferencesKeys.SEARCH_ENGINE] = config.searchEngine.name
            val iconPack = config.selectedIconPackPackage
            if (iconPack != null) {
                preferences[PreferencesKeys.ICON_PACK] = iconPack
            } else {
                preferences.remove(PreferencesKeys.ICON_PACK)
            }
        }
    }

    suspend fun setIconShape(shape: IconShapeOption) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ICON_SHAPE] = shape.name
        }
    }

    suspend fun setPageTransition(effect: PageTransitionEffect) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PAGE_TRANSITION] = effect.name
        }
    }

    suspend fun setIconPack(packageName: String?) {
        context.dataStore.edit { preferences ->
            if (packageName != null) {
                preferences[PreferencesKeys.ICON_PACK] = packageName
            } else {
                preferences.remove(PreferencesKeys.ICON_PACK)
            }
        }
    }

    suspend fun setParallaxEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PARALLAX_ENABLED] = enabled
        }
    }

    suspend fun setShowNotificationBadges(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_NOTIFICATION_BADGES] = show
        }
    }

    suspend fun setSearchEngine(engine: SearchEngineOption) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SEARCH_ENGINE] = engine.name
        }
    }

    val hiddenPackages: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HIDDEN_PACKAGES] ?: emptySet()
    }

    val vaultPin: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.VAULT_PIN]
    }

    suspend fun hidePackage(packageName: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.HIDDEN_PACKAGES] ?: emptySet()
            preferences[PreferencesKeys.HIDDEN_PACKAGES] = current + packageName
        }
    }

    suspend fun unhidePackage(packageName: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.HIDDEN_PACKAGES] ?: emptySet()
            preferences[PreferencesKeys.HIDDEN_PACKAGES] = current - packageName
        }
    }

    suspend fun setVaultPin(pin: String?) {
        context.dataStore.edit { preferences ->
            if (pin != null) {
                preferences[PreferencesKeys.VAULT_PIN] = pin
            } else {
                preferences.remove(PreferencesKeys.VAULT_PIN)
            }
        }
    }

    val isAutoUpdateCheckEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_UPDATE_CHECK] ?: true
    }

    suspend fun setAutoUpdateCheckEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_UPDATE_CHECK] = enabled
        }
    }
}
