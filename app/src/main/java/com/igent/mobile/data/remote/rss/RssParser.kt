package com.igent.mobile.data.remote.rss

import com.igent.mobile.domain.model.FeedItem
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.security.MessageDigest
import java.util.UUID

class RssParser {
    fun parse(xmlString: String, sourceName: String = "RSS Feed"): List<FeedItem> {
        val items = mutableListOf<FeedItem>()
        try {
            val factory = XmlPullParserFactory.newInstance().apply {
                isNamespaceAware = false
            }
            val parser = factory.newPullParser().apply {
                setInput(StringReader(xmlString))
            }

            var eventType = parser.eventType
            var currentTitle: String? = null
            var currentLink: String? = null
            var currentDescription: String? = null
            var currentPubDate: String? = null
            var currentImageUrl: String? = null
            var inItem = false
            var currentTag: String? = null

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name?.lowercase()
                        if (currentTag == "item" || currentTag == "entry") {
                            inItem = true
                            currentTitle = null
                            currentLink = null
                            currentDescription = null
                            currentPubDate = null
                            currentImageUrl = null
                        } else if (inItem) {
                            if (currentTag == "enclosure" || currentTag == "media:content") {
                                val url = parser.getAttributeValue(null, "url")
                                if (!url.isNullOrBlank() && (currentImageUrl == null || currentTag == "enclosure")) {
                                    currentImageUrl = url
                                }
                            }
                        }
                    }
                    XmlPullParser.TEXT -> {
                        val text = parser.text?.trim()
                        if (!text.isNullOrEmpty() && inItem) {
                            when (currentTag) {
                                "title" -> currentTitle = text
                                "link" -> currentLink = text
                                "description", "summary" -> if (currentDescription == null) currentDescription = text
                                "pubdate", "published", "updated" -> if (currentPubDate == null) currentPubDate = text
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        val tagName = parser.name?.lowercase()
                        if (tagName == "item" || tagName == "entry") {
                            inItem = false
                            val titleText = currentTitle ?: "Untitled"
                            val linkText = currentLink ?: ""
                            val descText = currentDescription ?: ""
                            val dateText = currentPubDate ?: System.currentTimeMillis().toString()
                            
                            // Generate stable ID from link or title
                            val uniqueString = if (linkText.isNotBlank()) linkText else titleText
                            val id = generateMd5(uniqueString)

                            items.add(
                                FeedItem(
                                    id = id,
                                    title = titleText,
                                    description = descText,
                                    link = linkText,
                                    pubDate = dateText,
                                    source = sourceName,
                                    imageUrl = currentImageUrl
                                )
                            )
                        }
                        currentTag = null
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return items
    }

    private fun generateMd5(input: String): String {
        try {
            val md = MessageDigest.getInstance("MD5")
            val digested = md.digest(input.toByteArray())
            return digested.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            return UUID.nameUUIDFromBytes(input.toByteArray()).toString()
        }
    }
}
