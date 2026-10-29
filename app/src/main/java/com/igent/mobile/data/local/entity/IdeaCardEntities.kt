package com.igent.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "idea_cards")
data class IdeaCardEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val summary: String,
    val recommendation: String,
    val category: String, // e.g. "STOCK", "FUND", "MACRO", "PORTFOLIO"
    val targetSymbol: String,
    val dateString: String, // e.g. "2026-10-29"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "weekly_summaries")
data class WeeklySummaryEntity(
    @PrimaryKey
    val weekId: String, // e.g. "2026-W43"
    val weekTitle: String,
    val summaryContent: String,
    val topPicksJson: String, // comma-separated or JSON list of top symbols/insights
    val marketTrend: String, // "BULLISH", "BEARISH", "NEUTRAL"
    val timestamp: Long = System.currentTimeMillis()
)
