package com.igent.mobile.telemetry

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TelemetryRateLimiterTest {

    private lateinit var rateLimiter: TelemetryRateLimiter

    @Before
    fun setUp() {
        rateLimiter = TelemetryRateLimiter(2)
    }

    @Test
    fun testRateLimitEnforced() {
        val category = "test_cat"
        assertTrue(rateLimiter.shouldAllow(category))
        assertTrue(rateLimiter.shouldAllow(category))
        assertFalse(rateLimiter.shouldAllow(category))
        assertTrue(rateLimiter.getDroppedCount(category) == 1L)
    }
}
