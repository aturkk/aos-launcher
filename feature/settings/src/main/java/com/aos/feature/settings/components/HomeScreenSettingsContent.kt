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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aos.core.domain.repository.UserPreferences

@Composable
fun HomeScreenSettingsContent(
    preferences: UserPreferences,
    onGridSizeChange: (Int, Int) -> Unit,
    onShowLabelsChange: (Boolean) -> Unit,
    onDoubleTapSleepChange: (Boolean) -> Unit,
    onNotificationBadgesChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridOptions = listOf(
        Pair(4, 4) to "4 x 4",
        Pair(4, 5) to "4 x 5 (Standart)",
        Pair(5, 5) to "5 x 5 (Kompakt)",
        Pair(5, 6) to "5 x 6 (Geniş)"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Izgara Boyutu",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Ana ekrandaki sütun ve satır yerleşimini belirleyin",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            gridOptions.forEach { (pair, label) ->
                val (cols, rows) = pair
                val isSelected = preferences.gridColumns == cols && preferences.gridRows == rows

                Box(
                    modifier = Modifier
                        .weight(1f)
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
                        .clickable { onGridSizeChange(cols, rows) }
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

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(24.dp))

        // Show App Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Uygulama İsimlerini Göster", style = MaterialTheme.typography.bodyLarge)
                Text("Ana ekranda ikonların altındaki etiketler", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Switch(
                checked = preferences.showAppLabels,
                onCheckedChange = onShowLabelsChange
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Double Tap to Sleep
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Çift Dokunmayla Kilitle", style = MaterialTheme.typography.bodyLarge)
                Text("Boş alana çift dokunarak ekranı kapatır", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Switch(
                checked = preferences.doubleTapToSleep,
                onCheckedChange = onDoubleTapSleepChange
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Notification Badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Bildirim Rozetleri", style = MaterialTheme.typography.bodyLarge)
                Text("İkonların üzerinde okunmamış bildirim sayısını gösterir", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Switch(
                checked = preferences.themeConfig.showNotificationBadges,
                onCheckedChange = onNotificationBadgesChange
            )
        }
    }
}
