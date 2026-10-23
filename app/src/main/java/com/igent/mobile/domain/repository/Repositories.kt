package com.igent.mobile.domain.repository

import com.igent.mobile.domain.model.Message
import com.igent.mobile.domain.model.Stock
import com.igent.mobile.domain.model.FeedItem
import com.igent.mobile.domain.model.UserPlaceholder
import kotlinx.coroutines.flow.Flow

interface AiRepository {
    suspend fun sendMessage(prompt: String): Result<Message>
    fun getMessageHistory(): Flow<List<Message>>
}

interface PsxRepository {
    suspend fun getStocks(): Result<List<Stock>>
    suspend fun getStockDetail(symbol: String): Result<Stock>
    fun getStockTickerFlow(): Flow<List<Stock>>
}

interface RssRepository {
    suspend fun getFeedItems(): Result<List<FeedItem>>
    suspend fun refreshFeed(): Result<Unit>
}
