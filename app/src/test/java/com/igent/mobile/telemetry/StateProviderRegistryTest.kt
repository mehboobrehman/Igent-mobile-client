package com.igent.mobile.telemetry

import org.junit.Assert.*
import org.junit.Test

class StateProviderRegistryTest {

    @Test
    fun testRegisterAndAllowList() {
        val registry = StateProviderRegistry()
        assertFalse(registry.isAllowed("user_profile"))

        registry.registerProvider("user_profile") { mapOf("username" to "john_doe") }

        assertTrue(registry.isAllowed("user_profile"))
        assertEquals(setOf("user_profile"), registry.getRegisteredProviderNames())

        val provider = registry.getProvider("user_profile")
        assertNotNull(provider)
        assertEquals(mapOf("username" to "john_doe"), provider!!())

        registry.unregisterProvider("user_profile")
        assertFalse(registry.isAllowed("user_profile"))
    }
}
