package com.aos.core.data.ai

import com.aos.core.data.database.dao.AppLaunchEventDao
import com.aos.core.domain.model.AppInfo
import com.aos.core.domain.model.AppSuggestion
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnDeviceSuggestionEngine @Inject constructor(
    private val appLaunchEventDao: AppLaunchEventDao,
    private val usageStatsHelper: UsageStatsHelper
) {

    suspend fun computeSuggestions(
        installedApps: List<AppInfo>,
        limit: Int = 5
    ): List<AppSuggestion> {
        if (installedApps.isEmpty()) return emptyList()

        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

        val (timeReason, minHour, maxHour) = when (currentHour) {
            in 6..11 -> Triple("Sabah Rutini", 6, 11)
            in 12..17 -> Triple("İş & Verimlilik", 12, 17)
            in 18..22 -> Triple("Akşam Önerisi", 18, 22)
            else -> Triple("Gece Modu", 23, 5)
        }

        // 1. Query local launch events in current hour window
        val contextLaunches = if (minHour <= maxHour) {
            appLaunchEventDao.getLaunchesByHour(minHour, maxHour)
        } else {
            // Wraps around midnight (23 to 5)
            appLaunchEventDao.getLaunchesByHour(23, 23) + appLaunchEventDao.getLaunchesByHour(0, 5)
        }

        val contextCounts = contextLaunches.groupingBy { it.packageName }.eachCount()

        // 2. Query usage stats (foreground time in ms)
        val usageStats = usageStatsHelper.getRecentUsageStats()

        // 3. Score each installed app
        val scoredList = installedApps.map { app ->
            val pkg = app.packageName

            val contextCount = contextCounts[pkg] ?: 0
            val foregroundTime = usageStats[pkg] ?: 0L

            // Context weight: 4.0, Usage time weight: normalized log
            val contextScore = contextCount * 4.0f
            val usageScore = if (foregroundTime > 0) {
                (kotlin.math.ln(foregroundTime.toDouble() / 1000.0 + 1.0)).toFloat()
            } else {
                0f
            }

            val totalScore = contextScore + usageScore

            val reason = if (contextCount > 1) {
                timeReason
            } else if (usageScore > 5f) {
                "Sık Kullanılan"
            } else {
                "Önerilen"
            }

            AppSuggestion(
                packageName = pkg,
                activityName = app.activityName,
                label = app.label,
                score = totalScore,
                reason = reason
            )
        }

        // Sort descending by score, take top N
        val suggestions = scoredList
            .filter { it.score > 0f }
            .sortedByDescending { it.score }
            .take(limit)

        // Fallback: If not enough data yet, return first few common apps
        return if (suggestions.size < limit) {
            val existingPkgs = suggestions.map { it.packageName }.toSet()
            val fallbacks = installedApps
                .filter { !existingPkgs.contains(it.packageName) }
                .take(limit - suggestions.size)
                .map {
                    AppSuggestion(
                        packageName = it.packageName,
                        activityName = it.activityName,
                        label = it.label,
                        score = 0.5f,
                        reason = "Sık Kullanılan"
                    )
                }
            suggestions + fallbacks
        } else {
            suggestions
        }
    }
}
