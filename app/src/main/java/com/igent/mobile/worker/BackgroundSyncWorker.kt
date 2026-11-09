package com.igent.mobile.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.igent.mobile.data.local.IgentDatabase
import com.igent.mobile.data.local.entity.SensorRunEntity
import com.igent.mobile.data.ingestion.parser.DataParsers
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@HiltWorker
class BackgroundSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val database: IgentDatabase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting periodic background sync (Phase 3 D1-D6)")
        var success = true
        val sensorDao = database.sensorRunDao()

        // 1. Sync SBP Rates
        try {
            val sourceId = "sbp"
            val lastRun = sensorDao.getLatestRunForSource(sourceId)
            val url = URL("https://www.sbp.org.pk")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 15000
                lastRun?.etag?.let { setRequestProperty("If-None-Match", it) }
                lastRun?.lastModified?.let { setRequestProperty("If-Modified-Since", it) }
            }
            val responseCode = connection.responseCode
            val etag = connection.getHeaderField("ETag")
            val lastModified = connection.getHeaderField("Last-Modified")

            if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
                sensorDao.insertRun(
                    SensorRunEntity(
                        sourceId = sourceId,
                        timestamp = System.currentTimeMillis(),
                        status = "NOT_MODIFIED",
                        etag = etag ?: lastRun?.etag,
                        lastModified = lastModified ?: lastRun?.lastModified
                    )
                )
            } else if (responseCode == HttpURLConnection.HTTP_OK) {
                val html = connection.inputStream.bufferedReader().use { it.readText() }
                val parsedRates = DataParsers.parseSbp(html)
                sensorDao.insertRun(
                    SensorRunEntity(
                        sourceId = sourceId,
                        timestamp = System.currentTimeMillis(),
                        status = "SUCCESS",
                        etag = etag,
                        lastModified = lastModified,
                        recordsCount = parsedRates.size
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing SBP", e)
            success = false
            sensorDao.insertRun(
                SensorRunEntity(
                    sourceId = "sbp",
                    timestamp = System.currentTimeMillis(),
                    status = "FAILED",
                    errorMessage = e.message
                )
            )
        }

        // 2. Sync PSX Indices
        try {
            val sourceId = "psx-indices"
            val lastRun = sensorDao.getLatestRunForSource(sourceId)
            val url = URL("https://dps.psx.com.pk/indices/KSE100")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 15000
                lastRun?.etag?.let { setRequestProperty("If-None-Match", it) }
            }
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val html = connection.inputStream.bufferedReader().use { it.readText() }
                val indices = DataParsers.parsePsxIndices(html)
                sensorDao.insertRun(
                    SensorRunEntity(
                        sourceId = sourceId,
                        timestamp = System.currentTimeMillis(),
                        status = "SUCCESS",
                        recordsCount = indices.size
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing PSX indices", e)
        }

        // 3. Prune old sensor runs (> 30 days)
        try {
            val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
            sensorDao.pruneOldRuns(thirtyDaysAgo)
        } catch (e: Exception) {
            Log.e(TAG, "Error pruning old runs", e)
        }

        if (success) Result.success() else Result.retry()
    }

    companion object {
        private const val TAG = "BackgroundSyncWorker"
        const val WORK_NAME = "com.igent.mobile.worker.BackgroundSyncWorker"
    }
}
