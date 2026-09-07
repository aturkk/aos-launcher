package com.aos.core.domain.repository

import com.aos.core.domain.model.DeviceContact

interface ContactSearchRepository {
    suspend fun searchContacts(query: String, maxResults: Int = 5): List<DeviceContact>
}
