package com.igent.mobile.telemetry

import kotlin.math.min
import kotlin.random.Random

class TelemetryBackoff(
    private val initialDelayMs: Long = 1000L,
    private val maxDelayMs: Long = 30000L,
    private val multiplier: Double = 2.0,
    private val jitterFactor: Double = 0.2
) {
    private var attempt: Int = 0

    fun nextDelayMs(): Long {
        val exponentialDelay = (initialDelayMs * Math.pow(multiplier, attempt.toDouble())).toLong()
        val cappedDelay = min(exponentialDelay, maxDelayMs)
        val jitterRange = (cappedDelay * jitterFactor).toLong()
        val jitter = if (jitterRange > 0) Random.nextLong(-jitterRange, jitterRange + 1) else 0L
        attempt++
        return maxOf(100L, cappedDelay + jitter)
    }

    fun reset() {
        attempt = 0
    }

    fun getAttemptCount(): Int = attempt
}
