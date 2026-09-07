package com.aos.core.data.news

import android.util.Xml
import com.aos.core.domain.model.NewsArticle
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.util.UUID
import java.util.regex.Pattern

object RssFeedParser {

    private val htmlTagPattern = Pattern.compile("<(\"[^\"]*\"|'[^']*'|[^'\">])*>")
    private val imgTagPattern = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE)

    fun parse(inputStream: InputStream, sourceName: String): List<NewsArticle> {
        val articles = mutableListOf<NewsArticle>()
        try {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)

            var eventType = parser.eventType
            var currentTitle: String? = null
            var currentLink: String? = null
            var currentDescription: String? = null
            var currentPubDate: String? = null
            var currentImageUrl: String? = null
            var insideItem = false

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagName = parser.name?.lowercase() ?: ""

                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (tagName == "item" || tagName == "entry") {
                            insideItem = true
                            currentTitle = null
                            currentLink = null
                            currentDescription = null
                            currentPubDate = null
                            currentImageUrl = null
                        } else if (insideItem) {
                            when (tagName) {
                                "title" -> currentTitle = parser.nextText()?.trim()
                                "link" -> {
                                    val href = parser.getAttributeValue(null, "href")
                                    currentLink = if (!href.isNullOrBlank()) href else parser.nextText()?.trim()
                                }
                                "description", "summary", "content" -> {
                                    val raw = parser.nextText()?.trim() ?: ""
                                    if (currentImageUrl == null) {
                                        val matcher = imgTagPattern.matcher(raw)
                                        if (matcher.find()) {
                                            currentImageUrl = matcher.group(1)
                                        }
                                    }
                                    currentDescription = htmlTagPattern.matcher(raw).replaceAll("").trim()
                                }
                                "pubdate", "published", "updated" -> {
                                    currentPubDate = parser.nextText()?.trim()
                                }
                                "enclosure", "media:content", "media:thumbnail" -> {
                                    val url = parser.getAttributeValue(null, "url")
                                    val type = parser.getAttributeValue(null, "type")
                                    if (!url.isNullOrBlank() && (type?.startsWith("image") == true || url.contains(".jpg") || url.contains(".png") || url.contains(".webp"))) {
                                        currentImageUrl = url
                                    }
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if ((tagName == "item" || tagName == "entry") && insideItem) {
                            if (!currentTitle.isNullOrBlank() && !currentLink.isNullOrBlank()) {
                                articles.add(
                                    NewsArticle(
                                        id = UUID.nameUUIDFromBytes((currentLink + currentTitle).toByteArray()).toString(),
                                        title = currentTitle,
                                        description = currentDescription?.take(220) ?: "",
                                        source = sourceName,
                                        link = currentLink,
                                        pubDate = currentPubDate ?: "",
                                        imageUrl = currentImageUrl
                                    )
                                )
                            }
                            insideItem = false
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (_: Exception) {
            // Ignore parse errors on individual corrupted feeds
        }
        return articles
    }
}
