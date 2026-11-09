package com.igent.mobile.domain.engine

import kotlin.math.round

data class NetInput(
    val monthlyIncome: Double,
    val monthlyExpenses: Double,
    val bankSavings: Double,
    val goldTola: Double,
    val propertyValue: Double,
    val debt: Double,
    val age: Int,
    val retireAge: Int
)

data class SafetyReport(
    val months: Double?,
    val level: String,
    val surplus: Double?,
    val savingsRatePct: Double?,
    val yearsToRetire: Int,
    val parts: Map<String, Double>,
    val gross: Double,
    val net: Double,
    val sharePct: Map<String, Double>
)

object SafetyNet {
    const val SAFETY_LOW_MONTHS = 3.0
    const val SAFETY_STRONG_MONTHS = 6.0

    fun safetyReport(n: NetInput, cash: Double, holdingsValue: Double, goldPerTola: Double?): SafetyReport {
        val months = if (n.monthlyExpenses > 0.0) n.bankSavings / n.monthlyExpenses else null
        val level = when {
            months == null -> "unknown"
            months < SAFETY_LOW_MONTHS -> "low"
            months < SAFETY_STRONG_MONTHS -> "ok"
            else -> "strong"
        }
        val known = n.monthlyIncome > 0.0 && n.monthlyExpenses > 0.0
        val surplus = if (known) n.monthlyIncome - n.monthlyExpenses else null
        val parts = mapOf(
            "cash" to cash,
            "holdings" to holdingsValue,
            "bank" to n.bankSavings,
            "gold" to n.goldTola * (goldPerTola ?: 0.0),
            "property" to n.propertyValue
        )
        val gross = parts.values.sum()
        val pct: (Double) -> Double = { v ->
            if (gross > 0.0) round(v / gross * 1000.0) / 10.0 else 0.0
        }
        val yearsToRetire = maxOf(0, n.retireAge - n.age)

        return SafetyReport(
            months = months?.let { round(it * 10.0) / 10.0 },
            level = level,
            surplus = surplus,
            savingsRatePct = surplus?.let { if (n.monthlyIncome > 0.0) round(it / n.monthlyIncome * 1000.0) / 10.0 else 0.0 },
            yearsToRetire = yearsToRetire,
            parts = parts,
            gross = gross,
            net = gross - n.debt,
            sharePct = mapOf(
                "cash" to pct(parts["cash"]!!),
                "holdings" to pct(parts["holdings"]!!),
                "bank" to pct(parts["bank"]!!),
                "gold" to pct(parts["gold"]!!),
                "property" to pct(parts["property"]!!)
            )
        )
    }
}
