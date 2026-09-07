package com.aos.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aos.core.domain.model.NotificationRecord

@Entity(tableName = "notification_history")
data class NotificationRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val packageName: String,
    val appName: String,
    val title: String,
    val content: String,
    val timestamp: Long
) {
    fun toDomain(): NotificationRecord = NotificationRecord(
        id = id,
        packageName = packageName,
        appName = appName,
        title = title,
        content = content,
        timestamp = timestamp
    )

    companion object {
        fun fromDomain(record: NotificationRecord): NotificationRecordEntity = NotificationRecordEntity(
            id = record.id,
            packageName = record.packageName,
            appName = record.appName,
            title = record.title,
            content = record.content,
            timestamp = record.timestamp
        )
    }
}
