package com.aos.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aos.core.data.database.entity.AppLaunchEventEntity

@Dao
interface AppLaunchEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: AppLaunchEventEntity): Long

    @Query("SELECT * FROM app_launch_events WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    suspend fun getRecentLaunches(sinceTimestamp: Long): List<AppLaunchEventEntity>

    @Query("SELECT * FROM app_launch_events WHERE hourOfDay BETWEEN :minHour AND :maxHour ORDER BY timestamp DESC")
    suspend fun getLaunchesByHour(minHour: Int, maxHour: Int): List<AppLaunchEventEntity>

    @Query("DELETE FROM app_launch_events")
    suspend fun clearAll()
}
