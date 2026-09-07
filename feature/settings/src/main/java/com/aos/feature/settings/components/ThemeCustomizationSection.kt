package com.aos.feature.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aos.core.domain.model.DarkModeOption
import com.aos.core.domain.model.IconPack
import com.aos.core.domain.model.IconShapeOption
import com.aos.core.domain.model.PageTransitionEffect
import com.aos.core.domain.model.ThemeConfig
import com.aos.core.ui.theme.getIconShape
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemeCustomizationSection(
    themeConfig: ThemeConfig,
    installedIconPacks: List<IconPack>,
    onDarkModeChange: (DarkModeOption) -> Unit,
    onDynamicColorsChange: (Boolean) -> Unit,
    onIconShapeChange: (IconShapeOption) -> Unit,
    onPageTransitionChange: (PageTransitionEffect) -> Unit,
    onIconPackChange: (String?) -> Unit,
    onBlurDepthChange: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showIconPackDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Kişiselleştirme & Görünüm",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Dark Mode Options
        Text(text = "Tema Modu", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val modes = listOf(
                DarkModeOption.System to "Sistem",
                DarkModeOption.Light to "Açık",
                DarkModeOption.Dark to "Koyu"
            )
            modes.forEach { (mode, label) ->
                val isSelected = themeConfig.darkMode == mode
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
                        .clickable { onDarkModeChange(mode) }
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

        Spacer(modifier = Modifier.height(18.dp))

        // Dynamic Colors Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Dinamik Renkler (Material You)", style = MaterialTheme.typography.bodyLarge)
                Text("Duvar kağıdından uyumlu renk paleti üretir", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Switch(
                checked = themeConfig.useDynamicColors,
                onCheckedChange = onDynamicColorsChange
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Icon Shape Selector
        Text(text = "İkon Şekli", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val shapes = listOf(
                IconShapeOption.Squircle to "Squircle",
                IconShapeOption.Circle to "Daire",
                IconShapeOption.RoundedSquare to "Kare",
                IconShapeOption.Teardrop to "Gözyaşı"
            )
            shapes.forEach { (shapeOpt, name) ->
                val isSelected = themeConfig.iconShape == shapeOpt
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.06f))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onIconShapeChange(shapeOpt) }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(getIconShape(shapeOpt))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Page Transition Effects Selector
        Text(text = "3D Sayfa Geçiş Efekti", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val effects = listOf(
                PageTransitionEffect.Standard to "Standart",
                PageTransitionEffect.Cube to "3D Küp",
                PageTransitionEffect.Depth to "Derinlik",
                PageTransitionEffect.Flip to "Çevirme",
                PageTransitionEffect.Accordion to "Akordiyon"
            )
            effects.forEach { (eff, label) ->
                val isSelected = themeConfig.pageTransition == eff
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.06f))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onPageTransitionChange(eff) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
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

        Spacer(modifier = Modifier.height(18.dp))

        // Icon Pack Row
        val currentPackLabel = remember(themeConfig.selectedIconPackPackage, installedIconPacks) {
            if (themeConfig.selectedIconPackPackage == null) {
                "Varsayılan Sistem"
            } else {
                installedIconPacks.find { it.packageName == themeConfig.selectedIconPackPackage }?.label ?: themeConfig.selectedIconPackPackage
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .clickable { showIconPackDialog = true }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ColorLens,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("İkon Paketi", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = currentPackLabel ?: "Varsayılan Sistem",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Smart Launcher 6: Liquid Glass Blur Depth Slider
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sıvı Cam (Liquid Glass) Bulanıklığı",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Açılır widget kartları ve arama çubuğu için optik cam derinliği",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                Text(
                    text = "%${(themeConfig.blurDepth * 100).roundToInt()}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Slider(
                value = themeConfig.blurDepth,
                onValueChange = onBlurDepthChange,
                valueRange = 0f..1f,
                steps = 19
            )
        }
    }

    // Icon Pack Picker Dialog
    if (showIconPackDialog) {
        AlertDialog(
            onDismissRequest = { showIconPackDialog = false },
            title = { Text("İkon Paketi Seçin") },
            text = {
                LazyColumn {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onIconPackChange(null)
                                    showIconPackDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeConfig.selectedIconPackPackage == null,
                                onClick = {
                                    onIconPackChange(null)
                                    showIconPackDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Varsayılan Sistem İkonları")
                        }
                    }

                    if (installedIconPacks.isEmpty()) {
                        item {
                            Text(
                                text = "Yüklü 3. parti ikon paketi bulunamadı.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    } else {
                        items(installedIconPacks) { pack ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onIconPackChange(pack.packageName)
                                        showIconPackDialog = false
                                    }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = themeConfig.selectedIconPackPackage == pack.packageName,
                                    onClick = {
                                        onIconPackChange(pack.packageName)
                                        showIconPackDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(pack.label)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showIconPackDialog = false }) {
                    Text("Kapat")
                }
            }
        )
    }
}
