package com.aos.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface HiddenAppsRepository {
    fun getHiddenPackages(): Flow<Set<String>>
    suspend fun hideApp(packageName: String)
    suspend fun unhideApp(packageName: String)
    fun getVaultPin(): Flow<String?>
    suspend fun setVaultPin(pin: String?)
    suspend fun verifyPin(pin: String): Boolean
}
