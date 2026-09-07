package com.aos.core.domain.repository

import com.aos.core.common.result.Result
import com.aos.core.domain.model.BackupPayload
import com.aos.core.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

interface BackupRepository {
    suspend fun createBackupPayload(): BackupPayload
    suspend fun exportEncryptedBackup(password: String): Result<ByteArray>
    suspend fun importEncryptedBackup(encryptedBytes: ByteArray, password: String): Result<Unit>
    suspend fun restorePayload(payload: BackupPayload): Result<Unit>
    fun getSyncStatus(): Flow<SyncStatus>
    suspend fun syncWithCloud(): Result<Unit>
}
