package com.igent.mobile.telemetry

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SnapshotRuleEngine @Inject constructor(
    private val captureEngine: SnapshotCaptureEngine,
    private val ringBuffer: PreTriggerRingBuffer
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val rules = ConcurrentHashMap<String, SnapshotRule>()
    private val lastTriggerTimes = ConcurrentHashMap<String, Long>()
    private val sessionTriggerCounts = ConcurrentHashMap<String, Int>()

    fun setRules(newRules: List<SnapshotRule>) {
        rules.clear()
        for (rule in newRules) {
            if (rule.enabled) {
                rules[rule.id] = rule
            }
        }
    }

    fun addRule(rule: SnapshotRule) {
        if (rule.enabled) {
            rules[rule.id] = rule
        } else {
            rules.remove(rule.id)
        }
    }

    fun getRules(): List<SnapshotRule> = rules.values.toList()

    fun evaluateEvent(
        category: String,
        eventData: Map<String, Any?>,
        sessionId: String,
        deviceId: String,
        isCrash: Boolean = false
    ) {
        ringBuffer.add(category, eventData)

        scope.launch {
            val now = System.currentTimeMillis()
            for (rule in rules.values) {
                if (rule.triggerCategory != category && !isCrash) continue

                // Check condition match
                if (!matchesCondition(rule, eventData)) continue

                // Check cooldown
                val lastTime = lastTriggerTimes[rule.id] ?: 0L
                if (now - lastTime < rule.cooldownMs) continue

                // Check session max
                val sessionCount = sessionTriggerCounts[rule.id] ?: 0
                if (sessionCount >= rule.maxPerSession) continue

                // Update cooldown & session count
                lastTriggerTimes[rule.id] = now
                sessionTriggerCounts[rule.id] = sessionCount + 1

                captureEngine.captureAndPersist(rule, sessionId, deviceId, eventData, isCrash)
            }
        }
    }

    private fun matchesCondition(rule: SnapshotRule, eventData: Map<String, Any?>): Boolean {
        if (rule.condition.isBlank() || rule.condition == "*") return true
        val conditionLower = rule.condition.lowercase()
        
        for ((k, v) in eventData) {
            val valStr = v?.toString()?.lowercase() ?: ""
            if (valStr.contains(conditionLower) || k.lowercase().contains(conditionLower)) {
                return true
            }
        }
        return false
    }

    fun reset() {
        rules.clear()
        lastTriggerTimes.clear()
        sessionTriggerCounts.clear()
    }
}
