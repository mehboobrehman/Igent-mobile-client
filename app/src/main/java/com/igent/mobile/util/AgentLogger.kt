package com.igent.mobile.util

import android.content.Context
import com.igent.mobile.data.local.dao.LogDao
import com.igent.mobile.data.local.entity.LogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.system.exitProcess

@Singleton
class AgentLogger @Inject constructor(
    private val logDao: LogDao
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun d(tag: String, message: String) = log("DEBUG", tag, message)
    fun i(tag: String, message: String) = log("INFO", tag, message)
    fun w(tag: String, message: String) = log("WARN", tag, message)
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        val msg = if (throwable != null) "$message: ${throwable.localizedMessage}" else message
        log("ERROR", tag, msg)
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
            } catch (e: Exception) {
                // Fallback if DB is unavailable during crash
                e.printStackTrace()
            }
        }
    }

    private fun log(level: String, tag: String, message: String) {
        scope.launch {
            try {
                logDao.insert(
                    LogEntity(
                        level = level,
                        tag = tag,
                        message = message
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
