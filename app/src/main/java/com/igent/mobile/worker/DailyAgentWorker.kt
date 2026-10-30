package com.igent.mobile.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.igent.mobile.domain.repository.IdeaCardRepository
import com.igent.mobile.domain.repository.WeeklySummaryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DailyAgentWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val ideaCardRepository: IdeaCardRepository,
    private val weeklySummaryRepository: WeeklySummaryRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Automatically generate daily idea cards and weekly summary
            ideaCardRepository.generateDailyIdeaCards()
            weeklySummaryRepository.generateWeeklySummary()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
