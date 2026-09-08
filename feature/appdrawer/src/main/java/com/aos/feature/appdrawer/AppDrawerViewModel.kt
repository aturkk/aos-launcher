package com.aos.feature.appdrawer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aos.core.common.result.Result
import com.aos.core.common.util.MathEvaluator
import com.aos.core.domain.model.AppInfo
import com.aos.core.domain.model.Profile
import com.aos.core.domain.repository.AiSuggestionRepository
import com.aos.core.domain.repository.AppListRepository
import com.aos.core.domain.repository.ContactSearchRepository
import com.aos.core.domain.repository.HiddenAppsRepository
import com.aos.core.domain.repository.ProfileRepository
import com.aos.core.domain.repository.UserPreferencesRepository
import com.aos.core.ui.components.AppIconCache
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppDrawerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appListRepository: AppListRepository,
    private val profileRepository: ProfileRepository,
    private val aiSuggestionRepository: AiSuggestionRepository,
    private val hiddenAppsRepository: HiddenAppsRepository,
    private val contactSearchRepository: ContactSearchRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppDrawerUiState(isLoading = true))
    val uiState: StateFlow<AppDrawerUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadAppsAndProfile()
        loadAiSuggestions()
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            userPreferencesRepository.userPreferences.collect { prefs ->
                _uiState.update { current ->
                    current.copy(
                        mathCalculatorEnabled = prefs.themeConfig.enableMathCalculator,
                        contactsSearchEnabled = prefs.themeConfig.enableContactsSearch,
                        aiSearchChipsEnabled = prefs.themeConfig.enableAiSearchChips,
                        searchBarAtBottom = prefs.themeConfig.searchBarAtBottom,
                        iconShape = prefs.themeConfig.iconShape,
                        selectedIconPackPackage = prefs.themeConfig.selectedIconPackPackage
                    )
                }
            }
        }
    }

    private fun loadAppsAndProfile() {
        viewModelScope.launch {
            combine(
                appListRepository.getInstalledApps(),
                profileRepository.getActiveProfile(),
                hiddenAppsRepository.getHiddenPackages(),
                hiddenAppsRepository.getVaultPin()
            ) { appsResult, activeProfile, hiddenPackages, vaultPin ->
                AppCombinedData(appsResult, activeProfile, hiddenPackages, vaultPin)
            }.collect { data ->
                when (val appsResult = data.appsResult) {
                    is Result.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Result.Success -> {
                        val baseApps = appsResult.data
                        val hiddenAppsList = baseApps.filter { data.hiddenPackages.contains(it.packageName) }
                        val visibleApps = baseApps.filterNot { data.hiddenPackages.contains(it.packageName) }
                        val profileFiltered = filterByProfile(visibleApps, data.activeProfile)
                        _uiState.update { current ->
                            current.copy(
                                apps = profileFiltered,
                                filteredApps = filterByQuery(profileFiltered, current.searchQuery),
                                activeProfile = data.activeProfile,
                                hiddenApps = hiddenAppsList,
                                isVaultPinSet = !data.vaultPin.isNullOrBlank(),
                                isLoading = false
                            )
                        }

                        // Warm up icon cache in background to ensure zero stutter while scrolling
                        viewModelScope.launch(Dispatchers.IO) {
                            baseApps.forEach { app ->
                                AppIconCache.preload(context, app.packageName)
                            }
                        }
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = appsResult.message)
                    }
                }
            }
        }
    }

    private fun loadAiSuggestions() {
        viewModelScope.launch {
            combine(
                aiSuggestionRepository.getSuggestedApps(limit = 10),
                hiddenAppsRepository.getHiddenPackages(),
                profileRepository.getActiveProfile()
            ) { suggestions, hiddenPackages, activeProfile ->
                suggestions
                    .filterNot { hiddenPackages.contains(it.packageName) }
                    .filter { activeProfile?.isAppAllowed(it.packageName) ?: true }
                    .take(5)
            }.collect { filteredSuggestions ->
                _uiState.update { current ->
                    current.copy(suggestedApps = filteredSuggestions)
                }
            }
        }
    }

    fun recordAppLaunch(packageName: String) {
        viewModelScope.launch {
            aiSuggestionRepository.recordAppLaunch(packageName)
        }
    }

    fun onSearchQueryChanged(query: String) {
        val math = if (_uiState.value.mathCalculatorEnabled) MathEvaluator.evaluate(query) else null

        _uiState.update { current ->
            current.copy(
                searchQuery = query,
                filteredApps = filterByQuery(current.apps, query),
                mathResult = math
            )
        }

        searchJob?.cancel()
        if (_uiState.value.contactsSearchEnabled && query.trim().length >= 2) {
            searchJob = viewModelScope.launch {
                val contacts = contactSearchRepository.searchContacts(query.trim(), maxResults = 5)
                _uiState.update { current ->
                    if (current.searchQuery == query) {
                        current.copy(contactResults = contacts)
                    } else current
                }
            }
        } else {
            _uiState.update { it.copy(contactResults = emptyList()) }
        }
    }

    fun hideApp(packageName: String) {
        viewModelScope.launch {
            hiddenAppsRepository.hideApp(packageName)
        }
    }

    fun unhideApp(packageName: String) {
        viewModelScope.launch {
            hiddenAppsRepository.unhideApp(packageName)
        }
    }

    fun setVaultPin(pin: String) {
        viewModelScope.launch {
            hiddenAppsRepository.setVaultPin(pin)
            _uiState.update { it.copy(isVaultUnlocked = true, isVaultPinSet = true) }
        }
    }

    suspend fun verifyVaultPin(pin: String): Boolean {
        val isValid = hiddenAppsRepository.verifyPin(pin)
        if (isValid) {
            _uiState.update { it.copy(isVaultUnlocked = true) }
        }
        return isValid
    }

    fun lockVault() {
        _uiState.update { it.copy(isVaultUnlocked = false) }
    }

    private fun filterByProfile(apps: List<AppInfo>, profile: Profile?): List<AppInfo> {
        if (profile == null) return apps
        return apps.filter { profile.isAppAllowed(it.packageName) }
    }

    private fun filterByQuery(apps: List<AppInfo>, query: String): List<AppInfo> {
        if (query.isBlank()) return apps
        return apps.filter { it.label.contains(query, ignoreCase = true) }
    }
}

private data class AppCombinedData(
    val appsResult: Result<List<AppInfo>>,
    val activeProfile: Profile?,
    val hiddenPackages: Set<String>,
    val vaultPin: String?
)

