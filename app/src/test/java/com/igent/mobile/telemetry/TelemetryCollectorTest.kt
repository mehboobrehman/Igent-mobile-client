package com.igent.mobile.telemetry

import com.igent.mobile.data.remote.telemetry.TelemetryEnvelopeDto
import com.igent.mobile.data.remote.telemetry.validateTelemetryEnvelope
import org.junit.Assert.*
import org.junit.Test

class TelemetryCollectorTest {

    @Test
    fun testEnvelopeTypesValidation() {
        val logEnvelope = TelemetryEnvelopeDto.createLog("sess_1", "dev_1", 1L, "INFO", "msg", "tag", "corr_1")
        assertTrue(validateTelemetryEnvelope(logEnvelope))

        val crashEnvelope = TelemetryEnvelopeDto.createCrash("sess_1", "dev_1", 2L, "main", "NullPointerException", "msg", "trace")
        assertTrue(validateTelemetryEnvelope(crashEnvelope))

        val anrEnvelope = TelemetryEnvelopeDto.createAnr("sess_1", "dev_1", 3L, "main", "trace", 5000L)
        assertTrue(validateTelemetryEnvelope(anrEnvelope))

        val networkEnvelope = TelemetryEnvelopeDto.createNetwork("sess_1", "dev_1", 4L, "GET", "https://api.test.com", 200, 150L, 100L, 500L)
        assertTrue(validateTelemetryEnvelope(networkEnvelope))

        val perfEnvelope = TelemetryEnvelopeDto.createPerformance("sess_1", "dev_1", 5L, 1000L, 20971520L, 104857600L, 15)
        assertTrue(validateTelemetryEnvelope(perfEnvelope))

        val lifecycleEnvelope = TelemetryEnvelopeDto.createLifecycle("sess_1", "dev_1", 6L, "MainActivity", "resumed")
        assertTrue(validateTelemetryEnvelope(lifecycleEnvelope))

        val commandEnvelope = TelemetryEnvelopeDto.createCommand("sess_1", "dev_1", 7L, "cmd_1", "ping", "")
        assertTrue(validateTelemetryEnvelope(commandEnvelope))
    }
}
