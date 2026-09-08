package com.aos.feature.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aos.feature.settings.components.BackupSyncSection
import com.aos.feature.settings.components.GesturesSettingsContent
import com.aos.feature.settings.components.HomeScreenSettingsContent
import com.aos.feature.settings.components.NotificationHistorySection
import com.aos.feature.settings.components.PluginManagementSection
import com.aos.feature.settings.components.ProfileManagementSection
import com.aos.feature.settings.components.SearchAndAiSettingsContent
import com.aos.feature.settings.components.ThemeSettingsContent
import com.aos.feature.settings.components.UpdatesAndAboutSettingsContent

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

    var activeSubPage by rememberSaveable { mutableStateOf<SettingsSubPage?>(null) }

    // Intercept back button to return to Settings Hub before exiting
    BackHandler(enabled = activeSubPage != null) {
        activeSubPage = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = activeSubPage?.title ?: "AOS Başlatıcı Ayarları",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (activeSubPage != null) {
                                activeSubPage = null
                            } else {
                                onBack()
                            }
                        }
                    ) {
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
        AnimatedContent(
            targetState = activeSubPage,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "settings_navigation",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { targetPage ->
            if (targetPage == null) {
                // Main Settings Hub View
                SettingsHubView(
                    onSelectCategory = { activeSubPage = it }
                )
            } else {
                // Dedicated Sub-page View
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    when (targetPage) {
                        SettingsSubPage.Theme -> {
                            ThemeSettingsContent(
                                preferences = prefs,
                                installedIconPacks = installedPacks,
                                onDarkModeChange = viewModel::setDarkMode,
                                onDynamicColorsChange = viewModel::setDynamicColors,
                                onIconShapeChange = viewModel::setIconShape,
                                onPageTransitionChange = viewModel::setPageTransition,
                                onIconPackChange = viewModel::setIconPack,
                                onParallaxChange = viewModel::setParallaxEnabled,
                                onBlurDepthChange = viewModel::setBlurDepth
                            )
                        }

                        SettingsSubPage.HomeScreen -> {
                            HomeScreenSettingsContent(
                                preferences = prefs,
                                onGridSizeChange = { cols, rows -> viewModel.setGridDimensions(rows = rows, cols = cols) },
                                onShowLabelsChange = viewModel::setShowAppLabels,
                                onDoubleTapSleepChange = viewModel::setDoubleTapToSleep,
                                onNotificationBadgesChange = viewModel::setShowNotificationBadges,
                                onHomeLayoutModeChange = viewModel::setHomeLayoutMode,
                                onWidgetPageChange = viewModel::setWidgetPageEnabled,
                                onNewsFeedChange = viewModel::setNewsFeedEnabled,
                                onSearchBarBottomChange = viewModel::setSearchBarAtBottom,
                                onClockWidgetChange = viewModel::setClockWidgetEnabled,
                                onSmartContextCardChange = viewModel::setSmartContextCardEnabled,
                                onHideStatusBarChange = viewModel::setHideStatusBar,
                                onHideNavigationBarChange = viewModel::setHideNavigationBar
                            )
                        }

                        SettingsSubPage.AppDrawer -> {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Uygulama Çekmecesi & Kategoriler",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Smart Launcher dikey sol kategori çubuğu ve alfabetik hızlı kaydırma.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "• Dikey Kategori Çubuğu: Sol kenardaki simgelerle (Tümü, İletişim, Medya, Oyunlar, Üretkenlik, Araçlar) anında filtreleyin.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "• Hızlı A..Z Gezinme: Sağ kenardaki alfabetik çubuktan harflere dokunarak zıplayabilirsiniz.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Arama Çubuğu Altta (Tek Elle Kullanım)", style = MaterialTheme.typography.bodyLarge)
                                        Text("Arama çubuğunu başparmak erişimi için çekmecenin altına yerleştirir", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                    androidx.compose.material3.Switch(
                                        checked = prefs.themeConfig.searchBarAtBottom,
                                        onCheckedChange = viewModel::setSearchBarAtBottom
                                    )
                                }
                            }
                        }

                        SettingsSubPage.SearchAndAi -> {
                            SearchAndAiSettingsContent(
                                preferences = prefs,
                                isAiEnabled = isAiEnabled,
                                hasUsagePermission = viewModel.hasUsageStatsPermission(),
                                onAiToggle = viewModel::setAiEnabled,
                                onClearAiData = viewModel::clearAiData,
                                onSearchEngineChange = viewModel::setSearchEngine,
                                onToggleMathCalculator = viewModel::setMathCalculatorEnabled,
                                onToggleContactsSearch = viewModel::setContactsSearchEnabled,
                                onToggleAiSearchChips = viewModel::setAiSearchChipsEnabled
                            )
                        }

                        SettingsSubPage.NewsFeed -> {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Haber Akışı (RSS Okuyucu)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Ana ekranın soluna kaydırılarak erişilen bağımsız haber merkezi.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "• Favori kaynaklar (Teknoloji, Gündem, Bilim) yerel ve temiz biçimde sunulur.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        SettingsSubPage.Gestures -> {
                            GesturesSettingsContent()
                        }

                        SettingsSubPage.Widgets -> {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Araç Takımları & Yığınlar",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Widget stacks (yığınlar) ve pop-up widget yapılandırması.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "• Ana ekranda boş alana uzun basıp 'Widget Ekle' seçeneğiyle sistem widget'larını yerleştirebilirsiniz.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        SettingsSubPage.Profiles -> {
                            Column(modifier = Modifier.padding(16.dp)) {
                                ProfileManagementSection(
                                    profiles = profiles,
                                    onSelectActiveProfile = viewModel::switchProfile,
                                    onUpdateProfile = viewModel::updateProfile
                                )
                            }
                        }

                        SettingsSubPage.Privacy -> {
                            Column(modifier = Modifier.padding(16.dp)) {
                                NotificationHistorySection(
                                    notifications = notificationHistory,
                                    onClearHistory = viewModel::clearNotificationHistory,
                                    onDeleteNotification = viewModel::deleteNotification
                                )
                            }
                        }

                        SettingsSubPage.BackupSync -> {
                            Column(modifier = Modifier.padding(16.dp)) {
                                BackupSyncSection(
                                    syncStatus = syncStatus,
                                    onExportBackup = viewModel::exportBackup,
                                    onImportBackup = viewModel::importBackup,
                                    onTriggerCloudSync = viewModel::triggerCloudSync
                                )
                            }
                        }

                        SettingsSubPage.Plugins -> {
                            Column(modifier = Modifier.padding(16.dp)) {
                                PluginManagementSection(
                                    plugins = plugins,
                                    onTogglePlugin = viewModel::togglePlugin
                                )
                            }
                        }

                        SettingsSubPage.UpdatesAndAbout -> {
                            UpdatesAndAboutSettingsContent(
                                updateStatus = updateStatus,
                                isAutoUpdateEnabled = isAutoUpdateEnabled,
                                crashReports = crashReports,
                                onCheckForUpdates = viewModel::checkForUpdates,
                                onDownloadAndInstall = viewModel::downloadAndInstallUpdate,
                                onInstallApk = viewModel::installApk,
                                onToggleAutoUpdate = viewModel::setAutoUpdateEnabled,
                                onClearCrashReports = viewModel::clearCrashReports
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsHubView(
    onSelectCategory: (SettingsSubPage) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Kategoriler",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )

        SettingsSubPage.entries.forEach { subPage ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onSelectCategory(subPage) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = subPage.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = subPage.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subPage.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
