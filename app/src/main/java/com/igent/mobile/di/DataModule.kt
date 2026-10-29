package com.igent.mobile.di

import android.content.Context
import androidx.room.Room
import com.igent.mobile.data.local.IgentDatabase
import com.igent.mobile.data.local.dao.FeedItemDao
import com.igent.mobile.data.local.dao.IdeaCardDao
import com.igent.mobile.data.local.dao.MessageDao
import com.igent.mobile.data.local.dao.StockDao
import com.igent.mobile.data.local.dao.WeeklySummaryDao
import com.igent.mobile.data.preferences.PreferencesDataSource
import com.igent.mobile.data.repository.AiRepositoryImpl
import com.igent.mobile.data.repository.PsxRepositoryImpl
import com.igent.mobile.data.repository.RssRepositoryImpl
import com.igent.mobile.domain.repository.AiRepository
import com.igent.mobile.domain.repository.PsxRepository
import com.igent.mobile.domain.repository.RssRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideIgentDatabase(
        @ApplicationContext context: Context
    ): IgentDatabase {
        return Room.databaseBuilder(
            context,
            IgentDatabase::class.java,
            IgentDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideMessageDao(database: IgentDatabase): MessageDao {
        return database.messageDao()
    }

    @Provides
    @Singleton
    fun provideStockDao(database: IgentDatabase): StockDao {
        return database.stockDao()
    }

    @Provides
    @Singleton
    fun provideFeedItemDao(database: IgentDatabase): FeedItemDao {
        return database.feedItemDao()
    }

    @Provides
    @Singleton
    fun provideIdeaCardDao(database: IgentDatabase): IdeaCardDao {
        return database.ideaCardDao()
    }

    @Provides
    @Singleton
    fun provideWeeklySummaryDao(database: IgentDatabase): WeeklySummaryDao {
        return database.weeklySummaryDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    @Provides
    @Singleton
    fun providePreferencesDataSource(
        @ApplicationContext context: Context
    ): PreferencesDataSource {
        return PreferencesDataSource(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAiRepository(
        aiRepositoryImpl: AiRepositoryImpl
    ): AiRepository

    @Binds
    @Singleton
    abstract fun bindPsxRepository(
        psxRepositoryImpl: PsxRepositoryImpl
    ): PsxRepository

    @Binds
    @Singleton
    abstract fun bindRssRepository(
        rssRepositoryImpl: RssRepositoryImpl
    ): RssRepository
}
