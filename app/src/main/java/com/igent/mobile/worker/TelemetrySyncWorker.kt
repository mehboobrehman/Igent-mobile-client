package com.igent.mobile.worker

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.igent.mobile.data.local.dao.LogDao
import com.igent.mobile.data.remote.telemetry.TelemetryApiService
import com.igent.mobile.data.remote.telemetry.TelemetryEnvelopeDto
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

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

            Log.d(TAG, "Found ${unsyncedLogs.size} unsynced logs")

            val dtos = unsyncedLogs.map { log ->
                TelemetryEnvelopeDto.createLog(
                    sessionId = SESSION_ID,
                    deviceId = DEVICE_ID,
                    seq = seqCounter.incrementAndGet(),
                    level = log.level,
                    message = log.message,
                    tag = log.tag
                )
            }

            // Try batch upload first
            var batchSuccess = false
            try {
                val batchResponse = telemetryApiService.postLogBatch(dtos)
                if (batchResponse.isSuccessful) {
                    batchSuccess = true
                } else {
                    Log.w(TAG, "Batch log sync failed: ${batchResponse.code()} ${batchResponse.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Batch log sync endpoint unavailable or failed, falling back to individual sync: ${e.message}")
            }

            val syncedIds = mutableListOf<Long>()

            if (batchSuccess) {
                syncedIds.addAll(unsyncedLogs.map { it.id })
            } else {
                // Fallback to individual log sync
                for (log in unsyncedLogs) {
                    val dto = TelemetryEnvelopeDto.createLog(
                        sessionId = SESSION_ID,
                        deviceId = DEVICE_ID,
                        seq = seqCounter.incrementAndGet(),
                        level = log.level,
                        message = log.message,
                        tag = log.tag
                    )
                    
                    val response = telemetryApiService.postLog(dto)
                    if (response.isSuccessful) {
                        syncedIds.add(log.id)
                    } else {
                        Log.w(TAG, "Failed to sync log ${log.id}: ${response.errorBody()?.string()}")
                    }
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
        const val ONE_TIME_WORK_NAME = "com.igent.mobile.worker.TelemetrySyncWorker.OneTime"

        private val SESSION_ID = "session_" + UUID.randomUUID().toString()
        private val DEVICE_ID = "android_${Build.MODEL.replace(" ", "_")}_${UUID.randomUUID().toString().take(8)}"
        private val seqCounter = AtomicLong(0)

        fun enqueueOneTimeSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncWorkRequest = OneTimeWorkRequestBuilder<TelemetrySyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                syncWorkRequest
            )
        }
    }
}
