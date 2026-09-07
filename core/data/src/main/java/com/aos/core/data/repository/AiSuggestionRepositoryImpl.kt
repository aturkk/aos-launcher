package com.aos.core.data.repository

import com.aos.core.common.result.Result
import com.aos.core.data.ai.OnDeviceSuggestionEngine
import com.aos.core.data.ai.UsageStatsHelper
import com.aos.core.data.database.dao.AppLaunchEventDao
import com.aos.core.data.database.entity.AppLaunchEventEntity
import com.aos.core.data.preferences.UserPreferencesDataStore
import com.aos.core.domain.model.AppSuggestion
import com.aos.core.domain.repository.AiSuggestionRepository
import com.aos.core.domain.repository.AppListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiSuggestionRepositoryImpl @Inject constructor(
    private val appLaunchEventDao: AppLaunchEventDao,
    private val usageStatsHelper: UsageStatsHelper,
    private val onDeviceSuggestionEngine: OnDeviceSuggestionEngine,
    private val appListRepository: AppListRepository,
    private val userPreferencesDataStore: UserPreferencesDataStore
) : AiSuggestionRepository {

    override fun getSuggestedApps(limit: Int): Flow<List<AppSuggestion>> {
        return combine(
            appListRepository.getInstalledApps(),
            userPreferencesDataStore.isAiSuggestionsEnabled
        ) { appsResult, isEnabled ->
            if (!isEnabled) {
                emptyList()
            } else if (appsResult is Result.Success) {
                onDeviceSuggestionEngine.computeSuggestions(appsResult.data, limit)
            } else {
                emptyList()
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun recordAppLaunch(packageName: String) = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        val event = AppLaunchEventEntity(
            packageName = packageName,
            timestamp = System.currentTimeMillis(),
            hourOfDay = calendar.get(Calendar.HOUR_OF_DAY),
            dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        )
        appLaunchEventDao.insert(event)
    }

    override fun hasUsagePermission(): Boolean {
        return usageStatsHelper.hasUsageStatsPermission()
    }

    override val isAiSuggestionsEnabled: Flow<Boolean> = userPreferencesDataStore.isAiSuggestionsEnabled

    override suspend fun setAiSuggestionsEnabled(enabled: Boolean) {
        userPreferencesDataStore.setAiSuggestionsEnabled(enabled)
    }

    override suspend fun clearLearningData() = withContext(Dispatchers.IO) {
        appLaunchEventDao.clearAll()
    }
}
