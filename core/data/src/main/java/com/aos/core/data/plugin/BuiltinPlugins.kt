package com.aos.core.data.plugin

import com.aos.core.domain.model.ActionPlugin
import com.aos.core.domain.model.PluginManifest
import com.aos.core.domain.model.PluginPermission
import com.aos.core.domain.model.PluginType
import com.aos.core.domain.model.SearchPlugin
import com.aos.core.domain.model.SearchResultItem
import com.aos.core.domain.model.WidgetPlugin
import java.net.URLEncoder

class DuckDuckGoSearchPlugin(
    override var isEnabled: Boolean = true
) : SearchPlugin {

    override val manifest: PluginManifest = PluginManifest(
        id = "com.aos.plugin.duckduckgo",
        name = "DuckDuckGo Arama Sağlayıcı",
        versionName = "1.0.0",
        author = "AOS Community",
        description = "Gizlilik odaklı doğrudan DuckDuckGo web arama eklentisi.",
        type = PluginType.SearchProvider,
        requiredPermissions = listOf(PluginPermission.ProvideSearch),
        iconName = "search"
    )

    override fun initialize(): Boolean = true
    override fun terminate() {}

    override suspend fun search(query: String): List<SearchResultItem> {
        if (!isEnabled || query.isBlank()) return emptyList()

        val encoded = try {
            URLEncoder.encode(query, "UTF-8")
        } catch (e: Exception) {
            query
        }

        return listOf(
            SearchResultItem(
                title = "DuckDuckGo: $query",
                snippet = "Web üzerinde gizli arama yapın",
                actionUrl = "https://duckduckgo.com/?q=$encoded",
                iconName = "search"
            ),
            SearchResultItem(
                title = "DuckDuckGo Görseller: $query",
                snippet = "İlgili görselleri DuckDuckGo ile keşfedin",
                actionUrl = "https://duckduckgo.com/?q=$encoded&iax=images&ia=images",
                iconName = "image"
            )
        )
    }
}

class WeatherWidgetPlugin(
    override var isEnabled: Boolean = true
) : WidgetPlugin {

    override val manifest: PluginManifest = PluginManifest(
        id = "com.aos.plugin.weather",
        name = "AOS Canlı Hava Durumu",
        versionName = "1.0.0",
        author = "AOS Core Team",
        description = "Ana ekranda anlık sıcaklık ve hava koşullarını gösteren hafif widget.",
        type = PluginType.Widget,
        requiredPermissions = listOf(PluginPermission.ProvideWidgets),
        iconName = "cloud"
    )

    override val widgetTitle: String = "AOS Hava Durumu (22°C - Güneşli)"
    override val defaultSpanX: Int = 4
    override val defaultSpanY: Int = 1
    override val previewSummary: String = "Konum bazlı 24 saatlik sıcaklık ve nem göstergesi"

    override fun initialize(): Boolean = true
    override fun terminate() {}
}

class QuickFlashlightActionPlugin(
    override var isEnabled: Boolean = true
) : ActionPlugin {

    override val manifest: PluginManifest = PluginManifest(
        id = "com.aos.plugin.flashlight",
        name = "Hızlı Fener Eylemi",
        versionName = "1.0.0",
        author = "AOS Core Team",
        description = "Jestlerle veya arama kutusundan feneri açıp kapatmayı sağlayan eylem eklentisi.",
        type = PluginType.GestureAction,
        requiredPermissions = listOf(PluginPermission.TriggerActions),
        iconName = "flash"
    )

    override val actionLabel: String = "Feneri Aç / Kapat"

    override fun initialize(): Boolean = true
    override fun terminate() {}

    override suspend fun executeAction(payload: String): Boolean {
        if (!isEnabled) return false
        // Simulates toggling flashlight safely
        return true
    }
}
