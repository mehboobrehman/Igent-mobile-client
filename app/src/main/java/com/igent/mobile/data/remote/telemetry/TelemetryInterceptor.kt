package com.igent.mobile.data.remote.telemetry

import com.igent.mobile.util.AgentLogger
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelemetryInterceptor @Inject constructor(
    private val logger: AgentLogger
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val tag = "NetworkTelemetry"
        
        val requestBodyString = request.body?.let { body ->
            try {
                val buffer = Buffer()
                body.writeTo(buffer)
                buffer.readUtf8()
            } catch (e: Exception) {
                "[Could not read request body: ${e.message}]"
            }
        } ?: ""

        val requestInfo = buildString {
            append("--> ${request.method} ${request.url}")
            if (requestBodyString.isNotBlank()) {
                append(" | Body: ${requestBodyString.take(1000)}")
            }
        }
        logger.i(tag, requestInfo)

        val startTime = System.nanoTime()
        val response: Response
        val durationMs: Long
        
        try {
            response = chain.proceed(request)
            durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
        } catch (e: Exception) {
            val failedDurationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
            logger.e(tag, "<-- HTTP FAILED: ${request.method} ${request.url} in ${failedDurationMs}ms: ${e.localizedMessage}", e)
            throw e
        }

        val code = response.code
        val statusCategory = when (code) {
            in 200..299 -> "Success"
            in 400..499 -> "ClientError"
            in 500..599 -> "ServerError"
            else -> "Unknown"
        }

        val responseBody = response.body
        val source = responseBody?.source()
        source?.request(Long.MAX_VALUE) // Buffer the entire body
        val buffer = source?.buffer
        val responseBodyString = buffer?.clone()?.readUtf8() ?: ""
        val contentLength = responseBody?.contentLength() ?: -1L

        val responseInfo = buildString {
            append("<-- [$statusCategory] ${response.code} ${request.method} ${request.url} | Latency: ${durationMs}ms | Size: ${contentLength}bytes")
            if (responseBodyString.isNotBlank()) {
                append(" | Res: ${responseBodyString.take(1000)}")
            }
        }

        when (statusCategory) {
            "Success" -> logger.i(tag, responseInfo)
            "ClientError" -> logger.w(tag, responseInfo)
            "ServerError" -> logger.e(tag, responseInfo)
            else -> logger.d(tag, responseInfo)
        }

        return response
    }
}
