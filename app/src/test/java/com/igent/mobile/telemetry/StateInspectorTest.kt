package com.igent.mobile.telemetry

import org.junit.Assert.*
import org.junit.Test

class StateInspectorTest {

    @Test
    fun testInspectUnallowedProvider() {
        val registry = StateProviderRegistry()
        val inspector = StateInspector(registry)

        val result = inspector.inspectState("unknown_provider")
        assertTrue(result is StateInspector.InspectionResult.Error)
        assertTrue((result as StateInspector.InspectionResult.Error).message.contains("not allow-listed"))
    }

    @Test
    fun testInspectAllowedProviderWithRedactionAndTreeView() {
        val registry = StateProviderRegistry()
        registry.registerProvider("session") {
            mapOf(
                "user" to "bob",
                "auth_token" to "secret123",
                "settings" to mapOf(
                    "theme" to "dark",
                    "api_secret" to "xyz"
                ),
                "tags" to listOf("a", "b")
            )
        }

        val inspector = StateInspector(registry)
        val result = inspector.inspectState("session")

        assertTrue(result is StateInspector.InspectionResult.Success)
        val success = result as StateInspector.InspectionResult.Success

        // Verify redaction
        assertEquals("bob", success.data["user"])
        assertEquals("[REDACTED]", success.data["auth_token"])
        @Suppress("UNCHECKED_CAST")
        val settings = success.data["settings"] as Map<String, Any?>
        assertEquals("dark", settings["theme"])
        assertEquals("[REDACTED]", settings["api_secret"])

        // Verify tree view
        assertFalse(success.treeView.isEmpty())
        val userNode = success.treeView.find { it.key == "user" }
        assertNotNull(userNode)
        assertEquals("bob", userNode!!.value)

        val settingsNode = success.treeView.find { it.key == "settings" }
        assertNotNull(settingsNode)
        assertTrue(settingsNode!!.isContainer)

        val apiSecretNode = success.treeView.find { it.path == "settings.api_secret" }
        assertNotNull(apiSecretNode)
        assertEquals("[REDACTED]", apiSecretNode!!.value)
    }

    @Test
    fun testSizeCapping() {
        val registry = StateProviderRegistry()
        registry.registerProvider("large_state") {
            // Generate a huge map exceeding 64KB
            val hugeString = "x".repeat(70 * 1024)
            mapOf("data" to hugeString)
        }

        val inspector = StateInspector(registry)
        val result = inspector.inspectState("large_state")

        assertTrue(result is StateInspector.InspectionResult.Error)
        assertTrue((result as StateInspector.InspectionResult.Error).message.contains("exceeds maximum allowed limit"))
    }
}
