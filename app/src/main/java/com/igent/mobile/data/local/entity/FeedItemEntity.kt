package com.igent.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.igent.mobile.domain.model.FeedItem

@Entity(tableName = "feed_items")
data class FeedItemEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val link: String,
    val pubDate: String,
    val source: String,
    val imageUrl: String?
) {
    fun toDomain(): FeedItem = FeedItem(
        id = id,
        title = title,
        description = description,
        link = link,
        pubDate = pubDate,
        source = source,
        imageUrl = imageUrl
    )

    companion object {
        fun fromDomain(feedItem: FeedItem): FeedItemEntity = FeedItemEntity(
            id = feedItem.id,
            title = feedItem.title,
            description = feedItem.description,
            link = feedItem.link,
            pubDate = feedItem.pubDate,
            source = feedItem.source,
            imageUrl = feedItem.imageUrl
        )
    }
}
