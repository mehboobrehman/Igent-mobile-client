package com.igent.mobile.util

import android.content.Context
import com.igent.mobile.data.local.dao.LogDao
import com.igent.mobile.data.local.entity.LogEntity
import com.igent.mobile.worker.TelemetrySyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentLogger @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logDao: LogDao
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val logCounter = AtomicInteger(0)

    companion object {
        const val BATCH_SIZE = 20
    }

    fun d(tag: String, message: String) = log("DEBUG", tag, message)
    fun i(tag: String, message: String) = log("INFO", tag, message)
    fun w(tag: String, message: String) = log("WARN", tag, message)
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        val msg = if (throwable != null) "$message: ${throwable.localizedMessage}" else message
        log("ERROR", tag, msg, immediateSync = true)
    }

    fun triggerSync() {
        try {
            TelemetrySyncWorker.enqueueOneTimeSync(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun logSync(level: String, tag: String, message: String) {
        runBlocking(Dispatchers.IO) {
            try {
                logDao.insert(
                    LogEntity(
                        level = level,
                        tag = tag,
                        message = message
                    )
                )
                if (level == "ERROR" || level == "FATAL") {
                    triggerSync()
                } else {
                    checkBatchAndTriggerSync()
                }
            } catch (e: Exception) {
                // Fallback if DB is unavailable during crash
                e.printStackTrace()
            }
        }
    }

    private fun checkBatchAndTriggerSync() {
        if (logCounter.incrementAndGet() >= BATCH_SIZE) {
            logCounter.set(0)
            triggerSync()
        }
    }

    private fun log(level: String, tag: String, message: String, immediateSync: Boolean = false) {
        scope.launch {
            try {
                logDao.insert(
                    LogEntity(
                        level = level,
                        tag = tag,
                        message = message
                    )
                )
                if (immediateSync || level == "ERROR" || level == "FATAL") {
                    triggerSync()
                } else {
                    checkBatchAndTriggerSync()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
