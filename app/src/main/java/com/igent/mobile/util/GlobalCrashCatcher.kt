package com.igent.mobile.util

import android.content.Context
import com.igent.mobile.data.local.dao.LogDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlin.system.exitProcess

class GlobalCrashCatcher private constructor(
    private val context: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface CrashCatcherEntryPoint {
        fun logDao(): LogDao
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            val entryPoint = EntryPointAccessors.fromApplication(context, CrashCatcherEntryPoint::class.java)
            val logDao = entryPoint.logDao()
            
            val stackTrace = throwable.stackTraceToString()
            val message = "FATAL CRASH on thread ${thread.name}: ${throwable.localizedMessage}\n$stackTrace"
            
            // Synchronously insert into Room DB before termination
            kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    logDao.insert(
                        com.igent.mobile.data.local.entity.LogEntity(
                            level = "ERROR",
                            tag = "CrashCatcher",
                            message = message.take(4000) // limit length if needed
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            defaultHandler?.uncaughtException(thread, throwable) ?: exitProcess(10)
        }
    }

    companion object {
        fun install(context: Context) {
            val currentHandler = Thread.getDefaultUncaughtExceptionHandler()
            if (currentHandler !is GlobalCrashCatcher) {
                Thread.setDefaultUncaughtExceptionHandler(GlobalCrashCatcher(context.applicationContext, currentHandler))
            }
        }
    }
}
