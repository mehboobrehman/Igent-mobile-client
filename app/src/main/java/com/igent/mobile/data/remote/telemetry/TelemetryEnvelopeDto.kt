package com.igent.mobile.data.remote.telemetry

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

@Serializable
data class TelemetryEnvelopeDto(
    val schemaVersion: String = "1.0.0",
    val sessionId: String,
    val deviceId: String,
    val seq: Long,
    val timestamp: Long,
    val wallTime: Long,
    val type: String, // logs, crashes, anrs, network, performance, lifecycle, state, config, commands, heartbeats
    val payload: JsonObject
) {
    companion object {
        fun createLog(
            sessionId: String,
            deviceId: String,
            seq: Long,
            level: String,
            message: String,
            tag: String
        ): TelemetryEnvelopeDto {
            val now = System.currentTimeMillis()
            val payloadObj = buildJsonObject {
                put("level", JsonPrimitive(level))
                put("message", JsonPrimitive(message))
                put("tag", JsonPrimitive(tag))
            }
            return TelemetryEnvelopeDto(
                schemaVersion = "1.0.0",
                sessionId = sessionId,
                deviceId = deviceId,
                seq = seq,
                timestamp = now,
                wallTime = now,
                type = "logs",
                payload = payloadObj
            )
        }
    }
}

fun validateTelemetryEnvelope(envelope: TelemetryEnvelopeDto): Boolean {
    val validTypes = setOf("logs", "crashes", "anrs", "network", "performance", "lifecycle", "state", "config", "commands", "heartbeats")
    return envelope.schemaVersion == "1.0.0" &&
           envelope.sessionId.isNotBlank() &&
           envelope.deviceId.isNotBlank() &&
           envelope.type in validTypes
}
