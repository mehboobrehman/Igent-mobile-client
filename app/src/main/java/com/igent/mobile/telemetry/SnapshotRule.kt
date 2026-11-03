package com.igent.mobile.telemetry

import kotlinx.serialization.Serializable

@Serializable
data class SnapshotRule(
    val id: String,
    val triggerCategory: String, // errors, performance, network, app_logic, system, manual
    val condition: String, // expression or threshold matcher
    val captureSet: SnapshotCaptureSetConfig = SnapshotCaptureSetConfig(),
    val cooldownMs: Long = 60000L,
    val maxPerSession: Int = 5,
    val enabled: Boolean = true
)

@Serializable
data class SnapshotCaptureSetConfig(
    val includeLogWindow: Boolean = true,
    val includeThreadDump: Boolean = true,
    val includeNetworkCalls: Boolean = true,
    val includePerformanceStats: Boolean = true,
    val includeLifecycleState: Boolean = true,
    val includeStateHolderValues: Boolean = true,
    val includeEnvironmentInfo: Boolean = true,
    val includeTriggerMetadata: Boolean = true,
    // Heavy items disabled by default, enabled per rule only
    val includeScreenshot: Boolean = false,
    val includeViewHierarchy: Boolean = false,
    val includeHeapDump: Boolean = false
)
