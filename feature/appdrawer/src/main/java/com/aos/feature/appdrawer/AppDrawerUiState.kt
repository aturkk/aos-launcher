package com.aos.feature.appdrawer

import com.aos.core.domain.model.AppInfo
import com.aos.core.domain.model.AppSuggestion
import com.aos.core.domain.model.Profile

data class AppDrawerUiState(
    val apps: List<AppInfo> = emptyList(),
    val filteredApps: List<AppInfo> = emptyList(),
    val suggestedApps: List<AppSuggestion> = emptyList(),
    val activeProfile: Profile? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hiddenApps: List<AppInfo> = emptyList(),
    val isVaultUnlocked: Boolean = false,
    val isVaultPinSet: Boolean = false
)
