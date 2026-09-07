package com.aos.core.data.repository

import com.aos.core.common.dispatcher.AosDispatchers
import com.aos.core.common.dispatcher.Dispatcher
import com.aos.core.data.search.DeviceContactSearcher
import com.aos.core.domain.model.DeviceContact
import com.aos.core.domain.repository.ContactSearchRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactSearchRepositoryImpl @Inject constructor(
    private val contactSearcher: DeviceContactSearcher,
    @Dispatcher(AosDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : ContactSearchRepository {

    override suspend fun searchContacts(query: String, maxResults: Int): List<DeviceContact> {
        return withContext(ioDispatcher) {
            contactSearcher.searchContacts(query, maxResults)
        }
    }
}
