package com.igent.mobile.telemetry

object StateRedactor {
    private val sensitiveKeyPatterns = listOf(
        "token", "password", "secret", "auth", "key", "credential", "email", "creditcard", "ssn", "pin"
    )

    fun redact(value: Any?): Any? {
        return when (value) {
            null -> null
            is Map<*, *> -> {
                val redactedMap = mutableMapOf<String, Any?>()
                for ((k, v) in value) {
                    val keyStr = k.toString()
                    if (isSensitiveKey(keyStr)) {
                        redactedMap[keyStr] = "[REDACTED]"
                    } else {
                        redactedMap[keyStr] = redact(v)
                    }
                }
                redactedMap
            }
            is List<*> -> {
                value.map { redact(it) }
            }
            is Array<*> -> {
                value.map { redact(it) }
            }
            else -> value
        }
    }

    private fun isSensitiveKey(key: String): Boolean {
        val lowerKey = key.lowercase()
        return sensitiveKeyPatterns.any { pattern -> lowerKey.contains(pattern) }
    }
}
