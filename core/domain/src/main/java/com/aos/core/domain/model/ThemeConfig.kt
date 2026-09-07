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
    Bing
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
    val searchEngine: SearchEngineOption = SearchEngineOption.Google
)
