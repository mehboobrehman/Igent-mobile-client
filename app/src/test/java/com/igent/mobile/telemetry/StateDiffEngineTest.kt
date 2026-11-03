package com.igent.mobile.telemetry

import org.junit.Assert.*
import org.junit.Test

class StateDiffEngineTest {

    @Test
    fun testStateDiffComputation() {
        val oldState = mapOf(
            "unchanged_key" to "value1",
            "modified_key" to "old_value",
            "removed_key" to "to_be_removed"
        )

        val newState = mapOf(
            "unchanged_key" to "value1",
            "modified_key" to "new_value",
            "added_key" to "brand_new"
        )

        val diff = StateDiffEngine.diff(oldState, newState)

        assertEquals(mapOf("added_key" to "brand_new"), diff.added)
        assertEquals(mapOf("removed_key" to "to_be_removed"), diff.removed)
        assertEquals(mapOf("unchanged_key" to "value1"), diff.unchanged)

        assertEquals(1, diff.modified.size)
        val mod = diff.modified["modified_key"]
        assertNotNull(mod)
        assertEquals("old_value", mod!!.oldValue)
        assertEquals("new_value", mod.newValue)
    }
}
