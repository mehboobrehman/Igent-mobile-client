package com.igent.mobile.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.igent.mobile.data.local.dao.LogDao
import com.igent.mobile.data.remote.telemetry.TelemetryApiService
import com.igent.mobile.data.remote.telemetry.TelemetryLogDto
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class TelemetrySyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val logDao: LogDao,
    private val telemetryApiService: TelemetryApiService
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting telemetry sync")

        return try {
            val unsyncedLogs = logDao.getUnsyncedLogs()
            if (unsyncedLogs.isEmpty()) {
                Log.d(TAG, "No logs to sync")
                return Result.success()
            }

            val syncedIds = mutableListOf<Long>()
            for (log in unsyncedLogs) {
                val dto = TelemetryLogDto(
                    timestamp = log.timestamp,
                    level = log.level,
                    message = log.message,
                    tag = log.tag
                )
                
                val response = telemetryApiService.postLog(dto)
                if (response.isSuccessful) {
                    syncedIds.add(log.id)
                } else {
                    Log.w(TAG, "Failed to sync log ${log.id}: ${response.errorBody()?.string()}")
                    // Optionally break or continue; for now, continue to try others
                }
            }

            if (syncedIds.isNotEmpty()) {
                logDao.markAsSynced(syncedIds)
                Log.d(TAG, "Successfully synced ${syncedIds.size} logs")
            }

            if (syncedIds.size == unsyncedLogs.size) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during telemetry sync", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "TelemetrySyncWorker"
        const val WORK_NAME = "com.igent.mobile.worker.TelemetrySyncWorker"
    }
}
