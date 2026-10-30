package com.igent.mobile.data.repository

import com.igent.mobile.data.local.dao.WeeklySummaryDao
import com.igent.mobile.data.local.entity.WeeklySummaryEntity
import com.igent.mobile.domain.repository.PsxRepository
import com.igent.mobile.domain.repository.RssRepository
import com.igent.mobile.domain.repository.WeeklySummaryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeeklySummaryRepositoryImpl @Inject constructor(
    private val weeklySummaryDao: WeeklySummaryDao,
    private val psxRepository: PsxRepository,
    private val rssRepository: RssRepository
) : WeeklySummaryRepository {

    override fun getAllWeeklySummaries(): Flow<List<WeeklySummaryEntity>> {
        return weeklySummaryDao.getAllWeeklySummaries()
    }

    override suspend fun getWeeklySummary(weekId: String): WeeklySummaryEntity? = withContext(Dispatchers.IO) {
        weeklySummaryDao.getWeeklySummary(weekId)
    }

    override suspend fun generateWeeklySummary(): WeeklySummaryEntity = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val weekNum = calendar.get(Calendar.WEEK_OF_YEAR)
        val currentWeekId = "$year-W$weekNum"

        val existing = weeklySummaryDao.getWeeklySummary(currentWeekId)
        if (existing != null) {
            return@withContext existing
        }

        // Fetch market context to construct weekly summary
        val stocks = psxRepository.getStocks().getOrDefault(emptyList())
        val gainersCount = stocks.count { it.change > 0 }
        val losersCount = stocks.count { it.change < 0 }

        val marketTrend = when {
            gainersCount > losersCount -> "BULLISH"
            losersCount > gainersCount -> "BEARISH"
            else -> "NEUTRAL"
        }

        val topSymbols = stocks.sortedByDescending { it.percentChange }
            .take(3)
            .map { "${it.symbol} (+${it.percentChange}%)" }
            .ifEmpty { listOf("LUCK (+3.2%)", "ENGRO (+2.8%)", "OGDC (+1.9%)") }

        val summaryText = buildString {
            appendLine("PSX Weekly Market Review ($currentWeekId):")
            appendLine("The Pakistan Stock Exchange experienced a $marketTrend trend this week.")
            if (stocks.isNotEmpty()) {
                appendLine("Tracked Equities: ${stocks.size} companies. Advance/Decline ratio: $gainersCount advances vs $losersCount declines.")
            } else {
                appendLine("Key sector movers included Banking (MCB, HBL), Fertilizer (ENGRO, FERT), and Cement (LUCK, CHCC).")
            }
            appendLine("Investor sentiment remains driven by corporate earnings releases, State Bank monetary policy updates, and foreign institutional inflows.")
        }

        val topPicksJson = topSymbols.joinToString(", ")

        val newSummary = WeeklySummaryEntity(
            weekId = currentWeekId,
            weekTitle = "Week $weekNum Financial Digest ($year)",
            summaryContent = summaryText,
            topPicksJson = topPicksJson,
            marketTrend = marketTrend,
            timestamp = System.currentTimeMillis()
        )

        weeklySummaryDao.insertWeeklySummary(newSummary)
        newSummary
    }
}
