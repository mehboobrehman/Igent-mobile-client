package com.igent.mobile.telemetry

import org.junit.Assert.*
import org.junit.Test

class StateRedactorTest {

    @Test
    fun testRedactSensitiveFields() {
        val state = mapOf(
            "username" to "alice",
            "auth_token" to "secret_token_123",
            "password" to "my_password_abc",
            "user_secret" to "top_secret",
            "email" to "alice@example.com",
            "api_key" to "key_999",
            "nested" to mapOf(
                "session_token" to "tok_xyz",
                "normal_field" to "hello"
            ),
            "list" to listOf(
                mapOf("credential" to "bad"),
                "normal_item"
            )
        )

        val redacted = StateRedactor.redact(state) as Map<*, *>

        assertEquals("alice", redacted["username"])
        assertEquals("[REDACTED]", redacted["auth_token"])
        assertEquals("[REDACTED]", redacted["password"])
        assertEquals("[REDACTED]", redacted["user_secret"])
        assertEquals("[REDACTED]", redacted["email"])
        assertEquals("[REDACTED]", redacted["api_key"])

        @Suppress("UNCHECKED_CAST")
        val nested = redacted["nested"] as Map<String, Any?>
        assertEquals("[REDACTED]", nested["session_token"])
        assertEquals("hello", nested["normal_field"])

        @Suppress("UNCHECKED_CAST")
        val list = redacted["list"] as List<Any?>
        @Suppress("UNCHECKED_CAST")
        val listItemMap = list[0] as Map<String, Any?>
        assertEquals("[REDACTED]", listItemMap["credential"])
        assertEquals("normal_item", list[1])
    }
}
