package com.igent.mobile.data.remote.telemetry

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

@Serializable
data class HandshakeRequestDto(
    val deviceId: String,
    val deviceModel: String,
    val osApiLevel: Int,
    val appVersion: String,
    val versionCode: Int,
    val capabilities: List<String>,
    val clientTimestamp: Long
)

@Serializable
data class HandshakeResponseDto(
    val success: Boolean,
    val serverTimestamp: String,
    val syncId: String? = null
)

interface TelemetryApiService {
    @POST("api/telemetry")
    suspend fun postLog(@Body envelope: TelemetryEnvelopeDto): Response<Unit>

    @POST("api/telemetry")
    suspend fun postLogBatch(@Body envelopes: List<TelemetryEnvelopeDto>): Response<Unit>

    @POST("api/sync")
    suspend fun handshake(@Body request: HandshakeRequestDto): Response<HandshakeResponseDto>
}
