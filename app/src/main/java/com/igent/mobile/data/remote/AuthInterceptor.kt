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
        val url = originalRequest.url.toString()

        // Determine request type and inject appropriate token/credentials
        val requestBuilder = originalRequest.newBuilder()

        // Check if request is for OpenRouter or Groq
        val rawApiKey = runBlocking {
            try {
                preferencesDataSource.apiKey.first()
            } catch (e: Exception) {
                ""
            }
        }

        val apiKey = KeyObfuscator.deobfuscate(rawApiKey).ifBlank { rawApiKey }

        if (url.contains("openrouter.ai") || url.contains("api.groq.com")) {
            if (apiKey.isNotBlank()) {
                val authHeaderValue = if (url.contains("openrouter.ai")) {
                    if (apiKey.startsWith("Bearer ")) apiKey else "Bearer $apiKey"
                } else {
                    if (apiKey.startsWith("Bearer ")) apiKey else "Bearer $apiKey"
                }
                requestBuilder.header("Authorization", authHeaderValue)
            }
        } else {
            // General or other authenticated requests (e.g. PSX or custom endpoints)
            if (apiKey.isNotBlank()) {
                requestBuilder.header("X-API-Key", apiKey)
            }
        }

        val response = chain.proceed(requestBuilder.build())
        val urlStr = originalRequest.url.toString()

        // Handle 401 Unauthorized globally for app backend endpoints ONLY.
        // External third-party requests (OpenRouter, Groq, Pollinations) should NOT clear session on 401.
        val isExternal = urlStr.contains("openrouter.ai") || 
                        urlStr.contains("api.groq.com") || 
                        urlStr.contains("pollinations.ai")

        if (response.code == 401 && !isExternal) {
            sessionManager.handleUnauthorized()
        }

        return response
    }
}
