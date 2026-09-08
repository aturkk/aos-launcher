package com.aos.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aos.core.common.result.Result
import com.aos.core.domain.model.AppInfo
import com.aos.core.domain.model.LauncherItem
import com.aos.core.domain.model.PageInfo
import com.aos.core.domain.model.Profile
import com.aos.core.domain.model.AssistantResult
import com.aos.core.domain.repository.LauncherRepository
import com.aos.core.domain.repository.NewsFeedRepository
import com.aos.core.domain.repository.ProfileRepository
import com.aos.core.domain.repository.SmartAssistantRepository
import com.aos.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val launcherRepository: LauncherRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val profileRepository: ProfileRepository,
    private val smartAssistantRepository: SmartAssistantRepository,
    private val newsFeedRepository: NewsFeedRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            smartAssistantRepository.getSmartContextCard().collect { card ->
                _uiState.update { it.copy(smartContextCard = card) }
            }
        }

        viewModelScope.launch {
            newsFeedRepository.getNewsArticles().collect { articles ->
                _uiState.update { it.copy(newsArticles = articles) }
            }
        }

        viewModelScope.launch {
            profileRepository.checkAndApplySchedule()
        }

        viewModelScope.launch {
            combine(
                launcherRepository.getPages(),
                launcherRepository.getDockItems(),
                userPreferencesRepository.userPreferences,
                profileRepository.getProfiles(),
                profileRepository.getActiveProfile()
            ) { pagesResult, dockResult, prefs, profiles, activeProfile ->
                val pages = when (pagesResult) {
                    is Result.Success -> pagesResult.data.ifEmpty {
                        listOf(PageInfo(pageId = 1L, pageIndex = 0, isHomePage = true))
                    }
                    else -> listOf(PageInfo(pageId = 1L, pageIndex = 0, isHomePage = true))
                }
                val dockItems = when (dockResult) {
                    is Result.Success -> dockResult.data
                    else -> emptyList()
                }

                _uiState.update { current ->
                    current.copy(
                        pages = pages,
                        dockItems = dockItems,
                        userPreferences = prefs,
                        profiles = profiles,
                        activeProfile = activeProfile,
                        isLoading = false
                    )
                }

                // Load items for each page
                pages.forEach { page ->
                    loadItemsForPage(page.pageIndex)
                }
            }.collect {}
        }
    }

    private val pageObservationJobs = mutableMapOf<Int, kotlinx.coroutines.Job>()

    private fun loadItemsForPage(pageIndex: Int) {
        if (pageObservationJobs[pageIndex]?.isActive == true) return
        pageObservationJobs[pageIndex] = viewModelScope.launch {
            launcherRepository.getItemsForPage(pageIndex).collect { result ->
                if (result is Result.Success) {
                    _uiState.update { current ->
                        val updatedMap = current.itemsByPage.toMutableMap()
                        updatedMap[pageIndex] = result.data
                        current.copy(itemsByPage = updatedMap)
                    }
                }
            }
        }
    }

    fun switchProfile(profileId: Long) {
        viewModelScope.launch {
            profileRepository.switchProfile(profileId)
        }
    }

    fun isAppBlockedByActiveProfile(packageName: String): Boolean {
        val profile = _uiState.value.activeProfile ?: return false
        return !profile.isAppAllowed(packageName)
    }

    fun findFirstEmptyCellAndAddApp(app: AppInfo, targetPageIndex: Int = 0) {
        viewModelScope.launch {
            val rows = _uiState.value.userPreferences.gridRows
            val cols = _uiState.value.userPreferences.gridColumns
            val currentItems = _uiState.value.itemsByPage[targetPageIndex] ?: emptyList()

            var emptyX = -1
            var emptyY = -1

            searchLoop@ for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val isOccupied = currentItems.any { item ->
                        item.cellX == c && item.cellY == r
                    }
                    if (!isOccupied) {
                        emptyX = c
                        emptyY = r
                        break@searchLoop
                    }
                }
            }

            if (emptyX != -1 && emptyY != -1) {
                val newItem = LauncherItem.AppItem(
                    pageIndex = targetPageIndex,
                    cellX = emptyX,
                    cellY = emptyY,
                    spanX = 1,
                    spanY = 1,
                    packageName = app.packageName,
                    activityName = app.activityName,
                    label = app.label
                )
                launcherRepository.saveItem(newItem)
            } else {
                val newPageIndex = _uiState.value.pages.size
                launcherRepository.addPage(newPageIndex, isHomePage = false)
                val newItem = LauncherItem.AppItem(
                    pageIndex = newPageIndex,
                    cellX = 0,
                    cellY = 0,
                    spanX = 1,
                    spanY = 1,
                    packageName = app.packageName,
                    activityName = app.activityName,
                    label = app.label
                )
                launcherRepository.saveItem(newItem)
            }
        }
    }

    fun placeAppAt(app: AppInfo, cellX: Int, cellY: Int, pageIndex: Int) {
        viewModelScope.launch {
            val rows = _uiState.value.userPreferences.gridRows
            val cols = _uiState.value.userPreferences.gridColumns
            val currentItems = _uiState.value.itemsByPage[pageIndex] ?: emptyList()

            // Check if cell is occupied by any item (considering item span)
            val isOccupied = currentItems.any { item ->
                cellX >= item.cellX && cellX < (item.cellX + item.spanX) &&
                cellY >= item.cellY && cellY < (item.cellY + item.spanY)
            }

            var targetX = cellX
            var targetY = cellY

            if (isOccupied) {
                // Find nearest empty cell on this page
                var minDistance = Double.MAX_VALUE
                var foundEmpty = false

                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        val occupied = currentItems.any { item ->
                            c >= item.cellX && c < (item.cellX + item.spanX) &&
                            r >= item.cellY && r < (item.cellY + item.spanY)
                        }
                        if (!occupied) {
                            val dist = Math.hypot((c - cellX).toDouble(), (r - cellY).toDouble())
                            if (dist < minDistance) {
                                minDistance = dist
                                targetX = c
                                targetY = r
                                foundEmpty = true
                            }
                        }
                    }
                }

                if (!foundEmpty) {
                    findFirstEmptyCellAndAddApp(app, targetPageIndex = pageIndex + 1)
                    return@launch
                }
            }

            val newItem = LauncherItem.AppItem(
                pageIndex = pageIndex,
                cellX = targetX,
                cellY = targetY,
                spanX = 1,
                spanY = 1,
                packageName = app.packageName,
                activityName = app.activityName,
                label = app.label
            )
            launcherRepository.saveItem(newItem)
        }
    }

    fun updateGridDimensions(rows: Int, cols: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setGridDimensions(rows, cols)
            // Re-clamp any items that exceed new boundaries so they don't get pushed off-screen
            _uiState.value.itemsByPage.forEach { (pageIndex, items) ->
                items.forEach { item ->
                    val maxAllowedX = (cols - item.spanX).coerceAtLeast(0)
                    val maxAllowedY = (rows - item.spanY).coerceAtLeast(0)
                    val clampedX = item.cellX.coerceAtMost(maxAllowedX)
                    val clampedY = item.cellY.coerceAtMost(maxAllowedY)
                    if (clampedX != item.cellX || clampedY != item.cellY) {
                        launcherRepository.moveItem(item.id, pageIndex, clampedX, clampedY)
                    }
                }
            }
        }
    }

    fun autoAlignPage(pageIndex: Int) {
        viewModelScope.launch {
            val rows = _uiState.value.userPreferences.gridRows
            val cols = _uiState.value.userPreferences.gridColumns
            val currentItems = _uiState.value.itemsByPage[pageIndex] ?: return@launch
            if (currentItems.isEmpty()) return@launch

            val sorted = currentItems.sortedWith(compareBy({ it.cellY }, { it.cellX }))

            var curC = 0
            var curR = 0

            sorted.forEach { item ->
                if (curC + item.spanX > cols) {
                    curC = 0
                    curR++
                }

                if (curR < rows) {
                    if (item.cellX != curC || item.cellY != curR) {
                        launcherRepository.moveItem(item.id, pageIndex, curC, curR)
                    }
                    curC += item.spanX
                    if (curC >= cols) {
                        curC = 0
                        curR++
                    }
                }
            }
        }
    }

    fun addNewPage() {
        viewModelScope.launch {
            val newIndex = _uiState.value.pages.maxOfOrNull { it.pageIndex }?.plus(1) ?: 1
            launcherRepository.addPage(newIndex, isHomePage = false)
        }
    }

    fun addWidget(appWidgetId: Int, spanX: Int = 2, spanY: Int = 2, targetPageIndex: Int = 0) {
        viewModelScope.launch {
            val rows = _uiState.value.userPreferences.gridRows
            val cols = _uiState.value.userPreferences.gridColumns
            val currentItems = _uiState.value.itemsByPage[targetPageIndex] ?: emptyList()

            var emptyX = -1
            var emptyY = -1

            searchLoop@ for (r in 0..(rows - spanY).coerceAtLeast(0)) {
                for (c in 0..(cols - spanX).coerceAtLeast(0)) {
                    val isOccupied = currentItems.any { item ->
                        val overlapX = c < (item.cellX + item.spanX) && (c + spanX) > item.cellX
                        val overlapY = r < (item.cellY + item.spanY) && (r + spanY) > item.cellY
                        overlapX && overlapY
                    }
                    if (!isOccupied) {
                        emptyX = c
                        emptyY = r
                        break@searchLoop
                    }
                }
            }

            val page = if (emptyX != -1 && emptyY != -1) {
                targetPageIndex
            } else {
                val newPageIndex = _uiState.value.pages.size
                launcherRepository.addPage(newPageIndex, isHomePage = false)
                emptyX = 0
                emptyY = 0
                newPageIndex
            }

            val widget = LauncherItem.WidgetItem(
                pageIndex = page,
                cellX = emptyX,
                cellY = emptyY,
                spanX = spanX,
                spanY = spanY,
                appWidgetId = appWidgetId
            )
            launcherRepository.saveItem(widget)
        }
    }

    fun deletePage(pageIndex: Int) {
        pageObservationJobs.remove(pageIndex)?.cancel()
        viewModelScope.launch {
            launcherRepository.deletePage(pageIndex)
        }
    }

    fun moveItem(itemId: Long, cellX: Int, cellY: Int, pageIndex: Int = 0) {
        viewModelScope.launch {
            launcherRepository.moveItem(itemId, pageIndex, cellX, cellY)
        }
    }

    fun mergeAppsIntoFolder(draggedApp: LauncherItem.AppItem, targetApp: LauncherItem.AppItem) {
        viewModelScope.launch {
            launcherRepository.deleteItem(draggedApp.id)
            launcherRepository.deleteItem(targetApp.id)

            val folder = LauncherItem.FolderItem(
                pageIndex = targetApp.pageIndex,
                cellX = targetApp.cellX,
                cellY = targetApp.cellY,
                title = "Klasör",
                items = listOf(targetApp, draggedApp)
            )
            launcherRepository.saveItem(folder)
        }
    }

    fun addAppToExistingFolder(draggedApp: LauncherItem.AppItem, targetFolder: LauncherItem.FolderItem) {
        viewModelScope.launch {
            launcherRepository.deleteItem(draggedApp.id)
            val updatedItems = targetFolder.items + draggedApp
            launcherRepository.updateFolder(targetFolder.id, targetFolder.title, updatedItems)
        }
    }

    fun updateFolderTitle(folderId: Long, newTitle: String, currentItems: List<LauncherItem.AppItem>) {
        viewModelScope.launch {
            launcherRepository.updateFolder(folderId, newTitle, currentItems)
        }
    }

    fun removeAppFromFolder(folder: LauncherItem.FolderItem, appToRemove: LauncherItem.AppItem) {
        viewModelScope.launch {
            val remainingApps = folder.items.filter {
                if (it.id != 0L && appToRemove.id != 0L) it.id != appToRemove.id
                else it.packageName != appToRemove.packageName
            }
            if (remainingApps.size <= 1) {
                launcherRepository.deleteItem(folder.id)
                if (remainingApps.isNotEmpty()) {
                    val singleApp = remainingApps.first().copy(
                        pageIndex = folder.pageIndex,
                        cellX = folder.cellX,
                        cellY = folder.cellY
                    )
                    launcherRepository.saveItem(singleApp)
                }
            } else {
                launcherRepository.updateFolder(folder.id, folder.title, remainingApps)
            }

            // Restore the removed app back to the home screen
            val restoredAppInfo = AppInfo(
                packageName = appToRemove.packageName,
                activityName = appToRemove.activityName,
                label = appToRemove.label
            )
            findFirstEmptyCellAndAddApp(restoredAppInfo, targetPageIndex = folder.pageIndex)
        }
    }

    fun mergeWidgetsIntoStack(draggedWidget: LauncherItem.WidgetItem, targetWidget: LauncherItem.WidgetItem) {
        viewModelScope.launch {
            launcherRepository.deleteItem(draggedWidget.id)
            launcherRepository.deleteItem(targetWidget.id)

            val stack = LauncherItem.WidgetStackItem(
                pageIndex = targetWidget.pageIndex,
                cellX = targetWidget.cellX,
                cellY = targetWidget.cellY,
                spanX = targetWidget.spanX.coerceAtLeast(draggedWidget.spanX),
                spanY = targetWidget.spanY.coerceAtLeast(draggedWidget.spanY),
                widgets = listOf(targetWidget, draggedWidget)
            )
            launcherRepository.saveItem(stack)
        }
    }

    fun addWidgetToExistingStack(draggedWidget: LauncherItem.WidgetItem, targetStack: LauncherItem.WidgetStackItem) {
        viewModelScope.launch {
            launcherRepository.deleteItem(draggedWidget.id)
            val updatedWidgets = targetStack.widgets + draggedWidget
            launcherRepository.updateWidgetStack(targetStack.id, updatedWidgets)
        }
    }

    fun addWidgetToStackById(targetItemId: Long, newAppWidgetId: Int) {
        viewModelScope.launch {
            val allItems = _uiState.value.itemsByPage.values.flatten()
            val target = allItems.find { it.id == targetItemId } ?: return@launch

            when (target) {
                is LauncherItem.WidgetItem -> {
                    launcherRepository.deleteItem(target.id)
                    val newWidget = LauncherItem.WidgetItem(
                        id = System.currentTimeMillis(),
                        pageIndex = target.pageIndex,
                        cellX = target.cellX,
                        cellY = target.cellY,
                        spanX = target.spanX,
                        spanY = target.spanY,
                        appWidgetId = newAppWidgetId
                    )
                    val stack = LauncherItem.WidgetStackItem(
                        pageIndex = target.pageIndex,
                        cellX = target.cellX,
                        cellY = target.cellY,
                        spanX = target.spanX,
                        spanY = target.spanY,
                        widgets = listOf(target, newWidget)
                    )
                    launcherRepository.saveItem(stack)
                }
                is LauncherItem.WidgetStackItem -> {
                    val newWidget = LauncherItem.WidgetItem(
                        id = System.currentTimeMillis(),
                        pageIndex = target.pageIndex,
                        cellX = target.cellX,
                        cellY = target.cellY,
                        spanX = target.spanX,
                        spanY = target.spanY,
                        appWidgetId = newAppWidgetId
                    )
                    val updatedWidgets = target.widgets + newWidget
                    launcherRepository.updateWidgetStack(target.id, updatedWidgets)
                }
                else -> {}
            }
        }
    }

    fun removeWidgetFromStack(stack: LauncherItem.WidgetStackItem, widgetToRemoveId: Long) {
        viewModelScope.launch {
            val remainingWidgets = stack.widgets.filter { it.id != widgetToRemoveId }
            if (remainingWidgets.isEmpty()) {
                launcherRepository.deleteItem(stack.id)
            } else if (remainingWidgets.size == 1) {
                launcherRepository.deleteItem(stack.id)
                val singleWidget = remainingWidgets.first().copy(
                    pageIndex = stack.pageIndex,
                    cellX = stack.cellX,
                    cellY = stack.cellY,
                    spanX = stack.spanX,
                    spanY = stack.spanY
                )
                launcherRepository.saveItem(singleWidget)
            } else {
                launcherRepository.updateWidgetStack(stack.id, remainingWidgets)
            }
        }
    }

    fun setAppPopupWidget(appItemId: Long, appWidgetId: Int) {
        viewModelScope.launch {
            val allItems = _uiState.value.itemsByPage.values.flatten()
            val target = allItems.find { it.id == appItemId } as? LauncherItem.AppItem ?: return@launch
            val updated = target.copy(popupWidgetId = appWidgetId)
            launcherRepository.saveItem(updated)
        }
    }

    fun removeAppPopupWidget(appItemId: Long) {
        viewModelScope.launch {
            val allItems = _uiState.value.itemsByPage.values.flatten()
            val target = allItems.find { it.id == appItemId } as? LauncherItem.AppItem ?: return@launch
            val updated = target.copy(popupWidgetId = null)
            launcherRepository.saveItem(updated)
        }
    }

    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            launcherRepository.deleteItem(itemId)
        }
    }

    fun setAssistantSheetOpen(open: Boolean) {
        _uiState.update { it.copy(isAssistantSheetOpen = open) }
    }

    fun processAssistantCommand(command: String) {
        viewModelScope.launch {
            val result = smartAssistantRepository.parseCommand(command)
            _uiState.update { it.copy(assistantResult = result) }
        }
    }

    fun clearAssistantResult() {
        _uiState.update { it.copy(assistantResult = null) }
    }

    fun refreshNews() {
        viewModelScope.launch {
            _uiState.update { it.copy(isNewsLoading = true) }
            try {
                val refreshed = newsFeedRepository.refreshNews()
                _uiState.update { it.copy(newsArticles = refreshed, isNewsLoading = false) }
            } catch (_: Exception) {
                _uiState.update { it.copy(isNewsLoading = false) }
            }
        }
    }

    fun setClockWidgetEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setClockWidgetEnabled(enabled)
        }
    }

    fun setSmartContextCardEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setSmartContextCardEnabled(enabled)
        }
    }

    fun setOnboardingCompleted(completed: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setOnboardingCompleted(completed)
        }
    }

    fun checkProfileSchedule() {
        viewModelScope.launch {
            profileRepository.checkAndApplySchedule()
        }
    }
}
