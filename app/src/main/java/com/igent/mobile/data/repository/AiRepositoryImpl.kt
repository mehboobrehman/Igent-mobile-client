package com.igent.mobile.data.repository

import com.igent.mobile.data.local.dao.MessageDao
import com.igent.mobile.data.local.entity.MessageEntity
import com.igent.mobile.domain.model.Message
import com.igent.mobile.domain.repository.AiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao
) : AiRepository {

    override suspend fun sendMessage(prompt: String): Result<Message> {
        return try {
            val userMessage = Message(
                id = java.util.UUID.randomUUID().toString(),
                senderId = "user",
                content = prompt,
                timestamp = System.currentTimeMillis(),
                isFromUser = true
            )
            messageDao.insertMessage(MessageEntity.fromDomain(userMessage))

            // Simulate AI response
            val aiMessage = Message(
                id = java.util.UUID.randomUUID().toString(),
                senderId = "ai",
                content = "Echo from Igent AI: $prompt",
                timestamp = System.currentTimeMillis() + 100,
                isFromUser = false
            )
            messageDao.insertMessage(MessageEntity.fromDomain(aiMessage))

            Result.success(aiMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getMessageHistory(): Flow<List<Message>> {
        return messageDao.getAllMessages().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
