package com.igent.mobile.data.remote.telemetry

import com.igent.mobile.util.AgentLogger
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelemetryInterceptor @Inject constructor(
    private val logger: AgentLogger
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val tag = "Network"
        
        logger.i(tag, "--> ${request.method} ${request.url}")
        request.headers.forEach { pair ->
            logger.d(tag, "Header: ${pair.first}: ${pair.second}")
        }

        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            logger.e(tag, "<-- HTTP FAILED: $e")
            throw e
        }

        logger.i(tag, "<-- ${response.code} ${request.url}")
        return response
    }
}
