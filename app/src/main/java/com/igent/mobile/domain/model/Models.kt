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

data class PortfolioHolding(
    val symbol: String,
    val name: String,
    val quantity: Double,
    val averagePrice: Double,
    val currentPrice: Double,
    val totalValue: Double,
    val unrealizedPnl: Double,
    val unrealizedPnlPct: Double
)

data class PortfolioTransaction(
    val id: String,
    val symbol: String,
    val type: String, // "BUY" or "SELL"
    val quantity: Double,
    val price: Double,
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
    val marketCap: Double?,
    val freeFloatPct: Double?,
    val eps: Double?,
    val pe: Double?,
    val w52High: Double?,
    val w52Low: Double?,
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
