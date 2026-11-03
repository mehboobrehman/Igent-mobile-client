package com.igent.mobile.telemetry

import org.junit.Assert.*
import org.junit.Test

class TelemetryCommandHandlerTest {

    @Test
    fun testCommandHandlerStateIntegration() {
        val registry = StateProviderRegistry()
        registry.registerProvider("auth") { mapOf("user" to "test", "token" to "secret") }
        val stateInspector = StateInspector(registry)

        assertTrue(registry.isAllowed("auth"))
        assertFalse(registry.isAllowed("unknown"))

        val providers = stateInspector.listProviders()
        assertTrue(providers.contains("auth"))

        val result = stateInspector.inspectState("auth")
        assertTrue(result is StateInspector.InspectionResult.Success)
    }
}
