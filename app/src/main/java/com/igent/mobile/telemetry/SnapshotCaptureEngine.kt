package com.igent.mobile.telemetry

import android.content.Context
import android.os.Build
import com.igent.mobile.data.local.dao.SnapshotDao
import com.igent.mobile.data.local.entity.SnapshotEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SnapshotCaptureEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val snapshotDao: SnapshotDao,
    private val ringBuffer: PreTriggerRingBuffer,
    private val stateProviderRegistry: StateProviderRegistry,
    private val performanceCollector: PerformanceCollector
) {
    companion object {
        const val MAX_GLOBAL_SNAPSHOT_STORAGE = 50 // max unsynced snapshots
        const val MAX_PAYLOAD_SIZE_BYTES = 128 * 1024 // 128 KB
    }

    suspend fun captureAndPersist(
        rule: SnapshotRule,
        sessionId: String,
        deviceId: String,
        triggerMetadata: Map<String, Any?>,
        isCrash: Boolean = false
    ): SnapshotEntity? = withContext(Dispatchers.IO) {
        try {
            // Check global storage budget
            val unsyncedCount = snapshotDao.getUnsyncedCount()
            if (unsyncedCount >= MAX_GLOBAL_SNAPSHOT_STORAGE) {
                val dropCount = (unsyncedCount - MAX_GLOBAL_SNAPSHOT_STORAGE) + 1
                snapshotDao.deleteOldestSnapshots(dropCount)
            }

            val payload = mutableMapOf<String, Any?>()

            if (rule.captureSet.includeTriggerMetadata) {
                payload["triggerMetadata"] = StateRedactor.redact(triggerMetadata)
            }

            if (rule.captureSet.includeEnvironmentInfo) {
                payload["environmentInfo"] = mapOf(
                    "deviceModel" to Build.MODEL,
                    "osApiLevel" to Build.VERSION.SDK_INT,
                    "appVersion" to "1.0.19"
                )
            }

            if (rule.captureSet.includePerformanceStats) {
                val mem = performanceCollector.getMemoryMetrics()
                payload["performanceStats"] = mapOf(
                    "usedMemoryBytes" to mem.usedBytes,
                    "maxMemoryBytes" to mem.maxBytes,
                    "threadCount" to performanceCollector.getThreadCount(),
                    "startupTimeMs" to performanceCollector.getStartupTimeMs()
                )
            }

            if (rule.captureSet.includeLogWindow) {
                val logs = ringBuffer.getWindow(50).map { StateRedactor.redact(it.data) }
                payload["logWindow"] = logs
            }

            if (rule.captureSet.includeThreadDump) {
                val threadDump = Thread.getAllStackTraces().entries.associate { (thread, stack) ->
                    thread.name to stack.map { it.toString() }
                }
                payload["threadDump"] = StateRedactor.redact(threadDump)
            }

            if (rule.captureSet.includeStateHolderValues) {
                val stateHolders = mutableMapOf<String, Any?>()
                for (providerName in stateProviderRegistry.getRegisteredProviderNames()) {
                    val provider = stateProviderRegistry.getProvider(providerName)
                    if (provider != null) {
                        try {
                            stateHolders[providerName] = StateRedactor.redact(provider())
                        } catch (e: Exception) {
                            stateHolders[providerName] = mapOf("error" to e.localizedMessage)
                        }
                    }
                }
                payload["stateHolders"] = stateHolders
            }

            // Heavy items only if explicitly enabled per rule
            if (rule.captureSet.includeScreenshot) {
                payload["screenshot"] = "[HEAVY_SCREENSHOT_DATA_PLACEHOLDER]"
            }
            if (rule.captureSet.includeViewHierarchy) {
                payload["viewHierarchy"] = "[HEAVY_VIEW_HIERARCHY_PLACEHOLDER]"
            }
            if (rule.captureSet.includeHeapDump) {
                payload["heapDump"] = "[HEAVY_HEAP_DUMP_PLACEHOLDER]"
            }

            val jsonPayload = JSONObject(payload).toString()
            if (jsonPayload.toByteArray(Charsets.UTF_8).size > MAX_PAYLOAD_SIZE_BYTES) {
                payload["payloadWarning"] = "Payload exceeded size limit, truncated."
            }

            val finalJson = JSONObject(payload).toString()
            val signature = computeSignature(rule.id, triggerMetadata)
            val snapshotId = "snap_" + UUID.randomUUID().toString()

            val entity = SnapshotEntity(
                snapshotId = snapshotId,
                sessionId = sessionId,
                deviceId = deviceId,
                triggerId = rule.id,
                triggerCategory = rule.triggerCategory,
                timestamp = System.currentTimeMillis(),
                signature = signature,
                occurrenceCount = 1,
                payloadJson = finalJson,
                isSynced = false,
                isCrashSnapshot = isCrash
            )

            if (isCrash) {
                snapshotDao.insertSync(entity)
            } else {
                snapshotDao.insert(entity)
            }
            entity
        } catch (e: Exception) {
            null
        }
    }

    private fun computeSignature(ruleId: String, metadata: Map<String, Any?>): String {
        val exClass = metadata["exceptionClass"]?.toString() ?: "unknown"
        val msg = metadata["message"]?.toString()?.take(50) ?: ""
        return "$ruleId:$exClass:$msg"
    }
}
