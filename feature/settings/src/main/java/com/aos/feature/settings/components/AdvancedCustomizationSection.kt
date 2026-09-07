package com.aos.feature.settings.components

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aos.core.domain.model.SearchEngineOption
import com.aos.core.domain.model.ThemeConfig

@Composable
fun AdvancedCustomizationSection(
    themeConfig: ThemeConfig,
    onParallaxChange: (Boolean) -> Unit,
    onNotificationBadgesChange: (Boolean) -> Unit,
    onSearchEngineChange: (SearchEngineOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Gelişmiş Deneyim",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Gyroscope Parallax Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Jiroskop 3D Paralaks", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "Telefon hareket ettikçe ana ekran öğelerine 3D derinlik kazandırır",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            Switch(
                checked = themeConfig.isParallaxEnabled,
                onCheckedChange = onParallaxChange
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Notification Badges Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Bildirim Rozetleri (Badges)", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "Uygulama ikonlarının üzerinde okunmamış bildirim sayısını gösterir",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            Switch(
                checked = themeConfig.showNotificationBadges,
                onCheckedChange = onNotificationBadgesChange
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Search Engine Selector
        Text(text = "Ana Ekran Arama Motoru", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val engines = listOf(
                SearchEngineOption.Google to "Google",
                SearchEngineOption.DuckDuckGo to "DuckDuckGo",
                SearchEngineOption.Bing to "Bing"
            )
            engines.forEach { (eng, label) ->
                val isSelected = themeConfig.searchEngine == eng
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.06f))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onSearchEngineChange(eng) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
                    )
                }
            }
        }
    }
}
