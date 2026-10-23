package com.igent.mobile.data.repository

import com.igent.mobile.data.local.dao.FeedItemDao
import com.igent.mobile.data.local.entity.FeedItemEntity
import com.igent.mobile.domain.model.FeedItem
import com.igent.mobile.domain.repository.RssRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RssRepositoryImpl @Inject constructor(
    private val feedItemDao: FeedItemDao
) : RssRepository {

    override suspend fun getFeedItems(): Result<List<FeedItem>> {
        return try {
            val sampleItems = listOf(
                FeedItem(
                    id = "1",
                    title = "PSX Hits Record High Amid Strong Economic Indicators",
                    description = "The Pakistan Stock Exchange saw unprecedented volumes today as investors cheered positive fiscal updates.",
                    link = "https://example.com/news/1",
                    pubDate = "2026-10-24 10:00:00",
                    source = "Business Recorder",
                    imageUrl = null
                ),
                FeedItem(
                    id = "2",
                    title = "Tech Sector Leads Growth in Mobile AI Integration",
                    description = "Local developers are rapidly adopting AI-driven clients for mobile productivity and data analytics.",
                    link = "https://example.com/news/2",
                    pubDate = "2026-10-24 09:30:00",
                    source = "TechInAsia",
                    imageUrl = null
                )
            )
            feedItemDao.insertFeedItems(sampleItems.map { FeedItemEntity.fromDomain(it) })
            Result.success(sampleItems)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshFeed(): Result<Unit> {
        return getFeedItems().map {}
    }

    fun getFeedItemsFlow(): Flow<List<FeedItem>> {
        return feedItemDao.getAllFeedItems().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
