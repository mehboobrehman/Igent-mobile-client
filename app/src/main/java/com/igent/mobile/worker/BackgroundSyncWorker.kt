package com.igent.mobile.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.igent.mobile.domain.repository.PsxRepository
import com.igent.mobile.domain.repository.RssRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import android.util.Log

@HiltWorker
class BackgroundSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val rssRepository: RssRepository,
    private val psxRepository: PsxRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting periodic background sync for RSS and PSX data")
        
        var success = true

        // 1. Refresh RSS feeds
        try {
            val rssResult = rssRepository.refreshFeed()
            if (rssResult.isFailure) {
                Log.w(TAG, "Failed to refresh RSS feeds: ${rssResult.exceptionOrNull()?.message}")
                success = false
            } else {
                Log.d(TAG, "Successfully refreshed RSS feeds")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during RSS refresh", e)
            success = false
        }

        // 2. Refresh PSX quotes
        try {
            val psxResult = psxRepository.getStocks()
            if (psxResult.isFailure) {
                Log.w(TAG, "Failed to fetch PSX stocks: ${psxResult.exceptionOrNull()?.message}")
                success = false
            } else {
                Log.d(TAG, "Successfully fetched and cached PSX stocks: ${psxResult.getOrNull()?.size ?: 0} items")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during PSX fetch", e)
            success = false
        }

        return if (success) {
            Result.success()
        } else {
            // If transient network issues occur, WorkManager can retry if requested,
            // or we can return Result.retry() or Result.success() depending on whether we want retry.
            // Returning retry for transient network failure is standard.
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "BackgroundSyncWorker"
        const val WORK_NAME = "com.igent.mobile.worker.BackgroundSyncWorker"
    }
}
