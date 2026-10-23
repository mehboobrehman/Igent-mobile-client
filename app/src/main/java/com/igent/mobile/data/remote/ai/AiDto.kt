package com.igent.mobile.data.remote.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    @SerialName("model") val model: String,
    @SerialName("messages") val messages: List<ChatMessage>,
    @SerialName("stream") val stream: Boolean = false,
    @SerialName("temperature") val temperature: Double = 0.7
)

@Serializable
data class ChatMessage(
    @SerialName("role") val role: String,
    @SerialName("content") val content: String
)

@Serializable
data class ChatResponse(
    @SerialName("id") val id: String? = null,
    @SerialName("choices") val choices: List<Choice>? = null
)

@Serializable
data class Choice(
    @SerialName("index") val index: Int? = null,
    @SerialName("message") val message: ChatMessage? = null,
    @SerialName("delta") val delta: ChatDelta? = null,
    @SerialName("finish_reason") val finishReason: String? = null
)

@Serializable
data class ChatDelta(
    @SerialName("role") val role: String? = null,
    @SerialName("content") val content: String? = null
)
