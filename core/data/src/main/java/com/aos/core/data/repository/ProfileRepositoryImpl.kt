package com.aos.core.data.repository

import com.aos.core.data.database.dao.ProfileDao
import com.aos.core.data.database.entity.ProfileEntity
import com.aos.core.domain.model.Profile
import com.aos.core.domain.model.ProfileType
import com.aos.core.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val profileDao: ProfileDao
) : ProfileRepository {

    private suspend fun ensureDefaultProfiles() {
        if (profileDao.getProfileCount() == 0) {
            val defaults = listOf(
                ProfileEntity(
                    id = 1L,
                    name = "Standart",
                    type = ProfileType.Normal.name,
                    isActive = true,
                    blockedPackagesJson = "[]",
                    allowedPackagesJson = "[]",
                    iconName = "default"
                ),
                ProfileEntity(
                    id = 2L,
                    name = "İş Modu",
                    type = ProfileType.Work.name,
                    isActive = false,
                    blockedPackagesJson = "[\"com.facebook.katana\",\"com.instagram.android\",\"com.zhiliaoapp.musically\",\"com.twitter.android\"]",
                    allowedPackagesJson = "[]",
                    iconName = "work"
                ),
                ProfileEntity(
                    id = 3L,
                    name = "Odak Modu",
                    type = ProfileType.Focus.name,
                    isActive = false,
                    blockedPackagesJson = "[\"com.facebook.katana\",\"com.instagram.android\",\"com.zhiliaoapp.musically\",\"com.twitter.android\",\"com.google.android.youtube\",\"com.netflix.mediaclient\"]",
                    allowedPackagesJson = "[]",
                    iconName = "focus"
                ),
                ProfileEntity(
                    id = 4L,
                    name = "Gece Modu",
                    type = ProfileType.Night.name,
                    isActive = false,
                    blockedPackagesJson = "[]",
                    allowedPackagesJson = "[]",
                    iconName = "night"
                ),
                ProfileEntity(
                    id = 5L,
                    name = "Çocuk Modu",
                    type = ProfileType.Kids.name,
                    isActive = false,
                    blockedPackagesJson = "[]",
                    allowedPackagesJson = "[]",
                    pinCode = "1234",
                    iconName = "child"
                ),
                ProfileEntity(
                    id = 6L,
                    name = "Araç Modu",
                    type = ProfileType.Car.name,
                    isActive = false,
                    blockedPackagesJson = "[]",
                    allowedPackagesJson = "[]",
                    iconName = "car"
                )
            )
            profileDao.insertAll(defaults)
        }
    }

    override fun getProfiles(): Flow<List<Profile>> {
        return profileDao.getAllProfiles()
            .onStart { ensureDefaultProfiles() }
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    override fun getActiveProfile(): Flow<Profile?> {
        return profileDao.getActiveProfile()
            .onStart { ensureDefaultProfiles() }
            .map { entity ->
                entity?.toDomain()
            }
    }

    override suspend fun switchProfile(profileId: Long) {
        profileDao.switchActiveProfile(profileId)
    }

    override suspend fun updateProfile(profile: Profile) {
        profileDao.update(profile.toEntity())
    }

    override suspend fun createProfile(profile: Profile): Long {
        return profileDao.insert(profile.toEntity())
    }

    override suspend fun deleteProfile(profileId: Long) {
        profileDao.deleteById(profileId)
    }

    override suspend fun checkAndApplySchedule(): Boolean {
        ensureDefaultProfiles()
        val allEntities = profileDao.getAllProfilesList()
        val allProfiles = allEntities.map { it.toDomain() }
        val scheduledProfiles = allProfiles.filter { it.isScheduleEnabled }
        if (scheduledProfiles.isEmpty()) return false

        val calendar = java.util.Calendar.getInstance()
        val currentMinutes = calendar.get(java.util.Calendar.HOUR_OF_DAY) * 60 + calendar.get(java.util.Calendar.MINUTE)

        val targetProfile = scheduledProfiles.firstOrNull { profile ->
            val startMinutes = profile.startHour * 60 + profile.startMinute
            val endMinutes = profile.endHour * 60 + profile.endMinute
            if (startMinutes <= endMinutes) {
                currentMinutes in startMinutes..endMinutes
            } else {
                currentMinutes >= startMinutes || currentMinutes <= endMinutes
            }
        }

        val activeProfile = allProfiles.find { it.isActive }

        if (targetProfile != null) {
            if (activeProfile?.id != targetProfile.id) {
                // If active profile is protected with a PIN, do not switch automatically
                if (activeProfile?.pinCode.isNullOrBlank()) {
                    switchProfile(targetProfile.id)
                    return true
                }
            }
        }
        return false
    }

    private fun ProfileEntity.toDomain(): Profile {
        val profileType = try {
            ProfileType.valueOf(type)
        } catch (e: Exception) {
            ProfileType.Normal
        }
        return Profile(
            id = id,
            name = name,
            type = profileType,
            isActive = isActive,
            blockedPackages = jsonToStringList(blockedPackagesJson),
            allowedPackages = jsonToStringList(allowedPackagesJson),
            pinCode = pinCode,
            isScheduleEnabled = isScheduleEnabled,
            startHour = startHour,
            startMinute = startMinute,
            endHour = endHour,
            endMinute = endMinute,
            iconName = iconName
        )
    }

    private fun Profile.toEntity(): ProfileEntity {
        return ProfileEntity(
            id = id,
            name = name,
            type = type.name,
            isActive = isActive,
            blockedPackagesJson = stringListToJson(blockedPackages),
            allowedPackagesJson = stringListToJson(allowedPackages),
            pinCode = pinCode,
            isScheduleEnabled = isScheduleEnabled,
            startHour = startHour,
            startMinute = startMinute,
            endHour = endHour,
            endMinute = endMinute,
            iconName = iconName
        )
    }

    private fun stringListToJson(list: List<String>): String {
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        return arr.toString()
    }

    private fun jsonToStringList(json: String): List<String> {
        if (json.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(json)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                list.add(arr.getString(i))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
}
