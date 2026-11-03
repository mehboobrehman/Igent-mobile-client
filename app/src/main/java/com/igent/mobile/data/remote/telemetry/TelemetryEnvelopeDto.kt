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
            return createLog(sessionId, deviceId, seq, level, message, tag, null)
        }

        fun createLog(
            sessionId: String,
            deviceId: String,
            seq: Long,
            level: String,
            message: String,
            tag: String,
            correlationId: String?
        ): TelemetryEnvelopeDto {
            val now = System.currentTimeMillis()
            val payloadObj = buildJsonObject {
                put("level", JsonPrimitive(level))
                put("message", JsonPrimitive(message))
                put("tag", JsonPrimitive(tag))
                if (correlationId != null) {
                    put("correlationId", JsonPrimitive(correlationId))
                }
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

        fun createCrash(
            sessionId: String,
            deviceId: String,
            seq: Long,
            threadName: String,
            exceptionClass: String,
            message: String,
            stackTrace: String
        ): TelemetryEnvelopeDto {
            val now = System.currentTimeMillis()
            val payloadObj = buildJsonObject {
                put("threadName", JsonPrimitive(threadName))
                put("exceptionClass", JsonPrimitive(exceptionClass))
                put("message", JsonPrimitive(message))
                put("stackTrace", JsonPrimitive(stackTrace))
            }
            return TelemetryEnvelopeDto(
                schemaVersion = "1.0.0",
                sessionId = sessionId,
                deviceId = deviceId,
                seq = seq,
                timestamp = now,
                wallTime = now,
                type = "crashes",
                payload = payloadObj
            )
        }

        fun createAnr(
            sessionId: String,
            deviceId: String,
            seq: Long,
            threadName: String,
            stackTrace: String,
            durationMs: Long
        ): TelemetryEnvelopeDto {
            val now = System.currentTimeMillis()
            val payloadObj = buildJsonObject {
                put("threadName", JsonPrimitive(threadName))
                put("stackTrace", JsonPrimitive(stackTrace))
                put("durationMs", JsonPrimitive(durationMs))
            }
            return TelemetryEnvelopeDto(
                schemaVersion = "1.0.0",
                sessionId = sessionId,
                deviceId = deviceId,
                seq = seq,
                timestamp = now,
                wallTime = now,
                type = "anrs",
                payload = payloadObj
            )
        }

        fun createNetwork(
            sessionId: String,
            deviceId: String,
            seq: Long,
            method: String,
            url: String,
            statusCode: Int,
            durationMs: Long,
            requestSize: Long,
            responseSize: Long
        ): TelemetryEnvelopeDto {
            val now = System.currentTimeMillis()
            val payloadObj = buildJsonObject {
                put("method", JsonPrimitive(method))
                put("url", JsonPrimitive(url))
                put("statusCode", JsonPrimitive(statusCode))
                put("durationMs", JsonPrimitive(durationMs))
                put("requestSize", JsonPrimitive(requestSize))
                put("responseSize", JsonPrimitive(responseSize))
            }
            return TelemetryEnvelopeDto(
                schemaVersion = "1.0.0",
                sessionId = sessionId,
                deviceId = deviceId,
                seq = seq,
                timestamp = now,
                wallTime = now,
                type = "network",
                payload = payloadObj
            )
        }

        fun createPerformance(
            sessionId: String,
            deviceId: String,
            seq: Long,
            startupTimeMs: Long,
            memoryUsedBytes: Long,
            memoryMaxBytes: Long,
            threadCount: Int
        ): TelemetryEnvelopeDto {
            val now = System.currentTimeMillis()
            val payloadObj = buildJsonObject {
                put("startupTimeMs", JsonPrimitive(startupTimeMs))
                put("memoryUsedBytes", JsonPrimitive(memoryUsedBytes))
                put("memoryMaxBytes", JsonPrimitive(memoryMaxBytes))
                put("threadCount", JsonPrimitive(threadCount))
            }
            return TelemetryEnvelopeDto(
                schemaVersion = "1.0.0",
                sessionId = sessionId,
                deviceId = deviceId,
                seq = seq,
                timestamp = now,
                wallTime = now,
                type = "performance",
                payload = payloadObj
            )
        }

        fun createLifecycle(
            sessionId: String,
            deviceId: String,
            seq: Long,
            activityName: String,
            eventType: String
        ): TelemetryEnvelopeDto {
            val now = System.currentTimeMillis()
            val payloadObj = buildJsonObject {
                put("activityName", JsonPrimitive(activityName))
                put("eventType", JsonPrimitive(eventType))
            }
            return TelemetryEnvelopeDto(
                schemaVersion = "1.0.0",
                sessionId = sessionId,
                deviceId = deviceId,
                seq = seq,
                timestamp = now,
                wallTime = now,
                type = "lifecycle",
                payload = payloadObj
            )
        }

        fun createCommand(
            sessionId: String,
            deviceId: String,
            seq: Long,
            commandId: String,
            commandName: String,
            parameters: String
        ): TelemetryEnvelopeDto {
            val now = System.currentTimeMillis()
            val payloadObj = buildJsonObject {
                put("commandId", JsonPrimitive(commandId))
                put("commandName", JsonPrimitive(commandName))
                put("parameters", JsonPrimitive(parameters))
            }
            return TelemetryEnvelopeDto(
                schemaVersion = "1.0.0",
                sessionId = sessionId,
                deviceId = deviceId,
                seq = seq,
                timestamp = now,
                wallTime = now,
                type = "commands",
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
