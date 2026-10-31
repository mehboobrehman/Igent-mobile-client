package com.igent.mobile.data.remote.telemetry

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TelemetryApiService {
    @POST("api/telemetry")
    suspend fun postLog(@Body log: TelemetryLogDto): Response<Unit>

    @POST("api/telemetry/batch")
    suspend fun postLogBatch(@Body logs: List<TelemetryLogDto>): Response<Unit>
}
