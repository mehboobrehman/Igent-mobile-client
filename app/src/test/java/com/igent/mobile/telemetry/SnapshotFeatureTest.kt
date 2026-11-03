package com.igent.mobile.telemetry

import org.junit.Assert.*
import org.junit.Test

class SnapshotFeatureTest {

    @Test
    fun testPreTriggerRingBufferCapacityAndWindow() {
        val buffer = PreTriggerRingBuffer(3)
        buffer.add("logs", mapOf("msg" to "one"))
        buffer.add("logs", mapOf("msg" to "two"))
        buffer.add("network", mapOf("url" to "api/v1"))
        buffer.add("lifecycle", mapOf("state" to "resumed"))

        val window = buffer.getWindow(10)
        assertEquals(3, window.size) // Capacity is 3, so oldest ("one") dropped
        assertEquals("two", window[0].data["msg"])
        assertEquals("api/v1", window[1].data["url"])
        assertEquals("resumed", window[2].data["state"])
    }

    @Test
    fun testRuleConfigurationAndDefaults() {
        val rule = SnapshotRule(
            id = "rule_err",
            triggerCategory = "errors",
            condition = "NullPointerException",
            enabled = true
        )
        assertTrue(rule.enabled)
        assertEquals("errors", rule.triggerCategory)
        assertEquals("NullPointerException", rule.condition)
        assertEquals(60000L, rule.cooldownMs)
        assertEquals(5, rule.maxPerSession)
    }

    @Test
    fun testCaptureSetConfigDefaults() {
        val config = SnapshotCaptureSetConfig()
        assertTrue(config.includeLogWindow)
        assertTrue(config.includeThreadDump)
        assertFalse(config.includeScreenshot) // Heavy items disabled by default
        assertFalse(config.includeViewHierarchy)
        assertFalse(config.includeHeapDump)
    }

    @Test
    fun testStateRedactionInSnapshots() {
        val sensitiveData = mapOf(
            "username" to "bob",
            "auth_token" to "secret_token_xyz",
            "password" to "secret_pass"
        )
        val redacted = StateRedactor.redact(sensitiveData) as Map<*, *>
        assertEquals("bob", redacted["username"])
        assertEquals("[REDACTED]", redacted["auth_token"])
        assertEquals("[REDACTED]", redacted["password"])
    }
}
