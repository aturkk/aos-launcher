package com.aos.core.domain.repository

import com.aos.core.domain.model.AppSuggestion
import kotlinx.coroutines.flow.Flow

interface AiSuggestionRepository {
    fun getSuggestedApps(limit: Int = 5): Flow<List<AppSuggestion>>
    suspend fun recordAppLaunch(packageName: String)
    fun hasUsagePermission(): Boolean
    val isAiSuggestionsEnabled: Flow<Boolean>
    suspend fun setAiSuggestionsEnabled(enabled: Boolean)
    suspend fun clearLearningData()
}
