package com.igent.mobile.telemetry

import java.util.concurrent.ArrayBlockingQueue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreTriggerRingBuffer @Inject constructor() {
    private var buffer = ArrayBlockingQueue<RingBufferItem>(200)

    constructor(capacity: Int) : this() {
        buffer = ArrayBlockingQueue(capacity)
    }

    fun add(type: String, data: Map<String, Any?>, timestamp: Long = System.currentTimeMillis()) {
        val item = RingBufferItem(type, data, timestamp)
        if (!buffer.offer(item)) {
            buffer.poll() // remove oldest
            buffer.offer(item)
        }
    }

    fun getWindow(limit: Int = 100): List<RingBufferItem> {
        val all = buffer.toList()
        return if (all.size <= limit) all else all.takeLast(limit)
    }

    fun clear() {
        buffer.clear()
    }

    data class RingBufferItem(
        val type: String,
        val data: Map<String, Any?>,
        val timestamp: Long
    )
}
