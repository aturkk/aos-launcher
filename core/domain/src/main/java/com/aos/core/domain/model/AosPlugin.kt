package com.aos.core.domain.model

interface AosPlugin {
    val manifest: PluginManifest
    val isEnabled: Boolean

    fun initialize(): Boolean
    fun terminate()
}

interface SearchPlugin : AosPlugin {
    suspend fun search(query: String): List<SearchResultItem>
}

interface WidgetPlugin : AosPlugin {
    val widgetTitle: String
    val defaultSpanX: Int
    val defaultSpanY: Int
    val previewSummary: String
}

interface ActionPlugin : AosPlugin {
    val actionLabel: String
    suspend fun executeAction(payload: String): Boolean
}
