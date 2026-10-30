package com.igent.mobile.data.remote.google

import com.igent.mobile.data.remote.telemetry.TelemetryInterceptor
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleOAuthManager @Inject constructor(
    private val telemetryInterceptor: TelemetryInterceptor
) {
    private val client = OkHttpClient.Builder()
        .addInterceptor(telemetryInterceptor)
        // Add auth interceptors here if needed
        .build()

    fun getClient() = client
}
