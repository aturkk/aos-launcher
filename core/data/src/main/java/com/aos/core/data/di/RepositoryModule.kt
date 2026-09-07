package com.aos.core.data.di

import com.aos.core.data.iconpack.IconPackManager
import com.aos.core.data.repository.AiSuggestionRepositoryImpl
import com.aos.core.data.repository.AppListRepositoryImpl
import com.aos.core.data.repository.LauncherRepositoryImpl
import com.aos.core.data.repository.ProfileRepositoryImpl
import com.aos.core.data.repository.UserPreferencesRepositoryImpl
import com.aos.core.domain.repository.AiSuggestionRepository
import com.aos.core.domain.repository.AppListRepository
import com.aos.core.domain.repository.IconPackRepository
import com.aos.core.domain.repository.LauncherRepository
import com.aos.core.domain.repository.ProfileRepository
import com.aos.core.domain.repository.UserPreferencesRepository
import com.aos.core.data.repository.SmartAssistantRepositoryImpl
import com.aos.core.domain.repository.SmartAssistantRepository
import com.aos.core.data.repository.BackupRepositoryImpl
import com.aos.core.domain.repository.BackupRepository
import com.aos.core.data.repository.PluginRepositoryImpl
import com.aos.core.domain.repository.PluginRepository
import com.aos.core.data.repository.NotificationHistoryRepositoryImpl
import com.aos.core.domain.repository.NotificationHistoryRepository
import com.aos.core.data.repository.HiddenAppsRepositoryImpl
import com.aos.core.domain.repository.HiddenAppsRepository
import com.aos.core.data.repository.CrashDiagnosticsRepositoryImpl
import com.aos.core.domain.repository.CrashDiagnosticsRepository
import com.aos.core.data.repository.AppUpdateRepositoryImpl
import com.aos.core.domain.repository.AppUpdateRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindLauncherRepository(impl: LauncherRepositoryImpl): LauncherRepository

    @Binds
    @Singleton
    abstract fun bindAppListRepository(impl: AppListRepositoryImpl): AppListRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindIconPackRepository(impl: IconPackManager): IconPackRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindAiSuggestionRepository(impl: AiSuggestionRepositoryImpl): AiSuggestionRepository

    @Binds
    @Singleton
    abstract fun bindSmartAssistantRepository(impl: SmartAssistantRepositoryImpl): SmartAssistantRepository

    @Binds
    @Singleton
    abstract fun bindBackupRepository(impl: BackupRepositoryImpl): BackupRepository

    @Binds
    @Singleton
    abstract fun bindPluginRepository(impl: PluginRepositoryImpl): PluginRepository

    @Binds
    @Singleton
    abstract fun bindNotificationHistoryRepository(impl: NotificationHistoryRepositoryImpl): NotificationHistoryRepository

    @Binds
    @Singleton
    abstract fun bindHiddenAppsRepository(impl: HiddenAppsRepositoryImpl): HiddenAppsRepository

    @Binds
    @Singleton
    abstract fun bindCrashDiagnosticsRepository(impl: CrashDiagnosticsRepositoryImpl): CrashDiagnosticsRepository

    @Binds
    @Singleton
    abstract fun bindAppUpdateRepository(impl: AppUpdateRepositoryImpl): AppUpdateRepository

    @Binds
    @Singleton
    abstract fun bindContactSearchRepository(impl: com.aos.core.data.repository.ContactSearchRepositoryImpl): com.aos.core.domain.repository.ContactSearchRepository

    @Binds
    @Singleton
    abstract fun bindNewsFeedRepository(impl: com.aos.core.data.repository.NewsFeedRepositoryImpl): com.aos.core.domain.repository.NewsFeedRepository
}

