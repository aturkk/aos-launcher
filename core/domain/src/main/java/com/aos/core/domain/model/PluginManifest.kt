package com.aos.core.domain.model

enum class PluginType {
    Widget,
    SearchProvider,
    GestureAction,
    General
}

enum class PluginPermission {
    ProvideSearch,
    ProvideWidgets,
    TriggerActions,
    ReadLauncherState
}

data class PluginManifest(
    val id: String,
    val name: String,
    val versionName: String,
    val author: String,
    val description: String,
    val type: PluginType,
    val requiredPermissions: List<PluginPermission> = emptyList(),
    val iconName: String = "extension"
)

data class SearchResultItem(
    val title: String,
    val snippet: String,
    val actionUrl: String,
    val iconName: String = "search"
)
