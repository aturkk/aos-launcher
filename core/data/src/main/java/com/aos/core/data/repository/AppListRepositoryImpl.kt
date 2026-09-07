package com.aos.core.data.repository

import android.content.Context
import android.content.Intent
import com.aos.core.common.dispatcher.AosDispatchers
import com.aos.core.common.dispatcher.Dispatcher
import com.aos.core.common.result.Result
import com.aos.core.common.result.asResult
import com.aos.core.domain.model.AppInfo
import com.aos.core.domain.repository.AppListRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class AppListRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appCategorizer: com.aos.core.data.category.AppCategorizer,
    @Dispatcher(AosDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : AppListRepository {

    private val refreshTrigger = MutableStateFlow(0L)

    override fun getInstalledApps(): Flow<Result<List<AppInfo>>> =
        refreshTrigger.flatMapLatest {
            flow {
                val apps = queryInstalledApps()
                emit(apps)
            }
        }.asResult().flowOn(ioDispatcher)

    override suspend fun refreshApps() {
        refreshTrigger.value = System.currentTimeMillis()
    }

    private suspend fun queryInstalledApps(): List<AppInfo> = withContext(ioDispatcher) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val activities = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(intent, android.content.pm.PackageManager.ResolveInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, 0)
        }

        activities.mapNotNull { resolveInfo ->
            val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
            val label = resolveInfo.loadLabel(pm).toString()
            val category = appCategorizer.categorize(activityInfo.packageName, label)
            AppInfo(
                packageName = activityInfo.packageName,
                activityName = activityInfo.name,
                label = label,
                isSystemApp = (activityInfo.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0,
                category = category
            )
        }.sortedBy { it.label.lowercase() }
    }
}
