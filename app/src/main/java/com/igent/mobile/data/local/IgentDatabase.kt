package com.igent.mobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.igent.mobile.data.local.dao.*
import com.igent.mobile.data.local.entity.*

@Database(
    entities = [
        MessageEntity::class,
        StockEntity::class,
        FeedItemEntity::class,
        IdeaCardEntity::class,
        WeeklySummaryEntity::class,
        CourseEntity::class,
        CourseModuleEntity::class,
        QuizEntity::class,
        QuizScoreEntity::class,
        LogEntity::class,
        SnapshotEntity::class,
        SensorRunEntity::class,
        PortfolioTransactionEntity::class,
        WatchlistEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class IgentDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun stockDao(): StockDao
    abstract fun feedItemDao(): FeedItemDao
    abstract fun ideaCardDao(): IdeaCardDao
    abstract fun weeklySummaryDao(): WeeklySummaryDao
    abstract fun courseDao(): CourseDao
    abstract fun logDao(): LogDao
    abstract fun snapshotDao(): SnapshotDao
    abstract fun sensorRunDao(): SensorRunDao
    abstract fun portfolioDao(): PortfolioDao
    abstract fun watchlistDao(): WatchlistDao

    companion object {
        const val DATABASE_NAME = "igent_database"
    }
}
