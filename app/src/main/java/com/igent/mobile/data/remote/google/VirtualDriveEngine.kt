package com.igent.mobile.data.remote.google

import com.igent.mobile.data.remote.telemetry.TelemetryInterceptor
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VirtualDriveEngine @Inject constructor(
    private val telemetryInterceptor: TelemetryInterceptor
) {
    private val client = OkHttpClient.Builder()
        .addInterceptor(telemetryInterceptor)
        .build()

    fun getClient() = client
}
