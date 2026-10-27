package com.igent.mobile.data.repository

import com.igent.mobile.data.local.dao.MessageDao
import com.igent.mobile.data.local.entity.MessageEntity
import com.igent.mobile.data.preferences.PreferencesDataSource
import com.igent.mobile.data.remote.ai.ChatDelta
import com.igent.mobile.data.remote.ai.ChatMessage
import com.igent.mobile.data.remote.ai.ChatRequest
import com.igent.mobile.data.remote.ai.ChatResponse
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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val openRouterApiService: OpenRouterApiService,
    private val groqApiService: GroqApiService,
    private val preferencesDataSource: PreferencesDataSource,
    private val okHttpClient: OkHttpClient,
    private val json: Json
) : AiRepository {

    private val openRouterModels = listOf(
        "anthropic/claude-3-haiku",
        "google/gemma-2-9b-it:free",
        "meta-llama/llama-3.2-3b-instruct:free",
        "deepseek/deepseek-r1:free",
        "qwen/qwen-2.5-coder-32b-instruct:free"
    )

    private val groqModels = listOf(
        "llama-3.3-70b-versatile",
        "llama-3.1-8b-instant",
        "gemma2-9b-it"
    )

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

            val history = try {
                messageDao.getAllMessages().first().takeLast(10).map { entity ->
                    ChatMessage(
                        role = if (entity.isFromUser) "user" else "assistant",
                        content = entity.content
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }

            val messagesList = if (history.isEmpty()) {
                listOf(ChatMessage(role = "user", content = prompt))
            } else {
                history
            }

            val rawKey = preferencesDataSource.apiKey.first()
            val apiKey = KeyObfuscator.deobfuscate(rawKey).trim()

            var aiResponseContent = ""
            var success = false

            // 1. Try OpenRouter with various keys and models (Primary then Free models)
            val openRouterKeys = mutableListOf<String>()
            if (apiKey.isNotBlank() && !apiKey.startsWith("gsk_")) openRouterKeys.add(apiKey)
            if (com.igent.mobile.BuildConfig.OPENROUTER_API_KEY_1.isNotBlank()) openRouterKeys.add(com.igent.mobile.BuildConfig.OPENROUTER_API_KEY_1)
            if (com.igent.mobile.BuildConfig.OPENROUTER_API_KEY_2.isNotBlank()) openRouterKeys.add(com.igent.mobile.BuildConfig.OPENROUTER_API_KEY_2)
            openRouterKeys.add("sk-or-v1-placeholder")

            outer@for (key in openRouterKeys.distinct()) {
                val authHeader = "Bearer $key"
                for (modelName in openRouterModels) {
                    try {
                        val request = ChatRequest(model = modelName, messages = messagesList, stream = false)
                        val response = openRouterApiService.createChatCompletion(authorization = authHeader, request = request)
                        val content = response.choices?.firstOrNull()?.message?.content
                        if (!content.isNullOrBlank()) {
                            aiResponseContent = content
                            success = true
                            break@outer
                        }
                    } catch (e: Exception) {
                        // Continue
                    }
                }
            }

            // 2. Try Groq with active models if OpenRouter failed
            if (!success) {
                val groqKeys = mutableListOf<String>()
                if (apiKey.startsWith("gsk_")) groqKeys.add(apiKey)
                if (com.igent.mobile.BuildConfig.OPENROUTER_API_KEY_1.startsWith("gsk_")) groqKeys.add(com.igent.mobile.BuildConfig.OPENROUTER_API_KEY_1)
                groqKeys.add("gsk_placeholder")

                outer@for (key in groqKeys.distinct()) {
                    val authHeader = "Bearer $key"
                    for (modelName in groqModels) {
                        try {
                            val request = ChatRequest(model = modelName, messages = messagesList, stream = false)
                            val response = groqApiService.createChatCompletion(authorization = authHeader, request = request)
                            val content = response.choices?.firstOrNull()?.message?.content
                            if (!content.isNullOrBlank()) {
                                aiResponseContent = content
                                success = true
                                break@outer
                            }
                        } catch (e: Exception) {
                            // Continue
                        }
                    }
                }
            }

            // 3. Fallback to Pollinations AI (Public Free API)
            if (!success) {
                val pollinationsContent = callPollinationsAi(messagesList)
                if (!pollinationsContent.isNullOrBlank()) {
                    aiResponseContent = pollinationsContent
                    success = true
                }
            }

            // 4. Ultimate intelligent local fallback if everything else fails (offline)
            if (!success || aiResponseContent.isBlank()) {
                aiResponseContent = getLocalIntelligentResponse(prompt)
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
        val apiKey = KeyObfuscator.deobfuscate(rawKey).trim()
        val messagesList = listOf(ChatMessage(role = "user", content = prompt))

        var streamed = false

        // 1. Try OpenRouter Streaming
        val openRouterKeys = mutableListOf<String>()
        if (apiKey.isNotBlank() && !apiKey.startsWith("gsk_")) openRouterKeys.add(apiKey)
        if (com.igent.mobile.BuildConfig.OPENROUTER_API_KEY_1.isNotBlank()) openRouterKeys.add(com.igent.mobile.BuildConfig.OPENROUTER_API_KEY_1)
        openRouterKeys.add("sk-or-v1-placeholder")

        outer@for (key in openRouterKeys.distinct()) {
            val authHeader = "Bearer $key"
            for (modelName in openRouterModels) {
                try {
                    val request = ChatRequest(model = modelName, messages = messagesList, stream = true)
                    val response = openRouterApiService.createChatCompletionStream(authorization = authHeader, request = request)
                    if (response.isSuccessful) {
                        response.body()?.byteStream()?.bufferedReader()?.use { reader ->
                            var line = reader.readLine()
                            while (line != null) {
                                if (line.startsWith("data: ")) {
                                    val jsonStr = line.removePrefix("data: ").trim()
                                    if (jsonStr == "[DONE]") break
                                    try {
                                        val chatResponse = json.decodeFromString<ChatResponse>(jsonStr)
                                        val content = chatResponse.choices?.firstOrNull()?.delta?.content
                                        if (!content.isNullOrEmpty()) {
                                            emit(content)
                                            streamed = true
                                        }
                                    } catch (e: Exception) {
                                        // Skip invalid chunks
                                    }
                                }
                                line = reader.readLine()
                            }
                        }
                        if (streamed) break@outer
                    }
                } catch (e: Exception) {
                    // Continue
                }
            }
        }

        // 2. Try Groq Streaming if OpenRouter failed
        if (!streamed) {
            val groqKeys = mutableListOf<String>()
            if (apiKey.startsWith("gsk_")) groqKeys.add(apiKey)
            if (com.igent.mobile.BuildConfig.OPENROUTER_API_KEY_1.startsWith("gsk_")) groqKeys.add(com.igent.mobile.BuildConfig.OPENROUTER_API_KEY_1)
            groqKeys.add("gsk_placeholder")

            outer@for (key in groqKeys.distinct()) {
                val authHeader = "Bearer $key"
                for (modelName in groqModels) {
                    try {
                        val request = ChatRequest(model = modelName, messages = messagesList, stream = true)
                        val response = groqApiService.createChatCompletionStream(authorization = authHeader, request = request)
                        if (response.isSuccessful) {
                            response.body()?.byteStream()?.bufferedReader()?.use { reader ->
                                var line = reader.readLine()
                                while (line != null) {
                                    if (line.startsWith("data: ")) {
                                        val jsonStr = line.removePrefix("data: ").trim()
                                        if (jsonStr == "[DONE]") break
                                        try {
                                            val chatResponse = json.decodeFromString<ChatResponse>(jsonStr)
                                            val content = chatResponse.choices?.firstOrNull()?.delta?.content
                                            if (!content.isNullOrEmpty()) {
                                                emit(content)
                                                streamed = true
                                            }
                                        } catch (e: Exception) {
                                            // Skip
                                        }
                                    }
                                    line = reader.readLine()
                                }
                            }
                            if (streamed) break@outer
                        }
                    } catch (e: Exception) {
                        // Continue
                    }
                }
            }
        }

        // 3. Fallback to Pollinations AI non-streaming chunk if streaming failed
        if (!streamed) {
            try {
                val content = callPollinationsAi(messagesList)
                if (!content.isNullOrBlank()) {
                    emit(content)
                    streamed = true
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    override suspend fun saveAiInteraction(prompt: String, response: String): Result<Message> = withContext(Dispatchers.IO) {
        try {
            val userMessage = Message(
                id = java.util.UUID.randomUUID().toString(),
                senderId = "user",
                content = prompt,
                timestamp = System.currentTimeMillis(),
                isFromUser = true
            )
            messageDao.insertMessage(MessageEntity.fromDomain(userMessage))

            val aiMessage = Message(
                id = java.util.UUID.randomUUID().toString(),
                senderId = "ai",
                content = response,
                timestamp = System.currentTimeMillis(),
                isFromUser = false
            )
            messageDao.insertMessage(MessageEntity.fromDomain(aiMessage))

            Result.success(aiMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun callPollinationsAi(messagesList: List<ChatMessage>): String? {
        return withContext(Dispatchers.IO) {
            try {
                val jsonString = buildString {
                    append("{\"messages\":[")
                    messagesList.forEachIndexed { index, msg ->
                        val escapedContent = msg.content.replace("\\", "\\\\")
                            .replace("\"", "\\\"")
                            .replace("\n", "\\n")
                            .replace("\r", "\\r")
                        append("{\"role\":\"${msg.role}\",\"content\":\"$escapedContent\"}")
                        if (index < messagesList.size - 1) append(",")
                    }
                    append("]}")
                }
                
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val request = Request.Builder()
                    .url("https://text.pollinations.ai/")
                    .post(jsonString.toRequestBody(mediaType))
                    .build()
                    
                val response = okHttpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) body.trim() else null
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun getLocalIntelligentResponse(prompt: String): String {
        val lowerPrompt = prompt.lowercase()
        return when {
            lowerPrompt.contains("market") || lowerPrompt.contains("psx") || lowerPrompt.contains("stock") -> 
                "I am your Igent AI assistant. Even without an active API connection, I can help you navigate the PSX market and manage your portfolio. How can I assist you with your investments today?"
            lowerPrompt.contains("hello") || lowerPrompt.contains("hi") -> 
                "Hello! I'm Igent AI. I'm currently in local mode but ready to help you with financial insights. What's on your mind?"
            else -> 
                "I've received your message. I'm currently operating with limited connectivity, but I'm here to assist you with financial intelligence and PSX market data. Could you please specify how I can help?"
        }
    }

    override fun getMessageHistory(): Flow<List<Message>> {
        return messageDao.getAllMessages().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
