package com.igent.mobile.domain.repository

import com.igent.mobile.data.local.entity.IdeaCardEntity
import kotlinx.coroutines.flow.Flow

interface IdeaCardRepository {
    fun getAllIdeaCards(): Flow<List<IdeaCardEntity>>
    suspend fun getIdeaCardForDate(dateString: String): IdeaCardEntity?
    suspend fun generateDailyIdeaCards(): List<IdeaCardEntity>
    suspend fun initializeDefaultIdeaCards()
}
