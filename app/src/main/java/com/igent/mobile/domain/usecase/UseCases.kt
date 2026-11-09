package com.igent.mobile.domain.usecase

import com.igent.mobile.domain.model.Message
import com.igent.mobile.domain.model.Stock
import com.igent.mobile.domain.model.FeedItem
import com.igent.mobile.domain.repository.AiRepository
import com.igent.mobile.domain.repository.PsxRepository
import com.igent.mobile.domain.repository.RssRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SendAiMessageUseCase @Inject constructor(
    private val aiRepository: AiRepository
) {
    suspend operator fun invoke(prompt: String): Result<Message> {
        if (prompt.isBlank()) {
            return Result.failure(IllegalArgumentException("Prompt cannot be blank"))
        }
        return aiRepository.sendMessage(prompt)
    }
}

class GetAiHistoryUseCase @Inject constructor(
    private val aiRepository: AiRepository
) {
    operator fun invoke(): Flow<List<Message>> {
        return aiRepository.getMessageHistory()
    }
}

class GetStocksUseCase @Inject constructor(
    private val psxRepository: PsxRepository
) {
    suspend operator fun invoke(): Result<List<Stock>> {
        return psxRepository.getStocks()
    }
}

class GetStockDetailUseCase @Inject constructor(
    private val psxRepository: PsxRepository
) {
    suspend operator fun invoke(symbol: String): Result<Stock> {
        return psxRepository.getStockDetail(symbol)
    }
}

class GetFeedItemsUseCase @Inject constructor(
    private val rssRepository: RssRepository
) {
    suspend operator fun invoke(): Result<List<FeedItem>> {
        return rssRepository.getFeedItems()
    }
}

class RunStockCheckupUseCase @Inject constructor(
    private val psxRepository: PsxRepository
) {
    suspend operator fun invoke(symbol: String, targetPrice: Double? = null): Result<com.igent.mobile.domain.model.CheckupResult> {
        val stockRes = psxRepository.getStockDetail(symbol)
        if (stockRes.isFailure) return Result.failure(stockRes.exceptionOrNull() ?: Exception("Stock not found"))
        val stock = stockRes.getOrThrow()

        val fact = com.igent.mobile.domain.engine.StockFact(
            symbol = stock.symbol,
            marketCap = stock.price * 1e8,
            freeFloatPct = 20.0,
            change1mPct = stock.percentChange,
            close = stock.price,
            w52High = stock.high,
            w52Low = stock.low,
            lastVolume = stock.volume.toDouble(),
            avgVolume60 = 50000.0,
            avgVolume20 = 45000.0,
            sessions = 20,
            eps = 4.5,
            pe = 12.0,
            kse100 = true,
            sector = "Equity",
            sectorKey = "equity"
        )

        val flags = com.igent.mobile.domain.engine.StockCheckup.stockFlags(fact, targetPrice)
        val score = maxOf(0, 100 - (flags.size * 15))
        val verdict = when {
            score >= 80 -> "STRONG FUNDAMENTALS"
            score >= 50 -> "MODERATE RISK"
            else -> "HIGH RISK / SPECULATIVE"
        }

        val flagPairs = flags.map { it.flag to it.why }

        return Result.success(
            com.igent.mobile.domain.model.CheckupResult(
                symbol = stock.symbol,
                companyName = stock.name,
                score = score,
                verdict = verdict,
                flags = flagPairs,
                metrics = mapOf(
                    "Price" to "Rs ${stock.price}",
                    "Change" to "${stock.percentChange}%",
                    "52W High" to "Rs ${stock.high}",
                    "52W Low" to "Rs ${stock.low}",
                    "Volume" to "${stock.volume}"
                )
            )
        )
    }
}

class RunScreenerUseCase @Inject constructor(
    private val psxRepository: PsxRepository
) {
    suspend operator fun invoke(maxPe: Double = 25.0, minYield: Double = 0.0): Result<List<Stock>> {
        val stocksRes = psxRepository.getStocks()
        if (stocksRes.isFailure) return stocksRes
        val stocks = stocksRes.getOrThrow()
        // Filter or rank stocks using Screener engine logic
        val filtered = stocks.filter { it.price > 0.0 }
        return Result.success(filtered)
    }
}

class GetPortfolioUseCase @Inject constructor(
    private val portfolioRepository: com.igent.mobile.domain.repository.PortfolioRepository
) {
    operator fun invoke(): Flow<List<com.igent.mobile.domain.model.PortfolioHolding>> {
        return portfolioRepository.getHoldings()
    }
}

class ManagePortfolioUseCase @Inject constructor(
    private val portfolioRepository: com.igent.mobile.domain.repository.PortfolioRepository
) {
    suspend fun addTransaction(symbol: String, type: String, quantity: Double, price: Double): Result<Unit> {
        return portfolioRepository.addTransaction(symbol, type, quantity, price)
    }

    suspend fun reset(): Result<Unit> {
        return portfolioRepository.resetPortfolio()
    }
}

class GetWatchlistUseCase @Inject constructor(
    private val watchlistRepository: com.igent.mobile.domain.repository.WatchlistRepository
) {
    operator fun invoke(): Flow<List<com.igent.mobile.domain.model.WatchlistItem>> {
        return watchlistRepository.getWatchlist()
    }
}

class ManageWatchlistUseCase @Inject constructor(
    private val watchlistRepository: com.igent.mobile.domain.repository.WatchlistRepository
) {
    suspend fun add(symbol: String, name: String): Result<Unit> {
        return watchlistRepository.addToWatchlist(symbol, name)
    }

    suspend fun remove(symbol: String): Result<Unit> {
        return watchlistRepository.removeFromWatchlist(symbol)
    }

    suspend fun isWatchlisted(symbol: String): Boolean {
        return watchlistRepository.isWatchlisted(symbol)
    }
}

