package com.igent.mobile.data.repository

import com.igent.mobile.data.local.dao.MessageDao
import com.igent.mobile.data.local.entity.MessageEntity
import com.igent.mobile.data.preferences.PreferencesDataSource
import com.igent.mobile.data.remote.ai.ChatDelta
import com.igent.mobile.data.remote.ai.ChatMessage
import com.igent.mobile.data.remote.ai.ChatRequest
import com.igent.mobile.data.remote.ai.GroqApiService
import com.igent.mobile.data.remote.ai.OpenRouterApiService
import com.igent.mobile.data.util.KeyObfuscator
import com.igent.mobile.domain.model.Message
import com.igent.mobile.domain.repository.AiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val openRouterApiService: OpenRouterApiService,
    private val groqApiService: GroqApiService,
    private val preferencesDataSource: PreferencesDataSource
) : AiRepository {

    override suspend fun sendMessage(prompt: String): Result<Message> = withContext(Dispatchers.IO) {
        try {
            val userMessage = Message(
                id = java.util.UUID.randomUUID().toString(),
                senderId = "user",
                content = prompt,
                timestamp = System.currentTimeMillis(),
                isFromUser = true
            )
            messageDao.insertMessage(MessageEntity.fromDomain(userMessage))

            val history = messageDao.getAllMessages().first().takeLast(10).map { entity ->
                ChatMessage(
                    role = if (entity.isFromUser) "user" else "assistant",
                    content = entity.content
                )
            }

            val rawKey = preferencesDataSource.apiKey.first()
            val apiKey = KeyObfuscator.deobfuscate(rawKey)

            val messagesList = if (history.isEmpty()) {
                listOf(ChatMessage(role = "user", content = prompt))
            } else {
                history
            }

            var aiResponseContent = ""
            var success = false

            // 1. Try OpenRouter first if api key or default model configured
            try {
                val openRouterKey = if (apiKey.isNotBlank()) "Bearer $apiKey" else "Bearer sk-or-v1-placeholder"
                val request = ChatRequest(
                    model = "anthropic/claude-3-haiku",
                    messages = messagesList,
                    stream = false
                )
                val response = openRouterApiService.createChatCompletion(
                    authorization = openRouterKey,
                    request = request
                )
                val content = response.choices?.firstOrNull()?.message?.content
                if (!content.isNullOrBlank()) {
                    aiResponseContent = content
                    success = true
                }
            } catch (e: Exception) {
                // OpenRouter failed, attempt Groq fallback
            }

            // 2. Fallback to Groq if OpenRouter failed
            if (!success) {
                try {
                    val groqKey = if (apiKey.isNotBlank()) "Bearer $apiKey" else "Bearer gsk_placeholder"
                    val request = ChatRequest(
                        model = "llama3-70b-8192",
                        messages = messagesList,
                        stream = false
                    )
                    val response = groqApiService.createChatCompletion(
                        authorization = groqKey,
                        request = request
                    )
                    val content = response.choices?.firstOrNull()?.message?.content
                    if (!content.isNullOrBlank()) {
                        aiResponseContent = content
                        success = true
                    }
                } catch (e: Exception) {
                    // Groq fallback also failed
                }
            }

            // 3. Ultimate local fallback if both APIs fail or keys are missing
            if (!success || aiResponseContent.isBlank()) {
                aiResponseContent = "Igent AI (Offline/Fallback): I received your message: '$prompt'. Both OpenRouter and Groq APIs were unavailable or unconfigured."
            }

            val aiMessage = Message(
                id = java.util.UUID.randomUUID().toString(),
                senderId = "ai",
                content = aiResponseContent,
                timestamp = System.currentTimeMillis(),
                isFromUser = false
            )
            messageDao.insertMessage(MessageEntity.fromDomain(aiMessage))

            Result.success(aiMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun sendMessageStream(prompt: String): Flow<String> = flow {
        val rawKey = preferencesDataSource.apiKey.first()
        val apiKey = KeyObfuscator.deobfuscate(rawKey)
        val messagesList = listOf(ChatMessage(role = "user", content = prompt))

        var streamed = false
        try {
            val openRouterKey = if (apiKey.isNotBlank()) "Bearer $apiKey" else "Bearer sk-or-v1-placeholder"
            val request = ChatRequest(
                model = "anthropic/claude-3-haiku",
                messages = messagesList,
                stream = true
            )
            val response = openRouterApiService.createChatCompletionStream(
                authorization = openRouterKey,
                request = request
            )
            if (response.isSuccessful) {
                response.body()?.byteStream()?.bufferedReader()?.use { reader ->
                    var line = reader.readLine()
                    while (line != null) {
                        if (line.startsWith("data: ")) {
                            val jsonStr = line.removePrefix("data: ").trim()
                            if (jsonStr == "[DONE]") break
                            // Simple parsing for content delta or emit chunk
                            if (jsonStr.contains("content")) {
                                // Extract content chunk using basic parsing or emit raw text
                                emit(jsonStr)
                            }
                        }
                        line = reader.readLine()
                    }
                }
                streamed = true
            }
        } catch (e: Exception) {
            // OpenRouter stream failed, try Groq stream
        }

        if (!streamed) {
            try {
                val groqKey = if (apiKey.isNotBlank()) "Bearer $apiKey" else "Bearer gsk_placeholder"
                val request = ChatRequest(
                    model = "llama3-70b-8192",
                    messages = messagesList,
                    stream = true
                )
                val response = groqApiService.createChatCompletionStream(
                    authorization = groqKey,
                    request = request
                )
                if (response.isSuccessful) {
                    response.body()?.byteStream()?.bufferedReader()?.use { reader ->
                        var line = reader.readLine()
                        while (line != null) {
                            if (line.startsWith("data: ")) {
                                val jsonStr = line.removePrefix("data: ").trim()
                                if (jsonStr == "[DONE]") break
                                emit(jsonStr)
                            }
                            line = reader.readLine()
                        }
                    }
                }
            } catch (e: Exception) {
                emit("Error streaming response from AI providers.")
            }
        }
    }

    override fun getMessageHistory(): Flow<List<Message>> {
        return messageDao.getAllMessages().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
