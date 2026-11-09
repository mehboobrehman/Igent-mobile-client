package com.igent.mobile.domain.engine

import kotlin.math.min
import kotlin.math.pow

data class GoalInput(
    val targetAmount: Double,
    val targetDate: String,
    val inflationPct: Double,
    val returnPct: Double,
    val saved: Double,
    val monthly: Double
)

data class GoalPlanResult(
    val months: Int,
    val futureTarget: Double,
    val projected: Double,
    val neededMonthly: Double,
    val onTrack: Boolean,
    val progressPct: Double
)

object GoalPlanner {
    fun monthsBetween(today: String, date: String): Int {
        val parts1 = today.take(10).split("-").mapNotNull { it.toIntOrNull() }
        val parts2 = date.take(10).split("-").mapNotNull { it.toIntOrNull() }
        if (parts1.size < 3 || parts2.size < 3) return 0
        val (y1, m1, d1) = parts1
        val (y2, m2, d2) = parts2
        return maxOf(0, (y2 - y1) * 12 + (m2 - m1) - if (d2 < d1) 1 else 0)
    }

    fun goalPlan(g: GoalInput, today: String): GoalPlanResult {
        val n = monthsBetween(today, g.targetDate)
        val futureTarget = g.targetAmount * (1.0 + g.inflationPct / 100.0).pow(n.toDouble() / 12.0)
        val r = g.returnPct / 100.0 / 12.0
        val growth = (1.0 + r).pow(n.toDouble())
        val annuity = if (r == 0.0) n.toDouble() else (growth - 1.0) / r
        val fromSaved = g.saved * growth
        val projected = fromSaved + g.monthly * annuity
        val gap = futureTarget - fromSaved
        val neededMonthly = when {
            gap <= 0.0 -> 0.0
            n == 0 -> gap
            else -> gap / annuity
        }
        return GoalPlanResult(
            months = n,
            futureTarget = futureTarget,
            projected = projected,
            neededMonthly = neededMonthly,
            onTrack = projected >= futureTarget - 0.5,
            progressPct = if (futureTarget > 0.0) min(100.0, projected / futureTarget * 100.0) else 100.0
        )
    }
}
