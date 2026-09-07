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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aos.core.domain.model.HomeLayoutMode
import com.aos.core.domain.repository.UserPreferences

@Composable
fun HomeScreenSettingsContent(
    preferences: UserPreferences,
    onGridSizeChange: (Int, Int) -> Unit,
    onShowLabelsChange: (Boolean) -> Unit,
    onDoubleTapSleepChange: (Boolean) -> Unit,
    onNotificationBadgesChange: (Boolean) -> Unit,
    onHomeLayoutModeChange: (HomeLayoutMode) -> Unit = {},
    onWidgetPageChange: (Boolean) -> Unit = {},
    onNewsFeedChange: (Boolean) -> Unit = {},
    onSearchBarBottomChange: (Boolean) -> Unit = {},
    onClockWidgetChange: (Boolean) -> Unit = {},
    onSmartContextCardChange: (Boolean) -> Unit = {},
    onHideStatusBarChange: (Boolean) -> Unit = {},
    onHideNavigationBarChange: (Boolean) -> Unit = {},
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
        // Smart Launcher Home Layout Mode (Grid, Flower, Honeycomb, Arch)
        Text(
            text = "Ana Ekran İkon Düzeni",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Klasik ızgara veya Smart Launcher geometrik ikon dizilimleri",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))

        val currentMode = preferences.themeConfig.homeLayoutMode

        // 2x2 Grid of Geometric Layout Modes
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Row 1: Grid & Flower
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isGrid = currentMode == HomeLayoutMode.Grid
                val isFlower = currentMode == HomeLayoutMode.Flower

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isGrid) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                        .border(1.dp, if (isGrid) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                        .clickable { onHomeLayoutModeChange(HomeLayoutMode.Grid) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📱 Standart Izgara",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isGrid) MaterialTheme.colorScheme.primary else Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isFlower) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                        .border(1.dp, if (isFlower) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                        .clickable { onHomeLayoutModeChange(HomeLayoutMode.Flower) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🌸 Çiçek (Flower)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isFlower) MaterialTheme.colorScheme.primary else Color.White
                    )
                }
            }

            // Row 2: Honeycomb & Arch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isHoneycomb = currentMode == HomeLayoutMode.Honeycomb
                val isArch = currentMode == HomeLayoutMode.Arch

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isHoneycomb) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                        .border(1.dp, if (isHoneycomb) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                        .clickable { onHomeLayoutModeChange(HomeLayoutMode.Honeycomb) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⬡ Petek (Honeycomb)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isHoneycomb) MaterialTheme.colorScheme.primary else Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isArch) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                        .border(1.dp, if (isArch) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                        .clickable { onHomeLayoutModeChange(HomeLayoutMode.Arch) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⌒ Kemer (Arch)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isArch) MaterialTheme.colorScheme.primary else Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(24.dp))

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

        // Smart Launcher Modular 3-Panel Options
        Text(
            text = "Modüler Sayfalar & Ergonomi",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Dedicated Widget Page (Left Screen)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Özel Widget Paneli (Sol Panel)", style = MaterialTheme.typography.bodyLarge)
                Text("Sola kaydırıldığında tam ekran widget sayfası açılır", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Switch(
                checked = preferences.themeConfig.enableWidgetPage,
                onCheckedChange = onWidgetPageChange
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // News Feed (Right Screen)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("RSS Haber Akışı (Sağ Panel)", style = MaterialTheme.typography.bodyLarge)
                Text("Sağa kaydırıldığında güncel yerel haber sayfası açılır", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Switch(
                checked = preferences.themeConfig.enableNewsFeed,
                onCheckedChange = onNewsFeedChange
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar at Bottom (Thumb Zone)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Arama Çubuğu Altta (Tek Elle Kullanım)", style = MaterialTheme.typography.bodyLarge)
                Text("Arama çubuğunu başparmak erişimi için ekranın altına alır", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Switch(
                checked = preferences.themeConfig.searchBarAtBottom,
                onCheckedChange = onSearchBarBottomChange
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Clock & Date Widget
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Saat ve Tarih Widget'ı", style = MaterialTheme.typography.bodyLarge)
                Text("Ana ekranın üst kısmındaki dinamik saat", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Switch(
                checked = preferences.themeConfig.enableClockWidget,
                onCheckedChange = onClockWidgetChange
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Smart Context Assistant Card
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Akıllı Asistan Bağlam Kartı", style = MaterialTheme.typography.bodyLarge)
                Text("Yapay zeka proaktif öneri ve eylem kartı", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Switch(
                checked = preferences.themeConfig.enableSmartContextCard,
                onCheckedChange = onSmartContextCardChange
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(20.dp))

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

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(20.dp))

        // Smart Launcher 6: Ultra Immersive Mode
        Text(
            text = "Sürükleyici Ekran (Ultra Immersive Mode)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Ekran alanını maksimize etmek için sistem çubuklarını gizleyin (Kenardan kaydırarak geçici açabilirsiniz)",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Hide Status Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Durum Çubuğunu Gizle (Saat, Pil, Bildirimler)", style = MaterialTheme.typography.bodyLarge)
                Text("Üst sistem çubuğunu gizleyerek temiz bir tam ekran sunar", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Switch(
                checked = preferences.themeConfig.hideStatusBar,
                onCheckedChange = onHideStatusBarChange
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hide Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Gezinme Çubuğunu Gizle (Tam Ekran Duvar Kağıdı)", style = MaterialTheme.typography.bodyLarge)
                Text("Alt gezinme çubuğunu gizleyerek başlatıcıyı uçtan uca yayar", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Switch(
                checked = preferences.themeConfig.hideNavigationBar,
                onCheckedChange = onHideNavigationBarChange
            )
        }
    }
}

