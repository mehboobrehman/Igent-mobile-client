package com.igent.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.igent.mobile.data.local.entity.IdeaCardEntity
import com.igent.mobile.data.local.entity.WeeklySummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IdeaCardDao {
    @Query("SELECT * FROM idea_cards ORDER BY timestamp DESC")
    fun getAllIdeaCards(): Flow<List<IdeaCardEntity>>

    @Query("SELECT * FROM idea_cards ORDER BY timestamp DESC")
    suspend fun getAllIdeaCardsOnce(): List<IdeaCardEntity>

    @Query("SELECT * FROM idea_cards WHERE dateString = :dateString LIMIT 1")
    suspend fun getIdeaCardForDate(dateString: String): IdeaCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdeaCard(card: IdeaCardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdeaCards(cards: List<IdeaCardEntity>)

    @Query("DELETE FROM idea_cards")
    suspend fun clearIdeaCards()
}

@Dao
interface WeeklySummaryDao {
    @Query("SELECT * FROM weekly_summaries ORDER BY timestamp DESC")
    fun getAllWeeklySummaries(): Flow<List<WeeklySummaryEntity>>

    @Query("SELECT * FROM weekly_summaries ORDER BY timestamp DESC")
    suspend fun getAllWeeklySummariesOnce(): List<WeeklySummaryEntity>

    @Query("SELECT * FROM weekly_summaries WHERE weekId = :weekId LIMIT 1")
    suspend fun getWeeklySummary(weekId: String): WeeklySummaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeeklySummary(summary: WeeklySummaryEntity)

    @Query("DELETE FROM weekly_summaries")
    suspend fun clearWeeklySummaries()
}
