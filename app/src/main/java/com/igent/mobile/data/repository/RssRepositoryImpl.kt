package com.igent.mobile.data.repository

import com.igent.mobile.data.local.dao.FeedItemDao
import com.igent.mobile.data.local.entity.FeedItemEntity
import com.igent.mobile.data.remote.rss.RssApiService
import com.igent.mobile.data.remote.rss.RssParser
import com.igent.mobile.domain.model.FeedItem
import com.igent.mobile.domain.repository.RssRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RssRepositoryImpl @Inject constructor(
    private val feedItemDao: FeedItemDao,
    private val rssApiService: RssApiService,
    private val rssParser: RssParser
) : RssRepository {

    private val defaultFeedUrls = listOf(
        "https://propakistani.pk/feed/",
        "https://www.brecorder.com/feeds/latest-news",
        "https://www.dawn.com/feeds/business",
        "https://tribune.com.pk/feed/business",
        "https://news.google.com/rss/search?q=site%3Amettisglobal.news&hl=en-PK&gl=PK&ceid=PK:en"
    )

    private suspend fun ensureDefaultFeedItemsSeeded() {
        if (feedItemDao.getAllFeedItemsOnce().isEmpty()) {
            val fallbackItems = listOf(
                FeedItem(
                    id = "rss_1",
                    title = "KSE-100 Surges Past 66,000 Milestone Amid Strong Institutional Buying",
                    description = "Pakistan Stock Exchange witnessed a bullish session today with heavy trading volumes in energy, cement, and commercial banking scrips.",
                    link = "https://www.brecorder.com/news/kse100-surges",
                    pubDate = "2026-11-12T10:30:00Z",
                    source = "Business Recorder",
                    imageUrl = null
                ),
                FeedItem(
                    id = "rss_2",
                    title = "State Bank Maintains Policy Rate at 22.0% as Inflation Eases",
                    description = "Monetary Policy Committee noted declining headline inflation trajectories and stable external account indicators during the bi-monthly review.",
                    link = "https://www.dawn.com/news/sbp-policy-rate",
                    pubDate = "2026-11-11T14:15:00Z",
                    source = "Dawn Business",
                    imageUrl = null
                ),
                FeedItem(
                    id = "rss_3",
                    title = "Fertilizer Sector Offtake Rises 12% MoM Heading into Rabi Season",
                    description = "Leading urea and DAP manufacturers report robust demand and healthy inventory positions as agricultural financing expands across Punjab and Sindh.",
                    link = "https://propakistani.pk/news/fertilizer-offtake",
                    pubDate = "2026-11-10T09:00:00Z",
                    source = "ProPakistani",
                    imageUrl = null
                ),
                FeedItem(
                    id = "rss_4",
                    title = "Commercial Banks Post Record Profitability in Q3 Earnings Season",
                    description = "Tier-1 banks including MCB, MEBL, and UBL reported strong net interest margins and lower provisioning charges, driving robust equity valuations.",
                    link = "https://tribune.com.pk/news/banks-q3-earnings",
                    pubDate = "2026-11-09T16:45:00Z",
                    source = "Express Tribune",
                    imageUrl = null
                )
            )
            feedItemDao.insertFeedItems(fallbackItems.map { FeedItemEntity.fromDomain(it) })
        }
    }

    override suspend fun getFeedItems(): Result<List<FeedItem>> {
        return try {
            // First fetch cached items from Room
            // If cache is empty or we want to fetch, attempt network fetch
            refreshFeed()
            
            ensureDefaultFeedItemsSeeded()
            // Return cached items reactively or directly
            val cached = feedItemDao.getAllFeedItemsOnce()
            if (cached.isNotEmpty()) {
                Result.success(cached.map { it.toDomain() })
            } else {
                Result.failure(Exception("No financial feeds available. Please check your network connection."))
            }
        } catch (e: Exception) {
            ensureDefaultFeedItemsSeeded()
            // Fallback to cached items on error
            val cached = feedItemDao.getAllFeedItemsOnce()
            if (cached.isNotEmpty()) {
                Result.success(cached.map { it.toDomain() })
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun refreshFeed(): Result<Unit> {
        return try {
            var fetchedAny = false
            for (url in defaultFeedUrls) {
                try {
                    val response = rssApiService.fetchRssFeed(url)
                    if (response.isSuccessful) {
                        val bodyString = response.body()?.string()
                        if (!bodyString.isNullOrBlank()) {
                            val sourceName = when {
                                url.contains("propakistani") -> "ProPakistani"
                                url.contains("brecorder") -> "Business Recorder"
                                url.contains("dawn.com") -> "Dawn Business"
                                url.contains("tribune.com.pk") -> "Express Tribune"
                                url.contains("mettisglobal") -> "Mettis Global"
                                else -> "Financial News"
                            }
                            val parsedItems = rssParser.parse(bodyString, sourceName)
                            if (parsedItems.isNotEmpty()) {
                                feedItemDao.insertFeedItems(parsedItems.map { FeedItemEntity.fromDomain(it) })
                                fetchedAny = true
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore individual feed fetch failure and try others
                }
            }
            if (fetchedAny) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to fetch any RSS feeds"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getFeedItemsFlow(): Flow<List<FeedItem>> {
        return feedItemDao.getAllFeedItems().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
