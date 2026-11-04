package com.igent.mobile.telemetry

import com.igent.mobile.util.AgentLogger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

data class LogpointCondition(
    val field: String, // e.g. "tag", "message", or any key in values
    val operator: String, // "==", "!=", "<", ">", "<=", ">=", "contains", "startsWith", "endsWith"
    val targetValue: String
) {
    fun evaluate(values: Map<String, Any?>, tag: String, message: String): Boolean {
        val actual = when (field) {
            "tag" -> tag
            "message" -> message
            else -> values[field]
        } ?: return false

        val actualStr = actual.toString()
        return when (operator) {
            "==" -> actualStr == targetValue
            "!=" -> actualStr != targetValue
            "<" -> (actualStr.toDoubleOrNull() ?: 0.0) < (targetValue.toDoubleOrNull() ?: 0.0)
            ">" -> (actualStr.toDoubleOrNull() ?: 0.0) > (targetValue.toDoubleOrNull() ?: 0.0)
            "<=" -> (actualStr.toDoubleOrNull() ?: 0.0) <= (targetValue.toDoubleOrNull() ?: 0.0)
            ">=" -> (actualStr.toDoubleOrNull() ?: 0.0) >= (targetValue.toDoubleOrNull() ?: 0.0)
            "contains" -> actualStr.contains(targetValue, ignoreCase = true)
            "startsWith" -> actualStr.startsWith(targetValue, ignoreCase = true)
            "endsWith" -> actualStr.endsWith(targetValue, ignoreCase = true)
            else -> true
        }
    }
}

class LogpointConfig(
    val id: String,
    val tag: String,
    val defaultLevel: String = "INFO",
    val callSite: String = ""
) {
    val enabled = AtomicBoolean(false)
    val level = AtomicReference(defaultLevel)
    val samplingRate = AtomicReference(1.0) // 0.0 to 1.0
    val condition = AtomicReference<LogpointCondition?>(null)
    
    val hitCount = AtomicLong(0)
    val dropCount = AtomicLong(0)
    val lastHitTime = AtomicLong(0)
    
    private val rateWindowStart = AtomicLong(System.currentTimeMillis())
    private val rateWindowCount = AtomicLong(0)
    private val maxPerMinute = 120

    fun shouldSample(): Boolean {
        val rate = samplingRate.get()
        if (rate >= 1.0) return true
        if (rate <= 0.0) return false
        return Random.nextDouble() <= rate
    }

    fun checkRateLimit(): Boolean {
        val now = System.currentTimeMillis()
        val start = rateWindowStart.get()
        if (now - start > 60000L) {
            synchronized(this) {
                if (now - rateWindowStart.get() > 60000L) {
                    rateWindowStart.set(now)
                    rateWindowCount.set(0)
                }
            }
        }
        synchronized(this) {
            val currentCount = rateWindowCount.get()
            if (currentCount >= maxPerMinute) {
                dropCount.incrementAndGet()
                return false
            }
            rateWindowCount.incrementAndGet()
            return true
        }
    }
}

@Singleton
class LogpointManager @Inject constructor(
    private val logger: AgentLogger?,
    private val rateLimiter: TelemetryRateLimiter?
) {
    private val logpoints = ConcurrentHashMap<String, LogpointConfig>()

    companion object {
        @Volatile
        private var instance: LogpointManager? = null

        fun getInstance(): LogpointManager {
            return instance ?: synchronized(this) {
                instance ?: LogpointManager(null, null).also { instance = it }
            }
        }
    }

    init {
        instance = this
    }

    fun getOrRegister(id: String, tag: String, defaultLevel: String = "INFO", callSite: String = ""): LogpointConfig {
        return logpoints.computeIfAbsent(id) {
            LogpointConfig(id, tag, defaultLevel, callSite)
        }
    }

    fun getConfig(id: String): LogpointConfig? = logpoints[id]

    fun getAllConfigs(): List<LogpointConfig> = logpoints.values.toList()

    fun setEnabled(id: String, enabled: Boolean) {
        val config = logpoints.computeIfAbsent(id) { LogpointConfig(id, "Logpoint", "INFO", "") }
        config.enabled.set(enabled)
    }

    fun setLevel(id: String, level: String) {
        val config = logpoints.computeIfAbsent(id) { LogpointConfig(id, "Logpoint", "INFO", "") }
        config.level.set(level)
    }

    fun setSamplingRate(id: String, rate: Double) {
        val config = logpoints.computeIfAbsent(id) { LogpointConfig(id, "Logpoint", "INFO", "") }
        config.samplingRate.set(rate.coerceIn(0.0, 1.0))
    }

    fun setCondition(id: String, condition: LogpointCondition?) {
        val config = logpoints.computeIfAbsent(id) { LogpointConfig(id, "Logpoint", "INFO", "") }
        config.condition.set(condition)
    }

    fun reset() {
        logpoints.clear()
    }

    fun recordHit(config: LogpointConfig, message: String, values: Map<String, Any?>) {
        if (!config.checkRateLimit()) return
        
        config.hitCount.incrementAndGet()
        config.lastHitTime.set(System.currentTimeMillis())

        val cappedMessage = if (message.length > 2000) message.take(2000) + "...[truncated]" else message
        val currentLevel = config.level.get()
        val valuesSummary = if (values.isNotEmpty()) " | Values: $values" else ""

        if (logger != null) {
            when (currentLevel.uppercase()) {
                "DEBUG" -> logger.d(config.tag, "[Logpoint:${config.id}] $cappedMessage$valuesSummary")
                "WARN" -> logger.w(config.tag, "[Logpoint:${config.id}] $cappedMessage$valuesSummary")
                "ERROR" -> logger.e(config.tag, "[Logpoint:${config.id}] $cappedMessage$valuesSummary")
                else -> logger.i(config.tag, "[Logpoint:${config.id}] $cappedMessage$valuesSummary")
            }
        }
    }
}

inline fun logpoint(
    id: String,
    tag: String = "Logpoint",
    defaultLevel: String = "INFO",
    callSite: String = "",
    crossinline valuesSupplier: () -> Map<String, Any?> = { emptyMap() },
    crossinline messageSupplier: () -> String
) {
    val manager = LogpointManager.getInstance()
    val config = manager.getOrRegister(id, tag, defaultLevel, callSite)
    if (!config.enabled.get()) return // Near-zero cost: atomic boolean check, zero allocation when disabled

    if (!config.shouldSample()) return

    val rawValues = valuesSupplier()

    val cond = config.condition.get()
    if (cond != null && !cond.evaluate(rawValues, tag, "")) {
        return
    }

    val message = messageSupplier()
    if (cond != null && !cond.evaluate(rawValues, tag, message)) {
        return
    }

    val redactedValues = StateRedactor.redact(rawValues) as? Map<String, Any?> ?: emptyMap()
    manager.recordHit(config, message, redactedValues)
}
