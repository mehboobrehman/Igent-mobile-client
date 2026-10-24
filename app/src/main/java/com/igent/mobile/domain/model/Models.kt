package com.igent.mobile.domain.model

data class Message(
    val id: String,
    val senderId: String,
    val content: String,
    val timestamp: Long,
    val isFromUser: Boolean
)

data class Stock(
    val symbol: String,
    val name: String,
    val price: Double,
    val change: Double,
    val percentChange: Double,
    val high: Double,
    val low: Double,
    val volume: Long
)

data class FeedItem(
    val id: String,
    val title: String,
    val description: String,
    val link: String,
    val pubDate: String,
    val source: String,
    val imageUrl: String? = null
)

data class UserPlaceholder(
    val id: String,
    val username: String,
    val email: String,
    val avatarUrl: String?
)
