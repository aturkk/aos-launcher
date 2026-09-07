package com.aos.core.domain.model

data class DeviceContact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val photoUri: String? = null
)
