package com.igent.mobile.telemetry

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.igent.mobile.util.AgentLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnrDetector @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logger: AgentLogger
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val lastHeartbeat = AtomicLong(System.currentTimeMillis())
    private var isRunning = false
    private val checkIntervalMs = 2000L
    private val anrThresholdMs = 4000L

    private val heartbeatRunnable = object : Runnable {
        override fun run() {
            lastHeartbeat.set(System.currentTimeMillis())
        }
    }

    private val watchdogThread = Thread {
        try {
            while (isRunning) {
                mainHandler.post(heartbeatRunnable)
                Thread.sleep(checkIntervalMs)

                val delta = System.currentTimeMillis() - lastHeartbeat.get()
                if (delta > anrThresholdMs) {
                    val mainThread = Looper.getMainLooper().thread
                    val stackTrace = mainThread.stackTrace.joinToString("\n") { "  at $it" }
                    val message = "ANR detected! Main thread hung for ${delta}ms.\nStack trace:\n$stackTrace"
                    Log.e("AnrDetector", message)
                    logger.e("AnrDetector", message)
                }
            }
        } catch (e: InterruptedException) {
            // Thread interrupted, exit gracefully
        }
    }.apply {
        name = "Igent-AnrDetector-Thread"
        isDaemon = true
    }

    fun start() {
        if (isRunning) return
        isRunning = true
        try {
            watchdogThread.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        isRunning = false
        watchdogThread.interrupt()
    }
}
