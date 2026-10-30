package com.igent.mobile.data.remote.ai

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
        val authHeader = if (apiKey.isNotBlank() && !apiKey.startsWith("Bearer ")) "Bearer $apiKey" else apiKey.ifBlank { "Bearer zen-placeholder-key" }
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
                // Continue to next Zen model variant
            }
        }
        return Result.failure(IllegalStateException("Zen AI provider failed to return valid completion"))
    }
}
