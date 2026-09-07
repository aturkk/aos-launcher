package com.aos.core.domain.model

enum class DarkModeOption {
    System,
    Light,
    Dark
}

enum class IconShapeOption {
    Squircle,
    Circle,
    RoundedSquare,
    Teardrop
}

enum class PageTransitionEffect {
    Standard,
    Cube,
    Depth,
    Flip,
    Accordion
}

enum class SearchEngineOption {
    Google,
    DuckDuckGo,
    Bing,
    ChatGPT,
    Perplexity;

    fun buildSearchUrl(query: String = ""): String {
        val trimmed = query.trim()
        val encoded = try {
            java.net.URLEncoder.encode(trimmed, "UTF-8")
        } catch (_: Exception) {
            trimmed
        }
        return when (this) {
            Google -> if (encoded.isBlank()) "https://www.google.com" else "https://www.google.com/search?q=$encoded"
            DuckDuckGo -> if (encoded.isBlank()) "https://duckduckgo.com" else "https://duckduckgo.com/?q=$encoded"
            Bing -> if (encoded.isBlank()) "https://www.bing.com" else "https://www.bing.com/search?q=$encoded"
            ChatGPT -> if (encoded.isBlank()) "https://chatgpt.com" else "https://chatgpt.com/?q=$encoded"
            Perplexity -> if (encoded.isBlank()) "https://www.perplexity.ai" else "https://www.perplexity.ai/search?q=$encoded"
        }
    }
}

data class ThemeConfig(
    val darkMode: DarkModeOption = DarkModeOption.System,
    val useDynamicColors: Boolean = true,
    val primaryColorArgb: Long = 0xFF6200EE,
    val iconShape: IconShapeOption = IconShapeOption.Squircle,
    val pageTransition: PageTransitionEffect = PageTransitionEffect.Cube,
    val selectedIconPackPackage: String? = null,
    val animationSpeedMultiplier: Float = 1.0f,
    val isParallaxEnabled: Boolean = true,
    val showNotificationBadges: Boolean = true,
    val searchEngine: SearchEngineOption = SearchEngineOption.Google,
    val enableMathCalculator: Boolean = true,
    val enableContactsSearch: Boolean = true,
    val enableAiSearchChips: Boolean = true,
    val enableNewsFeed: Boolean = true
)
