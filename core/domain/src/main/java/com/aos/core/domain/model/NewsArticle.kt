package com.aos.core.domain.model

data class NewsArticle(
    val id: String,
    val title: String,
    val description: String,
    val source: String,
    val link: String,
    val pubDate: String,
    val imageUrl: String? = null
)

data class NewsFeedSource(
    val id: String,
    val name: String,
    val url: String,
    val category: String = "Genel",
    val isEnabled: Boolean = true
)
