package com.igent.mobile.data.remote.telemetry

import com.igent.mobile.BuildConfig
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

        val telemetryBaseUrl = runBlocking {
            try {
                preferencesDataSource.telemetryBaseUrl.first()
            } catch (e: Exception) {
                "http://10.0.2.2:3000/"
            }
        }
        val targetUrl = telemetryBaseUrl.toHttpUrlOrNull()
        val targetHost = targetUrl?.host

        val isTelemetryHost = url.host == "igent-telemetry.local" ||
                (BuildConfig.DEBUG_TELEMETRY_ENABLED && targetHost != null && url.host == targetHost)

        if (isTelemetryHost) {
            val token = BuildConfig.TELEMETRY_TOKEN

            val newRequest = if (targetUrl != null && url.host == "igent-telemetry.local") {
                val newUrl = url.newBuilder()
                    .scheme(targetUrl.scheme)
                    .host(targetUrl.host)
                    .port(targetUrl.port)
                    .build()

                originalRequest.newBuilder()
                    .url(newUrl)
                    .header("X-Telemetry-Token", token)
                    .build()
            } else {
                originalRequest.newBuilder()
                    .header("X-Telemetry-Token", token)
                    .build()
            }

            return chain.proceed(newRequest)
        }

        return chain.proceed(originalRequest)
    }
}
