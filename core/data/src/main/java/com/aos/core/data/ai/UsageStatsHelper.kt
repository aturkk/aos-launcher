package com.aos.core.data.ai

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageStatsHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    suspend fun getRecentUsageStats(): Map<String, Long> = withContext(Dispatchers.IO) {
        if (!hasUsageStatsPermission()) return@withContext emptyMap()

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return@withContext emptyMap()

        val endTime = System.currentTimeMillis()
        val startTime = endTime - (1000L * 60 * 60 * 24) // Last 24 hours

        try {
            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )

            val usageMap = mutableMapOf<String, Long>()
            for (usage in stats) {
                if (usage.totalTimeInForeground > 0) {
                    usageMap[usage.packageName] = usage.totalTimeInForeground
                }
            }
            usageMap
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
