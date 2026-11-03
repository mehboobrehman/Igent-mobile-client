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

    private val sensitiveHeaderKeys = setOf(
        "authorization", "cookie", "set-cookie", "x-api-key", "api-key", "token", "secret", "password", "credential"
    )

    private val sensitiveBodyKeys = listOf(
        "password", "token", "secret", "authorization", "creditCard", "cardNumber", "cvv", "pin", "ssn", "api_key", "apiKey", "key"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        if (request.url.encodedPath.contains("telemetry")) {
            return chain.proceed(request)
        }
        
        val tag = "NetworkTelemetry"
        
        val requestBodyString = request.body?.let { body ->
            try {
                val buffer = Buffer()
                body.writeTo(buffer)
                redactBodyString(buffer.readUtf8())
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
        source?.request(Long.MAX_VALUE)
        val buffer = source?.buffer
        val rawResponseBodyString = buffer?.clone()?.readUtf8() ?: ""
        val responseBodyString = redactBodyString(rawResponseBodyString)
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

    private fun redactBodyString(body: String): String {
        var result = body
        for (key in sensitiveBodyKeys) {
            val regex = Regex("(\"$key\"\\s*:\\s*\")([^\"]*)(\")", RegexOption.IGNORE_CASE)
            result = regex.replace(result, "$1[REDACTED]$3")
        }
        return result
    }
}
