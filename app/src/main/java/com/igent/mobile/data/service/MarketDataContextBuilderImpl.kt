package com.igent.mobile.data.service

import com.igent.mobile.data.local.dao.StockDao
import com.igent.mobile.data.remote.ai.ChatMessage
import com.igent.mobile.domain.repository.PsxRepository
import com.igent.mobile.domain.service.MarketDataContextBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketDataContextBuilderImpl @Inject constructor(
    private val stockDao: StockDao,
    private val psxRepository: PsxRepository
) : MarketDataContextBuilder {

    private fun sanitizeAndDelimit(content: String, maxLength: Int = 500, label: String = "Untrusted Content"): String {
        val strippedContent = Jsoup.parse(content).text() // Strip HTML/XML markup
        val cappedContent = strippedContent.take(maxLength)
        return "<$label>\n$cappedContent\n</$label>"
    }

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

        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "PK"))
        currencyFormat.maximumFractionDigits = 2

        val percentFormat = NumberFormat.getPercentInstance(Locale.US)
        percentFormat.minimumFractionDigits = 2
        percentFormat.maximumFractionDigits = 2

        buildString {
            appendLine("<psx_market_data>")
            appendLine("=== LIVE PSX MARKET DATA CONTEXT ===")
            if (stocks.isNotEmpty()) {
                appendLine("Current tracked PSX stock quotes (${stocks.size} equities):")
                val topGainers = stocks.sortedByDescending { it.percentChange }.take(3)
                val topLosers = stocks.sortedBy { it.percentChange }.take(3)

                stocks.forEach { stock ->
                    val sign = if (stock.change >= BigDecimal.ZERO) "+" else ""
                    appendLine("- ${stock.symbol} (${stock.name}): Price = ${currencyFormat.format(stock.price)}, Change = $sign${currencyFormat.format(stock.change)} ($sign${percentFormat.format(stock.percentChange.divide(BigDecimal("100")))}), Range = [${currencyFormat.format(stock.low)} - ${currencyFormat.format(stock.high)}], Vol = ${stock.volume}")
                }

                if (topGainers.isNotEmpty()) {
                    val gainersStr = topGainers.joinToString(separator = ", ") { g ->
                        val pct = percentFormat.format(g.percentChange.divide(BigDecimal("100")))
                        "${g.symbol} (+$pct)"
                    }
                    appendLine("\nTop Market Gainers: $gainersStr")
                }
                if (topLosers.isNotEmpty()) {
                    val declinersStr = topLosers.joinToString(separator = ", ") { d ->
                        val pct = percentFormat.format(d.percentChange.divide(BigDecimal("100")))
                        "${d.symbol} ($pct)"
                    }
                    appendLine("Top Market Decliners: $declinersStr")
                }
            } else {
                appendLine("No live stock quotes currently cached. Provide insights based on macro PSX indicators and general financial principles.")
            }
            appendLine("=====================================")
            appendLine("</psx_market_data>")
        }
    }

    override suspend fun buildSystemMessageWithMarketData(): ChatMessage {
        val contextText = buildMarketDataContext()
        val sanitizedContext = sanitizeAndDelimit(contextText, maxLength = 2000, label = "Market Data")

        val promptBuilder = StringBuilder()
        promptBuilder.appendLine("You are Igent, an expert financial advisor and Pakistan Stock Exchange (PSX) market intelligence assistant.")
        promptBuilder.appendLine("You have direct access to real-time market data quotes.")
        promptBuilder.appendLine("Always reference live prices, percentages, and market trends when providing stock commentary or investment recommendations.")
        promptBuilder.appendLine() // for the blank line
        promptBuilder.append(sanitizedContext)

        val systemPrompt = promptBuilder.toString()

        return ChatMessage(role = "system", content = systemPrompt)
    }

    override suspend fun injectMarketContext(messages: List<ChatMessage>): List<ChatMessage> {
        val systemMessage = buildSystemMessageWithMarketData()
        val nonSystemMessages = messages.filter { it.role != "system" }
        return listOf(systemMessage) + nonSystemMessages
    }
}
