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

    fun d(tag: String, message: String, correlationId: String? = null) = log("DEBUG", tag, message, correlationId)
    fun i(tag: String, message: String, correlationId: String? = null) = log("INFO", tag, message, correlationId)
    fun w(tag: String, message: String, correlationId: String? = null) = log("WARN", tag, message, correlationId)
    fun e(tag: String, message: String, throwable: Throwable? = null, correlationId: String? = null) {
        val msg = if (throwable != null) "$message: ${throwable.localizedMessage}" else message
        log("ERROR", tag, msg, correlationId, immediateSync = true)
    }
    fun fatal(tag: String, message: String, throwable: Throwable? = null, correlationId: String? = null) {
        val msg = if (throwable != null) "$message: ${throwable.localizedMessage}" else message
        log("FATAL", tag, msg, correlationId, immediateSync = true)
    }

    fun triggerSync() {
        try {
            TelemetrySyncWorker.enqueueOneTimeSync(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkBatchAndTriggerSync() {
        if (logCounter.incrementAndGet() >= BATCH_SIZE) {
            logCounter.set(0)
            triggerSync()
        }
    }

    private fun log(level: String, tag: String, message: String, correlationId: String? = null, immediateSync: Boolean = false) {
        scope.launch {
            try {
                val formattedMessage = if (correlationId != null) "[$correlationId] $message" else message
                logDao.insert(
                    LogEntity(
                        level = level,
                        tag = tag,
                        message = formattedMessage
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
