package com.aos.core.data.repository

import com.aos.core.data.plugin.DuckDuckGoSearchPlugin
import com.aos.core.data.plugin.PluginSecuritySandbox
import com.aos.core.data.plugin.QuickFlashlightActionPlugin
import com.aos.core.data.plugin.WeatherWidgetPlugin
import com.aos.core.domain.model.ActionPlugin
import com.aos.core.domain.model.AosPlugin
import com.aos.core.domain.model.PluginPermission
import com.aos.core.domain.model.SearchPlugin
import com.aos.core.domain.model.WidgetPlugin
import com.aos.core.domain.repository.PluginRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PluginRepositoryImpl @Inject constructor(
    private val securitySandbox: PluginSecuritySandbox
) : PluginRepository {

    private val _plugins = MutableStateFlow<List<AosPlugin>>(
        listOf(
            DuckDuckGoSearchPlugin(isEnabled = true),
            WeatherWidgetPlugin(isEnabled = true),
            QuickFlashlightActionPlugin(isEnabled = true)
        )
    )

    override fun getPlugins(): Flow<List<AosPlugin>> = _plugins.asStateFlow()

    override suspend fun togglePlugin(pluginId: String, enabled: Boolean) {
        _plugins.update { currentList ->
            currentList.map { plugin ->
                if (plugin.manifest.id == pluginId) {
                    when (plugin) {
                        is DuckDuckGoSearchPlugin -> DuckDuckGoSearchPlugin(isEnabled = enabled)
                        is WeatherWidgetPlugin -> WeatherWidgetPlugin(isEnabled = enabled)
                        is QuickFlashlightActionPlugin -> QuickFlashlightActionPlugin(isEnabled = enabled)
                        else -> plugin
                    }
                } else {
                    plugin
                }
            }
        }
    }

    override suspend fun getActiveSearchPlugins(): List<SearchPlugin> {
        return _plugins.value
            .filterIsInstance<SearchPlugin>()
            .filter { it.isEnabled && securitySandbox.checkPermission(it, PluginPermission.ProvideSearch) }
    }

    override suspend fun getActiveWidgetPlugins(): List<WidgetPlugin> {
        return _plugins.value
            .filterIsInstance<WidgetPlugin>()
            .filter { it.isEnabled && securitySandbox.checkPermission(it, PluginPermission.ProvideWidgets) }
    }

    override suspend fun getActiveActionPlugins(): List<ActionPlugin> {
        return _plugins.value
            .filterIsInstance<ActionPlugin>()
            .filter { it.isEnabled && securitySandbox.checkPermission(it, PluginPermission.TriggerActions) }
    }
}
