package com.aos.feature.appdrawer

import com.aos.core.domain.model.AppInfo
import com.aos.core.domain.model.AppSuggestion
import com.aos.core.domain.model.DeviceContact
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
    val isVaultPinSet: Boolean = false,
    val mathResult: String? = null,
    val contactResults: List<DeviceContact> = emptyList(),
    val mathCalculatorEnabled: Boolean = true,
    val contactsSearchEnabled: Boolean = true,
    val aiSearchChipsEnabled: Boolean = true,
    val searchBarAtBottom: Boolean = false,
    val iconShape: com.aos.core.domain.model.IconShapeOption = com.aos.core.domain.model.IconShapeOption.Squircle,
    val selectedIconPackPackage: String? = null
)
