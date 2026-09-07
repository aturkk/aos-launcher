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
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.aos.core.domain.model.HomeLayoutMode
import com.aos.feature.home.components.AssistantBottomSheet
import com.aos.feature.home.components.ArchLayout
import com.aos.feature.home.components.DockBar
import com.aos.feature.home.components.FlowerLayout
import com.aos.feature.home.components.FolderModalDialog
import com.aos.feature.home.components.GridCellLayout
import com.aos.feature.home.components.HoneycombLayout
import com.aos.feature.home.components.GridLayoutPickerDialog
import com.aos.feature.home.components.OxygenEditModeBottomBar
import com.aos.feature.home.components.OxygenEditModeTopBar
import com.aos.feature.home.components.PageIndicator
import com.aos.feature.home.components.ProfileSwitcherBar
import com.aos.feature.home.components.SmartContextCardWidget
import com.aos.feature.home.components.SmartWidgetPage
import com.aos.feature.home.components.WidgetStackModalDialog
import com.aos.feature.home.news.NewsFeedView
import com.aos.feature.home.widget.LauncherWidgetHost
import com.aos.feature.home.widget.PopupWidgetDialog
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
    onAddWidgetToStack: (targetId: Long) -> Unit = {},
    onAddPopupWidgetToApp: (appItemId: Long) -> Unit = {},
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

    val enableWidgetPage = uiState.userPreferences.themeConfig.enableWidgetPage
    val enableNewsFeed = uiState.userPreferences.themeConfig.enableNewsFeed
    val homePageCount = maxOf(1, uiState.pages.size)
    val widgetPageOffset = if (enableWidgetPage) 1 else 0
    val newsPageOffset = if (enableNewsFeed) 1 else 0
    val totalPageCount = widgetPageOffset + homePageCount + newsPageOffset

    val pagerState = rememberPagerState(
        initialPage = widgetPageOffset,
        pageCount = { totalPageCount }
    )
    val isCurrentPageHome = pagerState.currentPage in widgetPageOffset until (widgetPageOffset + homePageCount)
    val coroutineScope = rememberCoroutineScope()

    var activeFolder by remember { mutableStateOf<LauncherItem.FolderItem?>(null) }
    var activeStackForEditing by remember { mutableStateOf<LauncherItem.WidgetStackItem?>(null) }
    var activePopupWidgetApp by remember { mutableStateOf<LauncherItem.AppItem?>(null) }
    var isEditMode by remember { mutableStateOf(false) }
    var showGridLayoutPicker by remember { mutableStateOf(false) }

    val effectiveEditMode = isEditMode

    // Intercept back button when in Edit Mode or placing an app
    BackHandler(enabled = isEditMode || (pendingPlacedApp != null)) {
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
            .pointerInput(isCurrentPageHome, effectiveEditMode) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (!effectiveEditMode && isCurrentPageHome) {
                        if (dragAmount < -45f) {
                            onOpenAppDrawer()
                        } else if (dragAmount > 45f) {
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
                    visible = isEditMode,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                ) {
                    OxygenEditModeTopBar(
                        onGroupClick = {
                            Toast.makeText(context, "Simgeler otomatik hizalandı", Toast.LENGTH_SHORT).show()
                        },
                        onDoneClick = { isEditMode = false }
                    )
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
                        // Main 3-Panel Horizontal Pager
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
                            val pageOffset = (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pageTransitionEffect(
                                        pageOffset = pageOffset,
                                        effect = uiState.userPreferences.themeConfig.pageTransition
                                    )
                            ) {
                                if (enableWidgetPage && pageIndex == 0) {
                                    // Left Panel: Smart Widget Page
                                    val allWidgets = remember(uiState.itemsByPage) {
                                        uiState.itemsByPage.values.flatten().filterIsInstance<LauncherItem.WidgetItem>()
                                    }
                                    SmartWidgetPage(
                                        widgets = allWidgets,
                                        onAddWidget = {
                                            if (effectiveEditMode) isEditMode = false
                                            onAddWidget()
                                        },
                                        onRemoveWidget = viewModel::deleteItem,
                                        widgetHost = widgetHost
                                    )
                                } else if (enableNewsFeed && pageIndex == totalPageCount - 1) {
                                    // Right Panel: RSS News Feed
                                    NewsFeedView(
                                        articles = uiState.newsArticles,
                                        isLoading = uiState.isNewsLoading,
                                        onRefresh = viewModel::refreshNews
                                    )
                                } else {
                                    // Center Panels: Main Home Pages
                                    val homeIndex = pageIndex - widgetPageOffset
                                    val pageItems = uiState.itemsByPage[homeIndex] ?: emptyList()

                                    Column(modifier = Modifier.fillMaxSize()) {
                                        // Built-in Clock Widget at the top
                                        if (uiState.userPreferences.themeConfig.enableClockWidget) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .pointerInput(Unit) {
                                                        detectTapGestures(
                                                            onLongPress = { isEditMode = true }
                                                        )
                                                    }
                                            ) {
                                                AosClockWidget()

                                                if (effectiveEditMode) {
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.TopEnd)
                                                            .padding(top = 10.dp, end = 20.dp)
                                                            .size(24.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFFE53935))
                                                            .clickable {
                                                                viewModel.setClockWidgetEnabled(false)
                                                                Toast.makeText(context, "Saat widget'ı kaldırıldı", Toast.LENGTH_SHORT).show()
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = "Saat Widget'ını Kaldır",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // AI Proactive Smart Context Card
                                        if (uiState.userPreferences.themeConfig.enableSmartContextCard) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .pointerInput(Unit) {
                                                        detectTapGestures(
                                                            onLongPress = { isEditMode = true }
                                                        )
                                                    }
                                            ) {
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

                                                if (effectiveEditMode) {
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.TopEnd)
                                                            .padding(top = 8.dp, end = 20.dp)
                                                            .size(24.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFFE53935))
                                                            .clickable {
                                                                viewModel.setSmartContextCardEnabled(false)
                                                                Toast.makeText(context, "Akıllı kart kaldırıldı", Toast.LENGTH_SHORT).show()
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = "Akıllı Kartı Kaldır",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }

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

                                        // Top Search Bar (if not at bottom)
                                        if (!uiState.userPreferences.themeConfig.searchBarAtBottom) {
                                            LauncherSearchBar(
                                                engine = uiState.userPreferences.themeConfig.searchEngine,
                                                onSearchClick = { onOpenSearch(uiState.userPreferences.themeConfig.searchEngine) },
                                                onVoiceSearchClick = { viewModel.setAssistantSheetOpen(true) }
                                            )
                                        }

                                        // Main Icon Content: Flower or Grid
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                        ) {
                                            val mode = uiState.userPreferences.themeConfig.homeLayoutMode
                                            if (homeIndex == 0 && mode != HomeLayoutMode.Grid) {
                                                when (mode) {
                                                    HomeLayoutMode.Flower -> {
                                                        FlowerLayout(
                                                            items = pageItems,
                                                            iconShape = activeIconShape,
                                                            showLabels = uiState.userPreferences.showAppLabels,
                                                            notificationCounts = activeBadges,
                                                            onAppClick = handleAppClick,
                                                            onFolderClick = { folder -> activeFolder = folder },
                                                            onEmptySlotClick = onOpenAppDrawer,
                                                            isEditMode = effectiveEditMode,
                                                            onRemoveItem = viewModel::deleteItem,
                                                            onItemLongClick = { isEditMode = true }
                                                        )
                                                    }
                                                    HomeLayoutMode.Honeycomb -> {
                                                        HoneycombLayout(
                                                            items = pageItems,
                                                            iconShape = activeIconShape,
                                                            showLabels = uiState.userPreferences.showAppLabels,
                                                            notificationCounts = activeBadges,
                                                            onAppClick = handleAppClick,
                                                            onFolderClick = { folder -> activeFolder = folder },
                                                            onEmptySlotClick = onOpenAppDrawer,
                                                            isEditMode = effectiveEditMode,
                                                            onRemoveItem = viewModel::deleteItem,
                                                            onItemLongClick = { isEditMode = true }
                                                        )
                                                    }
                                                    HomeLayoutMode.Arch -> {
                                                        ArchLayout(
                                                            items = pageItems,
                                                            iconShape = activeIconShape,
                                                            showLabels = uiState.userPreferences.showAppLabels,
                                                            notificationCounts = activeBadges,
                                                            onAppClick = handleAppClick,
                                                            onFolderClick = { folder -> activeFolder = folder },
                                                            onEmptySlotClick = onOpenAppDrawer,
                                                            isEditMode = effectiveEditMode,
                                                            onRemoveItem = viewModel::deleteItem,
                                                            onItemLongClick = { isEditMode = true }
                                                        )
                                                    }
                                                    HomeLayoutMode.Grid -> Unit
                                                }
                                            } else {
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
                                                        viewModel.placeAppAt(app, cellX, cellY, homeIndex)
                                                        onClearPendingPlacedApp()
                                                        Toast.makeText(context, "${app.label} ana ekrana yerleştirildi", Toast.LENGTH_SHORT).show()
                                                    },
                                                    onAppClick = handleAppClick,
                                                    onFolderClick = { folder -> activeFolder = folder },
                                                    onMoveItem = { itemId, x, y -> viewModel.moveItem(itemId, x, y, homeIndex) },
                                                    onMergeIntoFolder = { dragged, target -> viewModel.mergeAppsIntoFolder(dragged, target) },
                                                    onAddToExistingFolder = { dragged, targetFolder -> viewModel.addAppToExistingFolder(dragged, targetFolder) },
                                                    onMergeIntoWidgetStack = { dragged, target -> viewModel.mergeWidgetsIntoStack(dragged, target) },
                                                    onAddWidgetToExistingStack = { dragged, targetStack -> viewModel.addWidgetToExistingStack(dragged, targetStack) },
                                                    onOpenStackSettings = { stack -> activeStackForEditing = stack },
                                                    onAddWidgetToSingleWidget = { widgetItem -> onAddWidgetToStack(widgetItem.id) },
                                                    onOpenPopupWidget = { app -> activePopupWidgetApp = app },
                                                    onAddPopupWidget = { app -> onAddPopupWidgetToApp(app.id) },
                                                    onRemovePopupWidget = { app -> viewModel.removeAppPopupWidget(app.id) },
                                                    onRemoveItem = viewModel::deleteItem,
                                                    onAppInfo = onAppInfo,
                                                    onUninstall = onUninstall,
                                                    onEmptyAreaLongClick = { isEditMode = true },
                                                    widgetHost = widgetHost
                                                )
                                            }
                                        }

                                        // Bottom Search Bar (if configured at bottom for Thumb Zone)
                                        if (uiState.userPreferences.themeConfig.searchBarAtBottom) {
                                            LauncherSearchBar(
                                                engine = uiState.userPreferences.themeConfig.searchEngine,
                                                onSearchClick = { onOpenSearch(uiState.userPreferences.themeConfig.searchEngine) },
                                                onVoiceSearchClick = { viewModel.setAssistantSheetOpen(true) }
                                            )
                                        }
                                    }
                                }
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

        // Widget Stack Modal Dialog
        activeStackForEditing?.let { stackState ->
            val currentStack = uiState.itemsByPage.values.flatten().find { it.id == stackState.id } as? LauncherItem.WidgetStackItem ?: stackState
            WidgetStackModalDialog(
                stack = currentStack,
                onDismiss = { activeStackForEditing = null },
                onAddWidgetToStack = {
                    onAddWidgetToStack(currentStack.id)
                },
                onRemoveWidgetFromStack = { widgetId ->
                    viewModel.removeWidgetFromStack(currentStack, widgetId)
                },
                onDeleteStack = {
                    viewModel.deleteItem(currentStack.id)
                    activeStackForEditing = null
                }
            )
        }

        // Pop-up Widget Dialog
        activePopupWidgetApp?.let { appItem ->
            val currentApp = uiState.itemsByPage.values.flatten().find { it.id == appItem.id } as? LauncherItem.AppItem ?: appItem
            if (currentApp.popupWidgetId != null) {
                PopupWidgetDialog(
                    appItem = currentApp,
                    widgetHost = widgetHost,
                    onDismiss = { activePopupWidgetApp = null },
                    onRemovePopupWidget = {
                        viewModel.removeAppPopupWidget(currentApp.id)
                        activePopupWidgetApp = null
                    },
                    onOpenApp = {
                        activePopupWidgetApp = null
                        handleAppClick(currentApp.packageName, currentApp.activityName)
                    },
                    blurDepth = uiState.userPreferences.themeConfig.blurDepth
                )
            } else {
                activePopupWidgetApp = null
            }
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
