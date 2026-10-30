package com.igent.mobile.data.service

import com.igent.mobile.data.local.dao.StockDao
import com.igent.mobile.data.remote.ai.ChatMessage
import com.igent.mobile.domain.repository.PsxRepository
import com.igent.mobile.domain.service.MarketDataContextBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketDataContextBuilderImpl @Inject constructor(
    private val stockDao: StockDao,
    private val psxRepository: PsxRepository
) : MarketDataContextBuilder {

    override suspend fun buildMarketDataContext(): String = withContext(Dispatchers.IO) {
        val cachedStocks = try {
            stockDao.getAllStocks().firstOrNull() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        val stocks = if (cachedStocks.isNotEmpty()) {
            cachedStocks.map { entity ->
                com.igent.mobile.domain.model.Stock(
                    symbol = entity.symbol,
                    name = entity.name,
                    price = entity.price,
                    change = entity.change,
                    percentChange = entity.percentChange,
                    high = entity.high,
                    low = entity.low,
                    volume = entity.volume
                )
            }
        } else {
            psxRepository.getStocks().getOrDefault(emptyList())
        }

        buildString {
            appendLine("=== LIVE PSX MARKET DATA CONTEXT ===")
            if (stocks.isNotEmpty()) {
                appendLine("Current tracked PSX stock quotes (${stocks.size} equities):")
                val topGainers = stocks.sortedByDescending { it.percentChange }.take(3)
                val topLosers = stocks.sortedBy { it.percentChange }.take(3)

                stocks.forEach { stock ->
                    val sign = if (stock.change >= 0) "+" else ""
                    appendLine("- ${stock.symbol} (${stock.name}): Price = ${stock.price} PKR, Change = $sign${stock.change} ($sign${stock.percentChange}%), Range = [${stock.low} - ${stock.high}], Vol = ${stock.volume}")
                }

                if (topGainers.isNotEmpty()) {
                    appendLine("\nTop Market Gainers: " + topGainers.joinToString { "${it.symbol} (+${it.percentChange}%)" })
                }
                if (topLosers.isNotEmpty()) {
                    appendLine("Top Market Decliners: " + topLosers.joinToString { "${it.symbol} (${it.percentChange}%)" })
                }
            } else {
                appendLine("No live stock quotes currently cached. Provide insights based on macro PSX indicators and general financial principles.")
            }
            appendLine("=====================================")
        }
    }

    override suspend fun buildSystemMessageWithMarketData(): ChatMessage {
        val contextText = buildMarketDataContext()
        val systemPrompt = """
            You are Igent, an expert financial advisor and Pakistan Stock Exchange (PSX) market intelligence assistant.
            You have direct access to real-time market data quotes.
            Always reference live prices, percentages, and market trends when providing stock commentary or investment recommendations.
            
            $contextText
        """.trimIndent()

        return ChatMessage(role = "system", content = systemPrompt)
    }

    override suspend fun injectMarketContext(messages: List<ChatMessage>): List<ChatMessage> {
        val systemMessage = buildSystemMessageWithMarketData()
        val nonSystemMessages = messages.filter { it.role != "system" }
        return listOf(systemMessage) + nonSystemMessages
    }
}
