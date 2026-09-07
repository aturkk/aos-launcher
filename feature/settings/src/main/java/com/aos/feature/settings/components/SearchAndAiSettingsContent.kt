package com.aos.feature.settings.components

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aos.core.domain.model.SearchEngineOption
import com.aos.core.domain.repository.UserPreferences

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults

@Composable
fun SearchAndAiSettingsContent(
    preferences: UserPreferences,
    isAiEnabled: Boolean,
    hasUsagePermission: Boolean,
    onAiToggle: (Boolean) -> Unit,
    onClearAiData: () -> Unit,
    onSearchEngineChange: (SearchEngineOption) -> Unit,
    onToggleMathCalculator: (Boolean) -> Unit = {},
    onToggleContactsSearch: (Boolean) -> Unit = {},
    onToggleAiSearchChips: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Search Engine Selector
        Text(
            text = "Varsayılan Arama Motoru",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Ana ekrandaki arama çubuğunda kullanılacak varsayılan motor",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SearchEngineOption.entries.forEach { engine ->
                val isSelected = preferences.themeConfig.searchEngine == engine
                val displayName = when (engine) {
                    SearchEngineOption.Google -> "Google"
                    SearchEngineOption.DuckDuckGo -> "DuckDuckGo"
                    SearchEngineOption.Bing -> "Bing"
                    SearchEngineOption.ChatGPT -> "ChatGPT 🤖"
                    SearchEngineOption.Perplexity -> "Perplexity 🧠"
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else Color.White.copy(alpha = 0.05f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onSearchEngineChange(engine) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(20.dp))

        // Evrensel Akıllı Arama Özellikleri
        Text(
            text = "Evrensel Akıllı Arama Özellikleri",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Math Calculator Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Dahili Matematik Hesaplayıcı", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                Text("Aramada 15*8 gibi matematik ifadelerini anında çözer", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
            Switch(
                checked = preferences.themeConfig.enableMathCalculator,
                onCheckedChange = onToggleMathCalculator,
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Contacts Search Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Cihaz İçi Kişi Araması", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                Text("Rehberdeki kişileri arama kutusundan hızlıca arama/WhatsApp başlatma", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
            Switch(
                checked = preferences.themeConfig.enableContactsSearch,
                onCheckedChange = onToggleContactsSearch,
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AI Search Chips Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Hızlı Web & AI Arama Çipleri", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                Text("Arama çubuğunda ChatGPT, Perplexity, Google hızlı butonları", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
            Switch(
                checked = preferences.themeConfig.enableAiSearchChips,
                onCheckedChange = onToggleAiSearchChips,
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(20.dp))
        Spacer(modifier = Modifier.height(24.dp))

        // AI Suggestions & Smart Context Section
        AiSettingsSection(
            isAiEnabled = isAiEnabled,
            hasUsagePermission = hasUsagePermission,
            onAiToggle = onAiToggle,
            onRequestPermission = {
                try {
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    context.startActivity(intent)
                } catch (_: Exception) {}
            },
            onClearData = onClearAiData
        )
    }
}
