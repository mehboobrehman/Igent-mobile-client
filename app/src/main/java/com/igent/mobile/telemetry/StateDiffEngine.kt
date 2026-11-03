package com.igent.mobile.telemetry

data class StateDiff(
    val added: Map<String, Any?> = emptyMap(),
    val removed: Map<String, Any?> = emptyMap(),
    val modified: Map<String, StateChange> = emptyMap(),
    val unchanged: Map<String, Any?> = emptyMap()
)

data class StateChange(
    val oldValue: Any?,
    val newValue: Any?
)

object StateDiffEngine {
    fun diff(oldState: Map<String, Any?>, newState: Map<String, Any?>): StateDiff {
        val added = mutableMapOf<String, Any?>()
        val removed = mutableMapOf<String, Any?>()
        val modified = mutableMapOf<String, StateChange>()
        val unchanged = mutableMapOf<String, Any?>()

        val allKeys = (oldState.keys + newState.keys).toSet()

        for (key in allKeys) {
            val oldVal = oldState[key]
            val newVal = newState[key]

            when {
                !oldState.containsKey(key) -> added[key] = newVal
                !newState.containsKey(key) -> removed[key] = oldVal
                oldVal != newVal -> modified[key] = StateChange(oldVal, newVal)
                else -> unchanged[key] = oldVal
            }
        }

        return StateDiff(added, removed, modified, unchanged)
    }
}
