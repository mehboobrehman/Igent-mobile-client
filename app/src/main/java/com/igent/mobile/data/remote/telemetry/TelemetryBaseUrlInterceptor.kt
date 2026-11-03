package com.igent.mobile.data.remote.telemetry

import com.igent.mobile.data.preferences.PreferencesDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelemetryBaseUrlInterceptor @Inject constructor(
    private val preferencesDataSource: PreferencesDataSource
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url

        val isTelemetry = url.host == "igent-telemetry.local" || url.encodedPath.contains("telemetry") || url.toString().contains("telemetry")

        if (isTelemetry) {
            val telemetryBaseUrl = runBlocking {
                try {
                    preferencesDataSource.telemetryBaseUrl.first()
                } catch (e: Exception) {
                    "http://10.0.2.2:3000/"
                }
            }

            val targetUrl = telemetryBaseUrl.toHttpUrlOrNull()
            if (targetUrl != null) {
                val newUrl = url.newBuilder()
                    .scheme(targetUrl.scheme)
                    .host(targetUrl.host)
                    .port(targetUrl.port)
                    .build()

                val newRequest = originalRequest.newBuilder()
                    .url(newUrl)
                    .header("X-Telemetry-Token", "igent-telemetry-secret-token-2026")
                    .build()

                return chain.proceed(newRequest)
            }
        }

        return chain.proceed(originalRequest)
    }
}
