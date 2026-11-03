package com.igent.mobile.telemetry

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelemetryRateLimiter @Inject constructor(
    private val defaultLimitPerMinute: Int
) {
    constructor() : this(120)

    private val categoryLimits = ConcurrentHashMap<String, Int>().apply {
        put("logs", 180)
        put("network", 300)
        put("performance", 60)
        put("heartbeats", 30)
        put("lifecycle", 60)
        put("crashes", 1000)
        put("anrs", 1000)
    }

    private data class Bucket(
        val maxPerMinute: Int,
        var count: Int = 0,
        var windowStart: Long = System.currentTimeMillis()
    )

    private val buckets = ConcurrentHashMap<String, Bucket>()
    private val droppedCounters = ConcurrentHashMap<String, AtomicLong>()

    fun shouldAllow(category: String): Boolean {
        val limit = categoryLimits[category] ?: defaultLimitPerMinute
        val now = System.currentTimeMillis()
        val bucket = buckets.compute(category) { _, existing ->
            if (existing == null || now - existing.windowStart > 60000L) {
                Bucket(limit, 1, now)
            } else {
                existing.count++
                existing
            }
        }!!

        if (bucket.count > bucket.maxPerMinute) {
            droppedCounters.computeIfAbsent(category) { AtomicLong(0) }.incrementAndGet()
            return false
        }
        return true
    }

    fun getDroppedCount(category: String): Long {
        return droppedCounters[category]?.get() ?: 0L
    }

    fun getTotalDroppedCount(): Long {
        return droppedCounters.values.sumOf { it.get() }
    }

    fun reset() {
        buckets.clear()
        droppedCounters.clear()
    }
}
