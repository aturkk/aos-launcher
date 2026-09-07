package com.aos.feature.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aos.core.domain.model.DarkModeOption
import com.aos.core.domain.model.IconPack
import com.aos.core.domain.model.IconShapeOption
import com.aos.core.domain.model.PageTransitionEffect
import com.aos.core.domain.model.SearchEngineOption
import com.aos.core.domain.repository.UserPreferences

@Composable
fun ThemeSettingsContent(
    preferences: UserPreferences,
    installedIconPacks: List<IconPack>,
    onDarkModeChange: (DarkModeOption) -> Unit,
    onDynamicColorsChange: (Boolean) -> Unit,
    onIconShapeChange: (IconShapeOption) -> Unit,
    onPageTransitionChange: (PageTransitionEffect) -> Unit,
    onIconPackChange: (String?) -> Unit,
    onParallaxChange: (Boolean) -> Unit,
    onBlurDepthChange: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        ThemeCustomizationSection(
            themeConfig = preferences.themeConfig,
            installedIconPacks = installedIconPacks,
            onDarkModeChange = onDarkModeChange,
            onDynamicColorsChange = onDynamicColorsChange,
            onIconShapeChange = onIconShapeChange,
            onPageTransitionChange = onPageTransitionChange,
            onIconPackChange = onIconPackChange,
            onBlurDepthChange = onBlurDepthChange
        )

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(24.dp))

        AdvancedCustomizationSection(
            themeConfig = preferences.themeConfig,
            onParallaxChange = onParallaxChange,
            onNotificationBadgesChange = {},
            onSearchEngineChange = {}
        )
    }
}
