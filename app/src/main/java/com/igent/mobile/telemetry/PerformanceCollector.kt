package com.igent.mobile.telemetry

import android.content.Context
import com.igent.mobile.util.AgentLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerformanceCollector @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logger: AgentLogger
) {
    private val appStartTime = System.currentTimeMillis()

    fun getStartupTimeMs(): Long {
        return System.currentTimeMillis() - appStartTime
    }

    fun getMemoryMetrics(): MemoryMetrics {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory
        val maxMemory = runtime.maxMemory()
        return MemoryMetrics(usedMemory, maxMemory)
    }

    fun getThreadCount(): Int {
        return Thread.activeCount()
    }

    fun logPerformanceSnapshot() {
        val mem = getMemoryMetrics()
        val threads = getThreadCount()
        val startup = getStartupTimeMs()
        logger.i("PerformanceCollector", "Performance Snapshot -> Startup: ${startup}ms | UsedMem: ${mem.usedBytes / 1024 / 1024}MB | MaxMem: ${mem.maxBytes / 1024 / 1024}MB | Threads: $threads")
    }

    data class MemoryMetrics(
        val usedBytes: Long,
        val maxBytes: Long
    )
}
