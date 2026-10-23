package com.igent.mobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.igent.mobile.data.local.dao.FeedItemDao
import com.igent.mobile.data.local.dao.MessageDao
import com.igent.mobile.data.local.dao.StockDao
import com.igent.mobile.data.local.entity.FeedItemEntity
import com.igent.mobile.data.local.entity.MessageEntity
import com.igent.mobile.data.local.entity.StockEntity

@Database(
    entities = [
        MessageEntity::class,
        StockEntity::class,
        FeedItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class IgentDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun stockDao(): StockDao
    abstract fun feedItemDao(): FeedItemDao

    companion object {
        const val DATABASE_NAME = "igent_database"
    }
}
