package com.aos.core.domain.repository

import com.aos.core.common.result.Result
import com.aos.core.domain.model.AppInfo
import kotlinx.coroutines.flow.Flow

interface AppListRepository {
    fun getInstalledApps(): Flow<Result<List<AppInfo>>>
    suspend fun refreshApps()
}
