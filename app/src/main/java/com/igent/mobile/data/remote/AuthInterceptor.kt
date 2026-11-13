package com.igent.mobile.data.remote

import com.igent.mobile.data.preferences.PreferencesDataSource
import com.igent.mobile.data.util.KeyObfuscator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val preferencesDataSource: PreferencesDataSource,
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestUrl = originalRequest.url
        val host = requestUrl.host
        val urlStr = requestUrl.toString()

        val requestBuilder = originalRequest.newBuilder()

        val rawApiKey = runBlocking {
            try {
                preferencesDataSource.apiKey.first()
            } catch (e: Exception) {
                ""
            }
        }

        val apiKey = try {
            com.igent.mobile.data.util.KeyObfuscator.deobfuscate(rawApiKey).ifBlank { rawApiKey }
        } catch (e: Exception) {
            rawApiKey
        }

        val isOpenRouterOrGroq = host.equals("openrouter.ai", ignoreCase = true) ||
                host.endsWith(".openrouter.ai", ignoreCase = true) ||
                host.equals("api.groq.com", ignoreCase = true) ||
                host.endsWith(".api.groq.com", ignoreCase = true)

        if (isOpenRouterOrGroq && apiKey.isNotBlank()) {
            val authHeaderValue = if (apiKey.startsWith("Bearer ")) apiKey else "Bearer $apiKey"
            requestBuilder.header("Authorization", authHeaderValue)
            requestBuilder.header("X-API-Key", apiKey)
        }

        val response = chain.proceed(requestBuilder.build())

        // Handle 401 Unauthorized globally for app backend endpoints ONLY.
        // External third-party requests (OpenRouter, Groq, Pollinations) should NOT clear session on 401.
        val isExternal = isOpenRouterOrGroq || urlStr.contains("pollinations.ai")

        if (response.code == 401 && !isExternal) {
            sessionManager.handleUnauthorized()
        }

        return response
    }
}
