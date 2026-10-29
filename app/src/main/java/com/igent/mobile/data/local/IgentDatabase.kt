package com.igent.mobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.igent.mobile.data.local.dao.FeedItemDao
import com.igent.mobile.data.local.dao.IdeaCardDao
import com.igent.mobile.data.local.dao.MessageDao
import com.igent.mobile.data.local.dao.StockDao
import com.igent.mobile.data.local.dao.WeeklySummaryDao
import com.igent.mobile.data.local.entity.FeedItemEntity
import com.igent.mobile.data.local.entity.IdeaCardEntity
import com.igent.mobile.data.local.entity.MessageEntity
import com.igent.mobile.data.local.entity.StockEntity
import com.igent.mobile.data.local.entity.WeeklySummaryEntity

@Database(
    entities = [
        MessageEntity::class,
        StockEntity::class,
        FeedItemEntity::class,
        IdeaCardEntity::class,
        WeeklySummaryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class IgentDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun stockDao(): StockDao
    abstract fun feedItemDao(): FeedItemDao
    abstract fun ideaCardDao(): IdeaCardDao
    abstract fun weeklySummaryDao(): WeeklySummaryDao

    companion object {
        const val DATABASE_NAME = "igent_database"
    }
}
