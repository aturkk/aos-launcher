package com.aos.core.data.repository

import com.aos.core.common.dispatcher.AosDispatchers
import com.aos.core.common.dispatcher.Dispatcher
import com.aos.core.data.preferences.UserPreferencesDataStore
import com.aos.core.domain.repository.HiddenAppsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HiddenAppsRepositoryImpl @Inject constructor(
    private val preferencesDataStore: UserPreferencesDataStore,
    @Dispatcher(AosDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : HiddenAppsRepository {

    override fun getHiddenPackages(): Flow<Set<String>> {
        return preferencesDataStore.hiddenPackages.flowOn(ioDispatcher)
    }

    override suspend fun hideApp(packageName: String) = withContext(ioDispatcher) {
        preferencesDataStore.hidePackage(packageName)
    }

    override suspend fun unhideApp(packageName: String) = withContext(ioDispatcher) {
        preferencesDataStore.unhidePackage(packageName)
    }

    override fun getVaultPin(): Flow<String?> {
        return preferencesDataStore.vaultPin.flowOn(ioDispatcher)
    }

    override suspend fun setVaultPin(pin: String?) = withContext(ioDispatcher) {
        preferencesDataStore.setVaultPin(pin)
    }

    override suspend fun verifyPin(pin: String): Boolean = withContext(ioDispatcher) {
        val storedPin = preferencesDataStore.vaultPin.firstOrNull()
        storedPin != null && storedPin == pin
    }
}
