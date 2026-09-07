package com.aos.core.domain.model

enum class ProfileType {
    Normal,
    Work,
    Focus,
    Night,
    Kids,
    Car
}

data class Profile(
    val id: Long = 0L,
    val name: String,
    val type: ProfileType = ProfileType.Normal,
    val isActive: Boolean = false,
    val blockedPackages: List<String> = emptyList(),
    val allowedPackages: List<String> = emptyList(),
    val pinCode: String? = null,
    val isScheduleEnabled: Boolean = false,
    val startHour: Int = 9,
    val startMinute: Int = 0,
    val endHour: Int = 18,
    val endMinute: Int = 0,
    val iconName: String = "default"
) {
    fun isAppAllowed(packageName: String): Boolean {
        return when (type) {
            ProfileType.Kids -> {
                if (allowedPackages.isEmpty()) true else allowedPackages.contains(packageName)
            }
            ProfileType.Work, ProfileType.Focus -> {
                !blockedPackages.contains(packageName)
            }
            else -> true
        }
    }
}
