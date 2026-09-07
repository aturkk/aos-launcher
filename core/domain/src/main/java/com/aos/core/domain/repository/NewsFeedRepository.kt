package com.aos.core.domain.repository

import com.aos.core.domain.model.NewsArticle
import com.aos.core.domain.model.NewsFeedSource
import kotlinx.coroutines.flow.Flow

interface NewsFeedRepository {
    fun getNewsArticles(): Flow<List<NewsArticle>>
    suspend fun refreshNews(): List<NewsArticle>
    fun getFeedSources(): Flow<List<NewsFeedSource>>
    suspend fun addCustomSource(name: String, url: String)
    suspend fun removeSource(sourceId: String)
    suspend fun toggleSource(sourceId: String, isEnabled: Boolean)
}
