package com.aos.core.data.repository

import com.aos.core.data.ai.LocalNluCommandParser
import com.aos.core.data.ai.SmartContextEngine
import com.aos.core.domain.model.AssistantResult
import com.aos.core.domain.model.SmartContextCard
import com.aos.core.domain.repository.SmartAssistantRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartAssistantRepositoryImpl @Inject constructor(
    private val smartContextEngine: SmartContextEngine,
    private val localNluCommandParser: LocalNluCommandParser
) : SmartAssistantRepository {

    override fun getSmartContextCard(): Flow<SmartContextCard> {
        return smartContextEngine.getSmartContextCardFlow().flowOn(Dispatchers.IO)
    }

    override suspend fun parseCommand(query: String): AssistantResult = withContext(Dispatchers.Default) {
        localNluCommandParser.parse(query)
    }
}
