package com.igent.mobile.data.remote.telemetry

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TelemetryApiService {
    @POST("api/telemetry")
    suspend fun postLog(@Body envelope: TelemetryEnvelopeDto): Response<Unit>

    @POST("api/telemetry")
    suspend fun postLogBatch(@Body envelopes: List<TelemetryEnvelopeDto>): Response<Unit>
}
