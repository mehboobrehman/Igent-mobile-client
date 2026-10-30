package com.igent.mobile.data.remote.config

import kotlinx.serialization.Serializable

@Serializable
data class RemoteConfig(
    val telemetryEnabled: Boolean = true,
    val force401Error: Boolean = false,
    val featureFlags: Map<String, Boolean> = emptyMap()
)
