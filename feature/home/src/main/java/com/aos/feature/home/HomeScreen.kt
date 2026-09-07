package com.aos.feature.home

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aos.core.domain.model.AppInfo
import com.aos.core.domain.model.AssistantResult
import com.aos.core.domain.model.LauncherItem
import com.aos.core.domain.model.ProfileType
import com.aos.core.domain.model.SearchEngineOption
import com.aos.core.domain.model.SmartActionType
import com.aos.core.ui.animation.pageTransitionEffect
import com.aos.core.ui.components.AosClockWidget
import com.aos.core.ui.components.LauncherSearchBar
import com.aos.core.ui.sensor.parallaxSensorEffect
import com.aos.core.ui.theme.getIconShape
import com.aos.feature.home.components.AssistantBottomSheet
import com.aos.feature.home.components.DockBar
import com.aos.feature.home.components.FolderModalDialog
import com.aos.feature.home.components.GridCellLayout
import com.aos.feature.home.components.GridLayoutPickerDialog
import com.aos.feature.home.components.OxygenEditModeBottomBar
import com.aos.feature.home.components.OxygenEditModeTopBar
import com.aos.feature.home.components.PageIndicator
import com.aos.feature.home.components.ProfileSwitcherBar
import com.aos.feature.home.components.SmartContextCardWidget
import com.aos.feature.home.widget.LauncherWidgetHost
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    notificationCounts: Map<String, Int> = emptyMap(),
    pendingPlacedApp: AppInfo? = null,
    onClearPendingPlacedApp: () -> Unit = {},
    onOpenAppDrawer: () -> Unit,
    onOpenNotifications: () -> Unit,
    onDoubleTapSleep: () -> Unit,
    onOpenSettings: () -> Unit,
    onAddWidget: () -> Unit = {},
    widgetHost: LauncherWidgetHost? = null,
    onOpenSearch: (SearchEngineOption) -> Unit = {},
    onOpenVoiceSearch: () -> Unit = {},
    onExecuteAssistantResult: (AssistantResult) -> Unit = {},
    onAppClick: (packageName: String, activityName: String) -> Unit,
    onAppInfo: (packageName: String) -> Unit,
    onUninstall: (packageName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { maxOf(1, uiState.pages.size) }
    )
    val coroutineScope = rememberCoroutineScope()

    var activeFolder by remember { mutableStateOf<LauncherItem.FolderItem?>(null) }
    var isEditMode by remember { mutableStateOf(false) }
    var showGridLayoutPicker by remember { mutableStateOf(false) }

    val effectiveEditMode = isEditMode || (pendingPlacedApp != null)

    // Intercept back button when in Edit Mode or placing an app
    BackHandler(enabled = effectiveEditMode) {
        if (pendingPlacedApp != null) {
            onClearPendingPlacedApp()
        }
        isEditMode = false
    }

    // Blocked App mindful friction dialog state
    var blockedAppPending by remember { mutableStateOf<Pair<String, String>?>(null) }

    val activeIconShape = remember(uiState.userPreferences.themeConfig.iconShape) {
        getIconShape(uiState.userPreferences.themeConfig.iconShape)
    }

    val activeBadges = remember(uiState.userPreferences.themeConfig.showNotificationBadges, notificationCounts) {
        if (uiState.userPreferences.themeConfig.showNotificationBadges) notificationCounts else emptyMap()
    }

    val handleAppClick: (String, String) -> Unit = { pkg, activity ->
        if (viewModel.isAppBlockedByActiveProfile(pkg)) {
            blockedAppPending = Pair(pkg, activity)
        } else {
            onAppClick(pkg, activity)
        }
    }

    val homeScale by animateFloatAsState(
        targetValue = if (effectiveEditMode) 0.88f else 1f,
        label = "homeScale"
    )

    val onOpenWallpaperAndStyle: () -> Unit = {
        try {
            val intent = Intent(Intent.ACTION_SET_WALLPAPER)
            context.startActivity(Intent.createChooser(intent, "Duvar Kağıdı Seçin"))
        } catch (e: Exception) {
            Toast.makeText(context, "Duvar kağıdı seçici açılamadı", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            // Double-tap to sleep and long-press for Edit Mode on wallpaper
            .pointerInput(uiState.userPreferences.doubleTapToSleep) {
                detectTapGestures(
                    onDoubleTap = {
                        if (uiState.userPreferences.doubleTapToSleep) {
                            onDoubleTapSleep()
                        }
                    },
                    onLongPress = {
                        isEditMode = true
                    }
                )
            }
            // Swipe gestures: Swipe-down for notifications, Swipe-up for App Drawer
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (!effectiveEditMode) {
                        if (dragAmount < -35f) {
                            onOpenAppDrawer()
                        } else if (dragAmount > 35f) {
                            onOpenNotifications()
                        }
                    }
                }
            }
            // Pinch-in to enter Edit Mode
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    if (zoom < 0.85f) {
                        isEditMode = true
                    }
                }
            }
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top OxygenOS 16 Edit Mode Bar
                AnimatedVisibility(
                    visible = effectiveEditMode,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                ) {
                    if (pendingPlacedApp != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFF2A2B30).copy(alpha = 0.92f)
                            ) {
                                Text(
                                    text = "Yerleştir: ${pendingPlacedApp.label}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }

                            Surface(
                                onClick = onClearPendingPlacedApp,
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFD32F2F).copy(alpha = 0.92f)
                            ) {
                                Text(
                                    text = "Vazgeç",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    } else {
                        OxygenEditModeTopBar(
                            onGroupClick = {
                                Toast.makeText(context, "Simgeler otomatik hizalandı", Toast.LENGTH_SHORT).show()
                            },
                            onDoneClick = { isEditMode = false }
                        )
                    }
                }

                // Live Home Page Screen Container (Scales smoothly to 0.88x)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .graphicsLayer {
                            scaleX = homeScale
                            scaleY = homeScale
                        }
                        .then(
                            if (effectiveEditMode) {
                                Modifier
                                    .clip(RoundedCornerShape(24.dp))
                                    .border(1.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
                            } else {
                                Modifier
                            }
                        )
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Built-in Clock Widget at the top
                        AosClockWidget()

                        // AI Proactive Smart Context Card
                        SmartContextCardWidget(
                            card = uiState.smartContextCard,
                            onActionClick = { actionType, payload ->
                                when (actionType) {
                                    SmartActionType.LaunchApp -> handleAppClick(payload, "")
                                    SmartActionType.SwitchProfile -> {
                                        val id = payload.toLongOrNull() ?: 1L
                                        viewModel.switchProfile(id)
                                    }
                                    SmartActionType.SearchWeb -> onOpenSearch(uiState.userPreferences.themeConfig.searchEngine)
                                    SmartActionType.OpenSettings -> onOpenSettings()
                                    SmartActionType.LockScreen -> onDoubleTapSleep()
                                    SmartActionType.None -> {}
                                }
                            }
                        )

                        // Profile Switcher Bar (Quick Mode Pills)
                        if (uiState.profiles.isNotEmpty()) {
                            ProfileSwitcherBar(
                                profiles = uiState.profiles,
                                activeProfile = uiState.activeProfile,
                                onProfileSelect = { profile ->
                                    viewModel.switchProfile(profile.id)
                                }
                            )
                        }

                        // Search Bar Capsule (mic opens AI Assistant)
                        LauncherSearchBar(
                            engine = uiState.userPreferences.themeConfig.searchEngine,
                            onSearchClick = { onOpenSearch(uiState.userPreferences.themeConfig.searchEngine) },
                            onVoiceSearchClick = { viewModel.setAssistantSheetOpen(true) }
                        )

                        // Main Horizontal Pager for Home Pages with 3D Transitions & Gyro Parallax
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .parallaxSensorEffect(
                                    enabled = uiState.userPreferences.themeConfig.isParallaxEnabled,
                                    maxOffsetDp = 8.dp
                                )
                        ) { pageIndex ->
                            val pageItems = uiState.itemsByPage[pageIndex] ?: emptyList()
                            val pageOffset = (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pageTransitionEffect(
                                        pageOffset = pageOffset,
                                        effect = uiState.userPreferences.themeConfig.pageTransition
                                    )
                            ) {
                                GridCellLayout(
                                    items = pageItems,
                                    columns = uiState.userPreferences.gridColumns,
                                    rows = uiState.userPreferences.gridRows,
                                    iconShape = activeIconShape,
                                    showLabels = uiState.userPreferences.showAppLabels,
                                    notificationCounts = activeBadges,
                                    isEditMode = effectiveEditMode,
                                    pendingPlacedApp = pendingPlacedApp,
                                    onPlacePendingApp = { app, cellX, cellY ->
                                        viewModel.placeAppAt(app, cellX, cellY, pageIndex)
                                        onClearPendingPlacedApp()
                                        Toast.makeText(context, "${app.label} ana ekrana yerleştirildi", Toast.LENGTH_SHORT).show()
                                    },
                                    onAppClick = handleAppClick,
                                    onFolderClick = { folder -> activeFolder = folder },
                                    onMoveItem = { itemId, x, y -> viewModel.moveItem(itemId, x, y, pageIndex) },
                                    onMergeIntoFolder = { dragged, target -> viewModel.mergeAppsIntoFolder(dragged, target) },
                                    onAddToExistingFolder = { dragged, targetFolder -> viewModel.addAppToExistingFolder(dragged, targetFolder) },
                                    onRemoveItem = viewModel::deleteItem,
                                    onAppInfo = onAppInfo,
                                    onUninstall = onUninstall,
                                    onEmptyAreaLongClick = { isEditMode = true },
                                    widgetHost = widgetHost
                                )
                            }
                        }

                        // Page Indicator
                        PageIndicator(
                            pageCount = pagerState.pageCount,
                            currentPage = pagerState.currentPage,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { isEditMode = true }
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        )

                        // Bottom Dock Bar (hidden in edit mode)
                        if (!effectiveEditMode) {
                            Spacer(modifier = Modifier.height(4.dp))
                            DockBar(
                                dockItems = uiState.dockItems,
                                onOpenAppDrawer = onOpenAppDrawer,
                                onAppClick = handleAppClick
                            )
                        }
                    }
                }

                // OxygenOS 16 Bottom Action Bar in Edit Mode
                AnimatedVisibility(
                    visible = effectiveEditMode,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    OxygenEditModeBottomBar(
                        onAddWidget = {
                            isEditMode = false
                            onAddWidget()
                        },
                        onOpenWallpaperAndStyle = onOpenWallpaperAndStyle,
                        onOpenLayoutGrid = { showGridLayoutPicker = true },
                        onOpenSettings = {
                            isEditMode = false
                            onOpenSettings()
                        }
                    )
                }
            }
        }

        // Layout Grid Dimensions Picker Dialog
        if (showGridLayoutPicker) {
            GridLayoutPickerDialog(
                currentRows = uiState.userPreferences.gridRows,
                currentColumns = uiState.userPreferences.gridColumns,
                onSelectDimensions = { rows, cols ->
                    viewModel.updateGridDimensions(rows, cols)
                },
                onDismiss = { showGridLayoutPicker = false }
            )
        }

        // Folder Modal Dialog
        activeFolder?.let { folder ->
            FolderModalDialog(
                folder = folder,
                onDismiss = { activeFolder = null },
                onAppClick = { pkg, act ->
                    activeFolder = null
                    handleAppClick(pkg, act)
                },
                onUpdateTitle = { newTitle ->
                    viewModel.updateFolderTitle(folder.id, newTitle, folder.items)
                },
                onRemoveAppFromFolder = { appToRemove ->
                    viewModel.removeAppFromFolder(folder, appToRemove)
                },
                onDeleteFolder = {
                    viewModel.deleteItem(folder.id)
                    activeFolder = null
                }
            )
        }

        // Mindful Friction / Blocked App Dialog
        blockedAppPending?.let { (pkg, act) ->
            val isKids = uiState.activeProfile?.type == ProfileType.Kids
            AlertDialog(
                onDismissRequest = { blockedAppPending = null },
                title = { Text(if (isKids) "Erişim Engellendi" else "Odak Uyarısı") },
                text = {
                    val profileName = uiState.activeProfile?.name ?: "Aktif Mod"
                    if (isKids) {
                        Text("Bu uygulama Çocuk Modu için izinli uygulamalar listesinde yer almıyor.")
                    } else {
                        Text("$profileName devredeyken bu uygulama dikkat dağıtıcı olarak sınırlandırıldı. Yine de açmak istiyor musunuz?")
                    }
                },
                confirmButton = {
                    if (!isKids) {
                        TextButton(
                            onClick = {
                                blockedAppPending = null
                                onAppClick(pkg, act)
                            }
                        ) {
                            Text("Mola Ver & Aç")
                        }
                    } else {
                        TextButton(onClick = { blockedAppPending = null }) {
                            Text("Tamam")
                        }
                    }
                },
                dismissButton = {
                    if (!isKids) {
                        TextButton(onClick = { blockedAppPending = null }) {
                            Text("Vazgeç")
                        }
                    }
                }
            )
        }

        // AI Assistant Bottom Sheet
        if (uiState.isAssistantSheetOpen) {
            AssistantBottomSheet(
                lastResult = uiState.assistantResult,
                onSendCommand = { viewModel.processAssistantCommand(it) },
                onExecuteResult = { result ->
                    viewModel.setAssistantSheetOpen(false)
                    viewModel.clearAssistantResult()
                    onExecuteAssistantResult(result)
                },
                onVoiceInputClick = onOpenVoiceSearch,
                onDismiss = {
                    viewModel.setAssistantSheetOpen(false)
                    viewModel.clearAssistantResult()
                }
            )
        }
    }
}
