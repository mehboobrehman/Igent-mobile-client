package com.igent.mobile.data.remote.ai

import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ZenAiProviderHandler @Inject constructor(
    private val zenApiService: ZenApiService
) {
    val supportedModels = listOf(
        "zen-gpt",
        "zen-chat",
        "gpt-4o-mini",
        "llama-3.1-70b",
        "deepseek-r1-zen"
    )

    suspend fun executeChatCompletion(
        apiKey: String,
        messages: List<ChatMessage>,
        preferredModel: String = "zen-gpt"
    ): Result<String> {
        if (apiKey.isBlank()) {
            return Result.failure(IllegalArgumentException("API key cannot be blank for Zen AI provider"))
        }

        val authHeader = if (apiKey.startsWith("Bearer ")) apiKey else "Bearer $apiKey"
        val modelList = (listOf(preferredModel) + supportedModels).distinct()

        for (model in modelList) {
            try {
                val request = ChatRequest(model = model, messages = messages, stream = false)
                val response = zenApiService.createChatCompletion(authorization = authHeader, request = request)
                val content = response.choices?.firstOrNull()?.message?.content
                if (!content.isNullOrBlank()) {
                    return Result.success(content)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                // Continue to next Zen model variant
            }
        }
        return Result.failure(IllegalStateException("Zen AI provider failed to return valid completion"))
    }
}
