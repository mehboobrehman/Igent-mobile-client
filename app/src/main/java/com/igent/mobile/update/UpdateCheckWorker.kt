package com.igent.mobile.update

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class UpdateCheckWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val updateRepository: UpdateRepository,
    private val updatePreferences: UpdatePreferences
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val autoCheck = updatePreferences.autoCheckEnabled.first()
            if (!autoCheck) return Result.success()

            val state = updateRepository.checkForUpdate()
            if (state is DownloadState.Available) {
                updatePreferences.saveLastCheckTimestamp(System.currentTimeMillis())
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
