package com.igent.mobile.domain.service

import com.igent.mobile.data.remote.ai.ChatMessage

interface MarketDataContextBuilder {
    suspend fun buildMarketDataContext(): String
    suspend fun buildSystemMessageWithMarketData(): ChatMessage
    suspend fun injectMarketContext(messages: List<ChatMessage>): List<ChatMessage>
}
