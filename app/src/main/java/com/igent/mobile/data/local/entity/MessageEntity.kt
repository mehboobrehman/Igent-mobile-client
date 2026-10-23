package com.igent.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.igent.mobile.domain.model.Message

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val senderId: String,
    val content: String,
    val timestamp: Long,
    val isFromUser: Boolean
) {
    fun toDomain(): Message = Message(
        id = id,
        senderId = senderId,
        content = content,
        timestamp = timestamp,
        isFromUser = isFromUser
    )

    companion object {
        fun fromDomain(message: Message): MessageEntity = MessageEntity(
            id = message.id,
            senderId = message.senderId,
            content = message.content,
            timestamp = message.timestamp,
            isFromUser = message.isFromUser
        )
    }
}
