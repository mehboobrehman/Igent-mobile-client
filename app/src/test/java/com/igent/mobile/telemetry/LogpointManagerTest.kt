package com.igent.mobile.telemetry

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LogpointManagerTest {

    private val manager = LogpointManager.getInstance()

    @Before
    fun setUp() {
        manager.reset()
    }

    @Test
    fun testDisabledCostAndLazyEvaluation() {
        val logpointId = "test_disabled_lazy"
        var valuesEvaluated = false
        var messageEvaluated = false

        // 1. Logpoint is disabled by default
        logpoint(
            id = logpointId,
            tag = "TestTag",
            valuesSupplier = {
                valuesEvaluated = true
                mapOf("key" to "value")
            },
            messageSupplier = {
                messageEvaluated = true
                "Should not evaluate"
            }
        )

        assertFalse("Disabled logpoint must not evaluate values (near-zero cost / zero allocation)", valuesEvaluated)
        assertFalse("Disabled logpoint must not evaluate message", messageEvaluated)

        val config = manager.getConfig(logpointId)
        assertNotNull(config)
        assertEquals(0L, config?.hitCount?.get())

        // 2. Enable logpoint
        manager.setEnabled(logpointId, true)

        logpoint(
            id = logpointId,
            tag = "TestTag",
            valuesSupplier = {
                valuesEvaluated = true
                mapOf("key" to "value")
            },
            messageSupplier = {
                messageEvaluated = true
                "Should evaluate lazily"
            }
        )

        assertTrue("Enabled logpoint must evaluate values lazily", valuesEvaluated)
        assertTrue("Enabled logpoint must evaluate message lazily", messageEvaluated)
        assertEquals(1L, config?.hitCount?.get())
    }

    @Test
    fun testConditionValidation() {
        val logpointId = "test_condition"
        manager.setEnabled(logpointId, true)
        manager.setCondition(logpointId, LogpointCondition(field = "status", operator = "==", targetValue = "active"))

        var hit1 = false
        logpoint(
            id = logpointId,
            valuesSupplier = { mapOf("status" to "inactive") },
            messageSupplier = {
                hit1 = true
                "Inactive message"
            }
        )
        assertFalse("Condition mismatch should prevent logging", hit1)
        assertEquals(0L, manager.getConfig(logpointId)?.hitCount?.get())

        var hit2 = false
        logpoint(
            id = logpointId,
            valuesSupplier = { mapOf("status" to "active") },
            messageSupplier = {
                hit2 = true
                "Active message"
            }
        )
        assertTrue("Condition match should allow logging", hit2)
        assertEquals(1L, manager.getConfig(logpointId)?.hitCount?.get())
    }

    @Test
    fun testRedactionInLogpoints() {
        val raw = mapOf("username" to "bob", "auth_token" to "secret123", "password" to "mypass")
        val redacted = StateRedactor.redact(raw) as Map<String, Any?>

        assertEquals("bob", redacted["username"])
        assertEquals("[REDACTED]", redacted["auth_token"])
        assertEquals("[REDACTED]", redacted["password"])
    }

    @Test
    fun testRateLimitingPerLogpoint() {
        val logpointId = "test_rate_limit"
        manager.setEnabled(logpointId, true)
        val config = manager.getOrRegister(logpointId, "RateTest")

        // Trigger multiple hits rapidly
        for (i in 0 until 150) {
            logpoint(
                id = logpointId,
                messageSupplier = { "Hit $i" }
            )
        }

        // Max per minute is 120
        assertTrue(config.hitCount.get() <= 120)
        assertTrue(config.dropCount.get() > 0)
    }

    @Test
    fun testReleaseValidator() {
        assertTrue(LogpointReleaseValidator.validateRelease(isRelease = true, activeLogpointsCount = 0))
        assertTrue(LogpointReleaseValidator.validateRelease(isRelease = false, activeLogpointsCount = 5))
    }
}
