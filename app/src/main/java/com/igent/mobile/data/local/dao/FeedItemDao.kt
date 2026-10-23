package com.igent.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.igent.mobile.data.local.entity.FeedItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedItemDao {
    @Query("SELECT * FROM feed_items ORDER BY pubDate DESC")
    fun getAllFeedItems(): Flow<List<FeedItemEntity>>

    @Query("SELECT * FROM feed_items ORDER BY pubDate DESC")
    suspend fun getAllFeedItemsOnce(): List<FeedItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedItems(items: List<FeedItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedItem(item: FeedItemEntity)

    @Query("DELETE FROM feed_items")
    suspend fun clearFeedItems()
}
