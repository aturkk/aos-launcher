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
    private val smartAssistantRepository: SmartAssistantRepository
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

    private fun loadItemsForPage(pageIndex: Int) {
        viewModelScope.launch {
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

    fun addNewPage() {
        viewModelScope.launch {
            val newIndex = _uiState.value.pages.size
            launcherRepository.addPage(newIndex, isHomePage = false)
        }
    }

    fun deletePage(pageIndex: Int) {
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

    fun updateFolderTitle(folderId: Long, newTitle: String, currentItems: List<LauncherItem.AppItem>) {
        viewModelScope.launch {
            launcherRepository.updateFolder(folderId, newTitle, currentItems)
        }
    }

    fun removeAppFromFolder(folder: LauncherItem.FolderItem, appToRemove: LauncherItem.AppItem) {
        viewModelScope.launch {
            val remainingApps = folder.items.filter { it.id != appToRemove.id }
            if (remainingApps.size == 1) {
                launcherRepository.deleteItem(folder.id)
                val singleApp = remainingApps.first().copy(
                    pageIndex = folder.pageIndex,
                    cellX = folder.cellX,
                    cellY = folder.cellY
                )
                launcherRepository.saveItem(singleApp)
            } else {
                launcherRepository.updateFolder(folder.id, folder.title, remainingApps)
            }
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

    fun setOnboardingCompleted(completed: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setOnboardingCompleted(completed)
        }
    }
}
