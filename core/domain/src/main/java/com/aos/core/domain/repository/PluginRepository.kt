package com.aos.core.domain.repository

import com.aos.core.domain.model.ActionPlugin
import com.aos.core.domain.model.AosPlugin
import com.aos.core.domain.model.SearchPlugin
import com.aos.core.domain.model.WidgetPlugin
import kotlinx.coroutines.flow.Flow

interface PluginRepository {
    fun getPlugins(): Flow<List<AosPlugin>>
    suspend fun togglePlugin(pluginId: String, enabled: Boolean)
    suspend fun getActiveSearchPlugins(): List<SearchPlugin>
    suspend fun getActiveWidgetPlugins(): List<WidgetPlugin>
    suspend fun getActiveActionPlugins(): List<ActionPlugin>
}
