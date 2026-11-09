package com.igent.mobile.domain.repository

import com.igent.mobile.domain.model.*
import kotlinx.coroutines.flow.Flow

interface AiRepository {
    suspend fun sendMessage(prompt: String): Result<Message>
    fun sendMessageStream(prompt: String): Flow<String>
    fun getMessageHistory(): Flow<List<Message>>
    suspend fun saveAiInteraction(prompt: String, response: String): Result<Message>
}

interface PsxRepository {
    suspend fun getStocks(): Result<List<Stock>>
    suspend fun getStocksState(): MarketDataState<List<Stock>>
    suspend fun getStockDetail(symbol: String): Result<Stock>
    fun getStockTickerFlow(): Flow<List<Stock>>
}

interface RssRepository {
    suspend fun getFeedItems(): Result<List<FeedItem>>
    suspend fun refreshFeed(): Result<Unit>
}

interface PortfolioRepository {
    fun getHoldings(): Flow<List<PortfolioHolding>>
    fun getTransactions(): Flow<List<PortfolioTransaction>>
    suspend fun addTransaction(symbol: String, type: String, quantity: Double, price: Double): Result<Unit>
    suspend fun resetPortfolio(): Result<Unit>
}

interface WatchlistRepository {
    fun getWatchlist(): Flow<List<WatchlistItem>>
    suspend fun addToWatchlist(symbol: String, name: String): Result<Unit>
    suspend fun removeFromWatchlist(symbol: String): Result<Unit>
    suspend fun isWatchlisted(symbol: String): Boolean
}

interface CompanyRepository {
    suspend fun getCompanyInfo(symbol: String): Result<CompanyInfo>
    suspend fun getAllCompanies(): Result<List<CompanyInfo>>
}
