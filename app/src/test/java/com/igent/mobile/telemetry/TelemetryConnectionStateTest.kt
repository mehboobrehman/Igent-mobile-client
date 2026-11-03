package com.igent.mobile.telemetry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TelemetryConnectionStateTest {

    @Test
    fun testStatesExist() {
        val states = TelemetryConnectionState.values()
        assertEquals(5, states.size)
        assertTrue(states.contains(TelemetryConnectionState.DISCONNECTED))
        assertTrue(states.contains(TelemetryConnectionState.CONNECTING))
        assertTrue(states.contains(TelemetryConnectionState.CONNECTED))
        assertTrue(states.contains(TelemetryConnectionState.DEGRADED))
        assertTrue(states.contains(TelemetryConnectionState.OFFLINE))
    }
}
