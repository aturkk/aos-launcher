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

    private fun hashPin(pin: String): String {
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val digest = md.digest(("aos_vault_salt_$pin").toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    override fun getVaultPin(): Flow<String?> {
        return preferencesDataStore.vaultPin.flowOn(ioDispatcher)
    }

    override suspend fun setVaultPin(pin: String?) = withContext(ioDispatcher) {
        val hashed = if (pin != null) hashPin(pin) else null
        preferencesDataStore.setVaultPin(hashed)
    }

    override suspend fun verifyPin(pin: String): Boolean = withContext(ioDispatcher) {
        val storedPin = preferencesDataStore.vaultPin.firstOrNull() ?: return@withContext false
        val hashedInput = hashPin(pin)
        if (storedPin == hashedInput) {
            true
        } else if (storedPin == pin) {
            // Upgrade legacy plaintext PIN to hashed
            preferencesDataStore.setVaultPin(hashedInput)
            true
        } else {
            false
        }
    }
}
