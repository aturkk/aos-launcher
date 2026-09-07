package com.aos.feature.appdrawer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aos.core.common.result.Result
import com.aos.core.domain.model.AppInfo
import com.aos.core.domain.model.Profile
import com.aos.core.domain.repository.AiSuggestionRepository
import com.aos.core.domain.repository.AppListRepository
import com.aos.core.domain.repository.HiddenAppsRepository
import com.aos.core.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppDrawerViewModel @Inject constructor(
    private val appListRepository: AppListRepository,
    private val profileRepository: ProfileRepository,
    private val aiSuggestionRepository: AiSuggestionRepository,
    private val hiddenAppsRepository: HiddenAppsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppDrawerUiState(isLoading = true))
    val uiState: StateFlow<AppDrawerUiState> = _uiState.asStateFlow()

    init {
        loadAppsAndProfile()
        loadAiSuggestions()
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
            aiSuggestionRepository.getSuggestedApps(limit = 5).collect { suggestions ->
                _uiState.update { current ->
                    current.copy(suggestedApps = suggestions)
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
        _uiState.update { current ->
            current.copy(
                searchQuery = query,
                filteredApps = filterByQuery(current.apps, query)
            )
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

