package com.aos.core.domain.repository

import com.aos.core.domain.model.AssistantResult
import com.aos.core.domain.model.SmartContextCard
import kotlinx.coroutines.flow.Flow

interface SmartAssistantRepository {
    fun getSmartContextCard(): Flow<SmartContextCard>
    suspend fun parseCommand(query: String): AssistantResult
}
