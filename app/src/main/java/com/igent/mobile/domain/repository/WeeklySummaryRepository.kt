package com.igent.mobile.domain.repository

import com.igent.mobile.data.local.entity.WeeklySummaryEntity
import kotlinx.coroutines.flow.Flow

interface WeeklySummaryRepository {
    fun getAllWeeklySummaries(): Flow<List<WeeklySummaryEntity>>
    suspend fun getWeeklySummary(weekId: String): WeeklySummaryEntity?
    suspend fun generateWeeklySummary(): WeeklySummaryEntity
}
