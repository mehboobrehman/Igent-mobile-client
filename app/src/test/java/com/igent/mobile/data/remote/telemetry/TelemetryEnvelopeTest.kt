package com.igent.mobile.data.remote.telemetry

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TelemetryEnvelopeTest {

    @Test
    fun testEnvelopeValidation() {
        val validDto = TelemetryEnvelopeDto.createLog(
            sessionId = "session_1",
            deviceId = "device_1",
            seq = 1L,
            level = "INFO",
            message = "Test message",
            tag = "TestTag"
        )
        assertTrue(validateTelemetryEnvelope(validDto))

        val invalidVersion = validDto.copy(schemaVersion = "2.0.0")
        assertFalse(validateTelemetryEnvelope(invalidVersion))

        val invalidType = validDto.copy(type = "invalid_type")
        assertFalse(validateTelemetryEnvelope(invalidType))
    }

    @Test
    fun testSerialization() {
        val dto = TelemetryEnvelopeDto.createLog(
            sessionId = "session_1",
            deviceId = "device_1",
            seq = 1L,
            level = "INFO",
            message = "Test message",
            tag = "TestTag"
        )
        val jsonString = Json.encodeToString(TelemetryEnvelopeDto.serializer(), dto)
        val decoded = Json.decodeFromString(TelemetryEnvelopeDto.serializer(), jsonString)
        assertEquals(dto.sessionId, decoded.sessionId)
        assertEquals(dto.type, decoded.type)
        assertEquals(dto.seq, decoded.seq)
    }
}
