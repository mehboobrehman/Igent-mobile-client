package com.igent.mobile.domain.model

import java.math.BigDecimal

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
    val price: BigDecimal,
    val change: BigDecimal,
    val percentChange: BigDecimal,
    val high: BigDecimal,
    val low: BigDecimal,
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

data class PortfolioHolding(
    val symbol: String,
    val name: String,
    val quantity: BigDecimal,
    val averagePrice: BigDecimal,
    val currentPrice: BigDecimal,
    val totalValue: BigDecimal,
    val unrealizedPnl: BigDecimal,
    val unrealizedPnlPct: BigDecimal
)

data class PortfolioTransaction(
    val id: String,
    val symbol: String,
    val type: String, // "BUY" or "SELL"
    val quantity: BigDecimal,
    val price: BigDecimal,
    val timestamp: Long
)

data class WatchlistItem(
    val symbol: String,
    val name: String,
    val addedAt: Long
)

data class CompanyInfo(
    val symbol: String,
    val name: String,
    val sector: String,
    val marketCap: BigDecimal?,
    val freeFloatPct: BigDecimal?,
    val eps: BigDecimal?,
    val pe: BigDecimal?,
    val w52High: BigDecimal?,
    val w52Low: BigDecimal?,
    val kse100: Boolean
)

data class CheckupResult(
    val symbol: String,
    val companyName: String,
    val score: Int,
    val verdict: String,
    val flags: List<Pair<String, String>>, // flag title to explanation
    val metrics: Map<String, String>
)

sealed class MarketDataState<out T> {
    object Empty : MarketDataState<Nothing>()
    data class Success<T>(val data: T) : MarketDataState<T>()
    data class Stale<T>(val data: T, val lastUpdated: Long) : MarketDataState<T>()
    data class Error(val message: String) : MarketDataState<Nothing>()
}
