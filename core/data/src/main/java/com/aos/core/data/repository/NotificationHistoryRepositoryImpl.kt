package com.aos.core.data.repository

import com.aos.core.common.dispatcher.AosDispatchers
import com.aos.core.common.dispatcher.Dispatcher
import com.aos.core.data.database.dao.NotificationRecordDao
import com.aos.core.data.database.entity.NotificationRecordEntity
import com.aos.core.domain.model.NotificationRecord
import com.aos.core.domain.repository.NotificationHistoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHistoryRepositoryImpl @Inject constructor(
    private val notificationRecordDao: NotificationRecordDao,
    @Dispatcher(AosDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : NotificationHistoryRepository {

    override fun getNotificationHistory(): Flow<List<NotificationRecord>> {
        return notificationRecordDao.getAllNotifications()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }

    override suspend fun recordNotification(record: NotificationRecord) = withContext(ioDispatcher) {
        notificationRecordDao.insert(NotificationRecordEntity.fromDomain(record))
        Unit
    }

    override suspend fun clearHistory() = withContext(ioDispatcher) {
        notificationRecordDao.clearAll()
    }

    override suspend fun deleteNotification(id: Long) = withContext(ioDispatcher) {
        notificationRecordDao.deleteById(id)
    }
}
