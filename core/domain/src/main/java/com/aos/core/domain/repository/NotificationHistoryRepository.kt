package com.aos.core.domain.repository

import com.aos.core.domain.model.NotificationRecord
import kotlinx.coroutines.flow.Flow

interface NotificationHistoryRepository {
    fun getNotificationHistory(): Flow<List<NotificationRecord>>
    suspend fun recordNotification(record: NotificationRecord)
    suspend fun clearHistory()
    suspend fun deleteNotification(id: Long)
}
