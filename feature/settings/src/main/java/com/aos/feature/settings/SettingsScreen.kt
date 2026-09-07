package com.aos.feature.settings

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aos.feature.settings.components.AboutAndDiagnosticsSection
import com.aos.feature.settings.components.AdvancedCustomizationSection
import com.aos.feature.settings.components.AiSettingsSection
import com.aos.feature.settings.components.AppUpdateSection
import com.aos.feature.settings.components.BackupSyncSection
import com.aos.feature.settings.components.NotificationHistorySection
import com.aos.feature.settings.components.PluginManagementSection
import com.aos.feature.settings.components.ProfileManagementSection
import com.aos.feature.settings.components.ThemeCustomizationSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()
    val installedPacks by viewModel.installedIconPacks.collectAsStateWithLifecycle()
    val isAiEnabled by viewModel.isAiEnabled.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val plugins by viewModel.plugins.collectAsStateWithLifecycle()
    val notificationHistory by viewModel.notificationHistory.collectAsStateWithLifecycle()
    val crashReports by viewModel.crashReports.collectAsStateWithLifecycle()
    val updateStatus by viewModel.updateStatus.collectAsStateWithLifecycle()
    val isAutoUpdateEnabled by viewModel.isAutoUpdateEnabled.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val gridOptions = listOf(
        Pair(4, 4) to "4 x 4",
        Pair(4, 5) to "4 x 5 (Standart)",
        Pair(5, 5) to "5 x 5 (Kompakt)",
        Pair(5, 6) to "5 x 6 (Geniş)"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AOS Başlatıcı Ayarları") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                }
            )
        },
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // AI & Smart Suggestions Section
            AiSettingsSection(
                isAiEnabled = isAiEnabled,
                hasUsagePermission = viewModel.hasUsageStatsPermission(),
                onAiToggle = viewModel::setAiEnabled,
                onRequestPermission = {
                    try {
                        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // ignore if not supported
                    }
                },
                onClearData = viewModel::clearAiData
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(24.dp))

            // Profile & Modes Section
            if (profiles.isNotEmpty()) {
                ProfileManagementSection(
                    profiles = profiles,
                    onSelectActiveProfile = viewModel::switchProfile,
                    onUpdateProfile = viewModel::updateProfile
                )

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Theme & Customization Section
            ThemeCustomizationSection(
                themeConfig = prefs.themeConfig,
                installedIconPacks = installedPacks,
                onDarkModeChange = viewModel::setDarkMode,
                onDynamicColorsChange = viewModel::setDynamicColors,
                onIconShapeChange = viewModel::setIconShape,
                onPageTransitionChange = viewModel::setPageTransition,
                onIconPackChange = viewModel::setIconPack
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(24.dp))

            // Advanced Experience Section
            AdvancedCustomizationSection(
                themeConfig = prefs.themeConfig,
                onParallaxChange = viewModel::setParallaxEnabled,
                onNotificationBadgesChange = viewModel::setShowNotificationBadges,
                onSearchEngineChange = viewModel::setSearchEngine
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Ana Ekran & Düzen",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Show App Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Uygulama İsimlerini Göster", style = MaterialTheme.typography.bodyLarge)
                    Text("Ana ekranda ikonların altındaki başlıklar", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Switch(
                    checked = prefs.showAppLabels,
                    onCheckedChange = viewModel::setShowAppLabels
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Double Tap to Sleep
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Çift Dokunma ile Kilitle", style = MaterialTheme.typography.bodyLarge)
                    Text("Boş alana iki kez dokunarak ekranı kapat (Erişilebilirlik)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Switch(
                    checked = prefs.doubleTapToSleep,
                    onCheckedChange = viewModel::setDoubleTapToSleep
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Grid Size Selector
            Text(
                text = "Izgara Boyutu",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                gridOptions.forEach { (dim, label) ->
                    val isSelected = prefs.gridColumns == dim.first && prefs.gridRows == dim.second

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
                            .clickable {
                                viewModel.setGridDimensions(rows = dim.second, cols = dim.first)
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${dim.first}x${dim.second}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(24.dp))

            // Backup & Cloud Sync Section
            BackupSyncSection(
                syncStatus = syncStatus,
                onExportBackup = viewModel::exportBackup,
                onImportBackup = viewModel::importBackup,
                onTriggerCloudSync = viewModel::triggerCloudSync
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(24.dp))

            // Plugin Ecosystem Section
            PluginManagementSection(
                plugins = plugins,
                onTogglePlugin = viewModel::togglePlugin
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(24.dp))

            // Notification History Section
            NotificationHistorySection(
                notifications = notificationHistory,
                onClearHistory = viewModel::clearNotificationHistory,
                onDeleteNotification = viewModel::deleteNotification
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Gestures Guide Info Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Jestler & Kısayollar",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Yukarı Kaydır: Uygulama Çekmecesini açar", style = MaterialTheme.typography.bodyMedium)
                    Text("• Aşağı Kaydır: Bildirim Panelini indirir", style = MaterialTheme.typography.bodyMedium)
                    Text("• Çift Dokun: Ekranı kapatır ve kilitler", style = MaterialTheme.typography.bodyMedium)
                    Text("• İki Parmak Küçült (Pinch): Sayfaları kuşbakışı yönetir", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(24.dp))

            // GitHub Auto-Update Section
            AppUpdateSection(
                updateStatus = updateStatus,
                isAutoUpdateEnabled = isAutoUpdateEnabled,
                onCheckForUpdates = viewModel::checkForUpdates,
                onDownloadAndInstall = viewModel::downloadAndInstallUpdate,
                onInstallApk = viewModel::installApk,
                onToggleAutoUpdate = viewModel::setAutoUpdateEnabled
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(24.dp))

            // About & Diagnostics Section
            AboutAndDiagnosticsSection(
                crashReports = crashReports,
                onClearCrashReports = viewModel::clearCrashReports
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

