package com.igent.mobile.domain.engine

import kotlin.math.abs

data class WatchlistItem(
    val kind: String,
    val symbolOrName: String,
    val alertPct: Double
)

data class PriceRow(
    val subject: String,
    val date: String,
    val value: Double
)

data class RateRow(
    val subject: String,
    val date: String,
    val value: Double
)

data class ThresholdHit(
    val kind: String,
    val subject: String,
    val pct: Double,
    val date: String,
    val detail: String
)

object ThresholdEngine {
    fun computeThresholdHits(
        watchlist: List<WatchlistItem>,
        stockPrices: List<PriceRow>,
        fundNavs: List<PriceRow>,
        kse100Levels: List<PriceRow>,
        rates: List<RateRow>,
        kse100Limit: Double = 2.0,
        usdLimit: Double = 1.0,
        goldLimit: Double = 1.5
    ): List<ThresholdHit> {
        val hits = mutableListOf<ThresholdHit>()
        val stockLimits = watchlist.filter { it.kind == "stock" }.associate { it.symbolOrName.uppercase() to it.alertPct }
        val fundLimits = watchlist.filter { it.kind == "fund" }.associate { it.symbolOrName to it.alertPct }

        fun getPairs(rows: List<PriceRow>): List<PairWithPrev> {
            val grouped = rows.groupBy { it.subject }
            val res = mutableListOf<PairWithPrev>()
            for ((_, group) in grouped) {
                if (group.size >= 2) {
                    res.add(PairWithPrev(group[0].subject, group[0].date, group[0].value, group[1].value))
                }
            }
            return res
        }

        fun getRatePairs(rows: List<RateRow>): List<PairWithPrev> {
            val grouped = rows.groupBy { it.subject }
            val res = mutableListOf<PairWithPrev>()
            for ((_, group) in grouped) {
                if (group.size >= 2) {
                    res.add(PairWithPrev(group[0].subject, group[0].date, group[0].value, group[1].value))
                }
            }
            return res
        }

        fun pctChange(v: Double, p: Double): Double {
            if (p == 0.0) return 0.0
            return (v - p) / p * 100.0
        }

        if (stockLimits.isNotEmpty()) {
            for (p in getPairs(stockPrices)) {
                val sub = p.subject.uppercase()
                val limit = stockLimits[sub] ?: 5.0
                val change = pctChange(p.value, p.prev)
                if (abs(change) >= limit) {
                    val sign = if (change > 0) "+" else ""
                    hits.add(ThresholdHit("watchlist_stock", sub, change, p.date, "$sub closed at ${p.value} ($sign${String.format(java.util.Locale.US, "%.2f", change)}% vs ${p.prev})"))
                }
            }
        }

        if (fundLimits.isNotEmpty()) {
            for (p in getPairs(fundNavs)) {
                val limit = fundLimits[p.subject] ?: 5.0
                val change = pctChange(p.value, p.prev)
                if (abs(change) >= limit) {
                    val sign = if (change > 0) "+" else ""
                    hits.add(ThresholdHit("watchlist_fund", p.subject, change, p.date, "${p.subject} NAV ${p.value} ($sign${String.format(java.util.Locale.US, "%.2f", change)}%)"))
                }
            }
        }

        val ksePairs = getPairs(kse100Levels)
        if (ksePairs.isNotEmpty()) {
            val idx = ksePairs[0]
            val change = pctChange(idx.value, idx.prev)
            if (abs(change) >= kse100Limit) {
                val sign = if (change > 0) "+" else ""
                hits.add(ThresholdHit("kse100", "KSE100", change, idx.date, "KSE-100 at ${Math.round(idx.value)} ($sign${String.format(java.util.Locale.US, "%.2f", change)}%)"))
            }
        }

        val policyRows = rates.filter { it.subject == "policy_rate" }
        if (policyRows.isNotEmpty()) {
            val currentPolicy = policyRows[0]
            val prevDifferent = policyRows.find { it.value != currentPolicy.value }
            val lastSameIndex = policyRows.indexOfFirst { it.value != currentPolicy.value }
            if (prevDifferent != null && lastSameIndex in 0..2) {
                hits.add(ThresholdHit(
                    kind = "policy_rate",
                    subject = "policy_rate",
                    pct = currentPolicy.value - prevDifferent.value,
                    date = currentPolicy.date,
                    detail = "SBP policy rate changed from ${prevDifferent.value}% to ${currentPolicy.value}%"
                ))
            }
        }

        for (p in getRatePairs(rates.filter { it.subject != "policy_rate" })) {
            val change = pctChange(p.value, p.prev)
            if (p.subject == "usd_open_sell" && abs(change) >= usdLimit) {
                val sign = if (change > 0) "+" else ""
                hits.add(ThresholdHit("usd", "USD", change, p.date, "Open-market dollar Rs ${p.value} ($sign${String.format(java.util.Locale.US, "%.2f", change)}%)"))
            }
            if (p.subject == "gold_24k_tola" && abs(change) >= goldLimit) {
                val sign = if (change > 0) "+" else ""
                val formattedGold = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(p.value)
                hits.add(ThresholdHit("gold", "gold", change, p.date, "Gold 24K Rs $formattedGold/tola ($sign${String.format(java.util.Locale.US, "%.2f", change)}%)"))
            }
        }

        return hits
    }
}

data class PairWithPrev(
    val subject: String,
    val date: String,
    val value: Double,
    val prev: Double
)
