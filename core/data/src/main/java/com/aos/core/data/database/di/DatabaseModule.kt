package com.aos.core.data.database.di

import android.content.Context
import androidx.room.Room
import com.aos.core.data.database.AosLauncherDatabase
import com.aos.core.data.database.dao.AppLaunchEventDao
import com.aos.core.data.database.dao.LauncherItemDao
import com.aos.core.data.database.dao.PageDao
import com.aos.core.data.database.dao.ProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AosLauncherDatabase =
        Room.databaseBuilder(
            context,
            AosLauncherDatabase::class.java,
            "aos_launcher.db"
        ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideLauncherItemDao(db: AosLauncherDatabase): LauncherItemDao = db.launcherItemDao()

    @Provides
    fun providePageDao(db: AosLauncherDatabase): PageDao = db.pageDao()

    @Provides
    fun provideProfileDao(db: AosLauncherDatabase): ProfileDao = db.profileDao()

    @Provides
    fun provideAppLaunchEventDao(db: AosLauncherDatabase): AppLaunchEventDao = db.appLaunchEventDao()

    @Provides
    fun provideNotificationRecordDao(db: AosLauncherDatabase): com.aos.core.data.database.dao.NotificationRecordDao =
        db.notificationRecordDao()
}
