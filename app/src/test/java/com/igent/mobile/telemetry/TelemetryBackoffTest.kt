package com.igent.mobile.telemetry

import org.junit.Assert.assertTrue
import org.junit.Test

class TelemetryBackoffTest {

    @Test
    fun testExponentialBackoffIncreases() {
        val backoff = TelemetryBackoff(initialDelayMs = 1000L, maxDelayMs = 30000L, multiplier = 2.0, jitterFactor = 0.0)
        
        val delay1 = backoff.nextDelayMs()
        val delay2 = backoff.nextDelayMs()
        val delay3 = backoff.nextDelayMs()

        assertTrue(delay1 == 1000L)
        assertTrue(delay2 == 2000L)
        assertTrue(delay3 == 4000L)
    }

    @Test
    fun testBackoffReset() {
        val backoff = TelemetryBackoff(initialDelayMs = 1000L, maxDelayMs = 30000L, multiplier = 2.0, jitterFactor = 0.0)
        backoff.nextDelayMs()
        backoff.nextDelayMs()
        backoff.reset()
        
        val delayAfterReset = backoff.nextDelayMs()
        assertTrue(delayAfterReset == 1000L)
    }
}
