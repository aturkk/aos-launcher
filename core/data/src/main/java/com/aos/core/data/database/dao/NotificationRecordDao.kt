package com.aos.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aos.core.data.database.entity.NotificationRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationRecordDao {

    @Query("SELECT * FROM notification_history ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NotificationRecordEntity): Long

    @Query("DELETE FROM notification_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM notification_history")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM notification_history")
    suspend fun getCount(): Int
}
