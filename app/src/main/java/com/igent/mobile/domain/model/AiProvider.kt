package com.igent.mobile.domain.model

enum class AiProvider(
    val id: String,
    val displayName: String,
    val baseUrl: String,
    val defaultModel: String,
    val description: String
) {
    OPEN_ROUTER(
        id = "openrouter",
        displayName = "OpenRouter",
        baseUrl = "https://openrouter.ai/",
        defaultModel = "anthropic/claude-3-haiku",
        description = "Unified AI Gateway (Claude 3, Gemma 2, Llama 3)"
    ),
    GROQ(
        id = "groq",
        displayName = "Groq LPU",
        baseUrl = "https://api.groq.com/",
        defaultModel = "llama-3.3-70b-versatile",
        description = "Ultra-fast low latency LPU inference"
    ),
    ZEN_AI(
        id = "zen",
        displayName = "Zen AI Provider",
        baseUrl = "https://api.zenai.network/",
        defaultModel = "zen-gpt",
        description = "Zen Financial & Multi-Model Intelligence Service"
    ),
    POLLINATIONS(
        id = "pollinations",
        displayName = "Pollinations AI",
        baseUrl = "https://text.pollinations.ai/",
        defaultModel = "openai",
        description = "Public Free High-Performance Endpoint"
    );

    companion object {
        fun fromId(id: String): AiProvider = values().find { it.id.equals(id, ignoreCase = true) } ?: OPEN_ROUTER
    }
}
