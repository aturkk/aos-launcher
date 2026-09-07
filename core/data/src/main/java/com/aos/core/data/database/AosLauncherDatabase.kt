package com.aos.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aos.core.data.database.dao.AppLaunchEventDao
import com.aos.core.data.database.dao.LauncherItemDao
import com.aos.core.data.database.dao.NotificationRecordDao
import com.aos.core.data.database.dao.PageDao
import com.aos.core.data.database.dao.ProfileDao
import com.aos.core.data.database.entity.AppLaunchEventEntity
import com.aos.core.data.database.entity.LauncherItemEntity
import com.aos.core.data.database.entity.NotificationRecordEntity
import com.aos.core.data.database.entity.PageEntity
import com.aos.core.data.database.entity.ProfileEntity
import com.aos.core.data.database.entity.ThemeConfigEntity

@Database(
    entities = [
        LauncherItemEntity::class,
        PageEntity::class,
        ProfileEntity::class,
        ThemeConfigEntity::class,
        AppLaunchEventEntity::class,
        NotificationRecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AosLauncherDatabase : RoomDatabase() {
    abstract fun launcherItemDao(): LauncherItemDao
    abstract fun pageDao(): PageDao
    abstract fun profileDao(): ProfileDao
    abstract fun appLaunchEventDao(): AppLaunchEventDao
    abstract fun notificationRecordDao(): NotificationRecordDao
}
