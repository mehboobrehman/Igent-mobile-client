package com.igent.mobile.domain.engine

import kotlin.math.sqrt

data class FundInput(
    val symbol: String,
    val category: String?,
    val annualPct: Double?,
    val dailyVolPct: Double?
)

object FundRanker {
    fun fundScore(f: FundInput, group: String): Double {
        val ret = f.annualPct ?: -999.0
        if (group == "safe" || group == "income" || f.dailyVolPct == null) return ret
        return ret - 0.5 * f.dailyVolPct * sqrt(250.0)
    }

    fun fundGroup(category: String?): String {
        val cat = (category ?: "").lowercase()
        return when {
            cat.contains("money market") || cat.contains("cash") -> "safe"
            cat.contains("income") || cat.contains("government securities") -> "income"
            cat.contains("equity") -> "equity"
            cat.contains("balanced") || cat.contains("asset allocation") || cat.contains("mixed") -> "mixed"
            else -> "other"
        }
    }

    fun isShariahFund(category: String?): Boolean {
        val cat = (category ?: "").lowercase()
        return cat.contains("islamic") || cat.contains("shariah") || cat.contains("halal")
    }
}
