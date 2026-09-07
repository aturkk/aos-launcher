package com.aos.core.data.repository

import com.aos.core.common.dispatcher.AosDispatchers
import com.aos.core.common.dispatcher.Dispatcher
import com.aos.core.data.news.RssFeedParser
import com.aos.core.domain.model.NewsArticle
import com.aos.core.domain.model.NewsFeedSource
import com.aos.core.domain.repository.NewsFeedRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsFeedRepositoryImpl @Inject constructor(
    @Dispatcher(AosDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : NewsFeedRepository {

    private val defaultSources = listOf(
        NewsFeedSource(id = "webtekno", name = "Webtekno", url = "https://www.webtekno.com/rss.xml", category = "Teknoloji"),
        NewsFeedSource(id = "shiftdelete", name = "ShiftDelete", url = "https://shiftdelete.net/feed", category = "Teknoloji"),
        NewsFeedSource(id = "bbcturkce", name = "BBC Türkçe", url = "https://feeds.bbci.co.uk/turkce/rss.xml", category = "Gündem"),
        NewsFeedSource(id = "ntv", name = "NTV Gündem", url = "https://www.ntv.com.tr/gundem.rss", category = "Gündem")
    )

    private val _sources = MutableStateFlow(defaultSources)
    private val _articles = MutableStateFlow<List<NewsArticle>>(emptyList())

    override fun getNewsArticles(): Flow<List<NewsArticle>> = _articles.asStateFlow()

    override fun getFeedSources(): Flow<List<NewsFeedSource>> = _sources.asStateFlow()

    override suspend fun refreshNews(): List<NewsArticle> = withContext(ioDispatcher) {
        val activeSources = _sources.value.filter { it.isEnabled }
        val allArticles = mutableListOf<NewsArticle>()

        for (source in activeSources) {
            try {
                val url = URL(source.url)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 7000
                    readTimeout = 7000
                    setRequestProperty("User-Agent", "AOSLauncher/1.4")
                }
                connection.inputStream.use { stream ->
                    val parsed = RssFeedParser.parse(stream, source.name)
                    allArticles.addAll(parsed)
                }
            } catch (_: Exception) {
                // Individual feed failure shouldn't fail entire news feed
            }
        }

        // De-duplicate by title/link and sort
        val distinctArticles = allArticles.distinctBy { it.id }
        _articles.update { distinctArticles }
        distinctArticles
    }

    override suspend fun addCustomSource(name: String, url: String) = withContext(ioDispatcher) {
        val newSource = NewsFeedSource(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "Özel Kaynak" },
            url = url,
            category = "Özel",
            isEnabled = true
        )
        _sources.update { it + newSource }
        refreshNews()
        Unit
    }

    override suspend fun removeSource(sourceId: String) = withContext(ioDispatcher) {
        _sources.update { list -> list.filterNot { it.id == sourceId } }
        refreshNews()
        Unit
    }

    override suspend fun toggleSource(sourceId: String, isEnabled: Boolean) = withContext(ioDispatcher) {
        _sources.update { list ->
            list.map { if (it.id == sourceId) it.copy(isEnabled = isEnabled) else it }
        }
        refreshNews()
        Unit
    }
}
