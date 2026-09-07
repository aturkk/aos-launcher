package com.aos.core.domain.repository

import com.aos.core.domain.model.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getProfiles(): Flow<List<Profile>>
    fun getActiveProfile(): Flow<Profile?>
    suspend fun switchProfile(profileId: Long)
    suspend fun updateProfile(profile: Profile)
    suspend fun createProfile(profile: Profile): Long
    suspend fun deleteProfile(profileId: Long)
}
