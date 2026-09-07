package com.aos.feature.home

import com.aos.core.domain.model.AssistantResult
import com.aos.core.domain.model.LauncherItem
import com.aos.core.domain.model.NewsArticle
import com.aos.core.domain.model.PageInfo
import com.aos.core.domain.model.Profile
import com.aos.core.domain.model.SmartContextCard
import com.aos.core.domain.repository.UserPreferences

data class HomeUiState(
    val pages: List<PageInfo> = listOf(PageInfo(pageId = 1L, pageIndex = 0, isHomePage = true)),
    val itemsByPage: Map<Int, List<LauncherItem>> = emptyMap(),
    val dockItems: List<LauncherItem> = emptyList(),
    val userPreferences: UserPreferences = UserPreferences(),
    val profiles: List<Profile> = emptyList(),
    val activeProfile: Profile? = null,
    val smartContextCard: SmartContextCard? = null,
    val assistantResult: AssistantResult? = null,
    val isAssistantSheetOpen: Boolean = false,
    val newsArticles: List<NewsArticle> = emptyList(),
    val isNewsLoading: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

