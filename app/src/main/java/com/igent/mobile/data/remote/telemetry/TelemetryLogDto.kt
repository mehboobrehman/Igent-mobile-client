package com.igent.mobile.data.remote.telemetry

import kotlinx.serialization.Serializable

@Serializable
data class TelemetryLogDto(
    val timestamp: Long,
    val level: String,
    val message: String,
    val tag: String
)
