package com.aos.launcher.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.aos.core.domain.model.NotificationRecord
import com.aos.core.domain.repository.NotificationHistoryRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NotificationListenerEntryPoint {
    fun notificationHistoryRepository(): NotificationHistoryRepository
}

class AosNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private val _notificationCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
        val notificationCounts: StateFlow<Map<String, Int>> = _notificationCounts.asStateFlow()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        refreshNotifications()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        refreshNotifications()
        if (sbn == null || sbn.isOngoing) return

        val extras = sbn.notification?.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val packageName = sbn.packageName ?: return

        val appName = try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }

        if (title.isNotBlank() || text.isNotBlank()) {
            serviceScope.launch {
                try {
                    val entryPoint = EntryPointAccessors.fromApplication(
                        applicationContext,
                        NotificationListenerEntryPoint::class.java
                    )
                    entryPoint.notificationHistoryRepository().recordNotification(
                        NotificationRecord(
                            packageName = packageName,
                            appName = appName,
                            title = title,
                            content = text,
                            timestamp = if (sbn.postTime > 0) sbn.postTime else System.currentTimeMillis()
                        )
                    )
                } catch (e: Exception) {
                    // Ignore recording failure if service unbinds or DB unavailable
                }
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        refreshNotifications()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun refreshNotifications() {
        try {
            val active = activeNotifications ?: return
            val counts = mutableMapOf<String, Int>()
            for (sbn in active) {
                if (!sbn.isOngoing) {
                    val pkg = sbn.packageName
                    counts[pkg] = (counts[pkg] ?: 0) + 1
                }
            }
            _notificationCounts.value = counts
        } catch (e: Exception) {
            // Service not yet connected or permission pending
        }
    }
}

