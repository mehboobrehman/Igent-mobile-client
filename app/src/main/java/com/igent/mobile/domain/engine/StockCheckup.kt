package com.igent.mobile.domain.engine

import kotlin.math.pow

data class StockFact(
    val symbol: String,
    val marketCap: Double?,
    val freeFloatPct: Double?,
    val change1mPct: Double?,
    val close: Double?,
    val w52High: Double?,
    val w52Low: Double?,
    val lastVolume: Double?,
    val avgVolume60: Double?,
    val avgVolume20: Double?,
    val sessions: Int,
    val eps: Double?,
    val pe: Double?,
    val kse100: Boolean,
    val sector: String?,
    val sectorKey: String?
)

data class StockFlag(
    val flag: String,
    val why: String
)

data class PriceLevelRow(
    val date: String,
    val close: Double,
    val level: Double
)

object StockCheckup {
    val SEV_COST = mapOf("high" to 20, "medium" to 10, "low" to 4, "good" to 0)

    val SECTORS = listOf(
        Regex("bank", RegexOption.IGNORE_CASE) to "banks",
        Regex("exploration|e&p", RegexOption.IGNORE_CASE) to "oil-gas-exploration",
        Regex("oil.*market|omc", RegexOption.IGNORE_CASE) to "oil-marketing",
        Regex("refiner", RegexOption.IGNORE_CASE) to "refinery",
        Regex("cement", RegexOption.IGNORE_CASE) to "cement",
        Regex("fertili[sz]er", RegexOption.IGNORE_CASE) to "fertilizer",
        Regex("power|generation", RegexOption.IGNORE_CASE) to "power",
        Regex("tech|software|communication", RegexOption.IGNORE_CASE) to "technology",
        Regex("steel|engineering", RegexOption.IGNORE_CASE) to "steel",
        Regex("auto|car|tractor|transport", RegexOption.IGNORE_CASE) to "autos",
        Regex("pharma|medicine", RegexOption.IGNORE_CASE) to "pharmaceuticals",
        Regex("food|beverage|sugar", RegexOption.IGNORE_CASE) to "food",
        Regex("textile|spinning|weaving", RegexOption.IGNORE_CASE) to "textiles",
        Regex("sugar", RegexOption.IGNORE_CASE) to "sugar",
        Regex("reit", RegexOption.IGNORE_CASE) to "reit",
        Regex("telecom", RegexOption.IGNORE_CASE) to "telecom"
    )

    fun sectorKey(sector: String?): String {
        if (sector.isNullOrBlank()) return "unknown"
        for ((re, key) in SECTORS) {
            if (re.containsMatchIn(sector)) return key
        }
        return sector.lowercase()
            .replace(Regex("companies|company"), "")
            .replace(Regex("[^a-z0-9]+"), "-")
            .replace(Regex("^-|-$"), "")
            .ifEmpty { "unknown" }
    }

    fun stockFlags(f: StockFact, target: Double?): List<StockFlag> {
        val out = mutableListOf<StockFlag>()
        if (f.marketCap != null && f.marketCap < 5e9) {
            out.add(StockFlag("Small company (market cap about Rs ${(f.marketCap / 1e9).toFixed(1)} bn)", "Small companies need little money to push the price up, which is why pump groups like them."))
        }
        if (f.freeFloatPct != null && f.freeFloatPct < 15.0) {
            out.add(StockFlag("Few shares trade freely (free float ${f.freeFloatPct.roundToInt()}%)", "With few shares available, a small group can move the price a lot, both up and down."))
        }
        if (f.change1mPct != null && f.change1mPct > 30.0) {
            out.add(StockFlag("Already up ${f.change1mPct.roundToInt()}% in a month", "Tips often arrive after the rise, when early buyers want someone to sell to."))
        }
        if (f.close != null && f.w52High != null && f.w52Low != null && f.w52High > f.w52Low) {
            if (f.close >= f.w52Low + 0.95 * (f.w52High - f.w52Low)) {
                out.add(StockFlag("Price is near its 52-week high", "Buying at the top of a sharp run is how tip followers usually lose."))
            }
        }
        val baseVol = f.avgVolume60 ?: f.avgVolume20
        if (f.lastVolume != null && baseVol != null && f.sessions >= 10 && f.lastVolume > 3.0 * baseVol) {
            out.add(StockFlag("Volume spike (${(f.lastVolume / baseVol).toFixed(1)}× normal)", "A sudden jump in trading with no news is a classic sign of a pump."))
        }
        if (f.eps != null && f.eps < 0.0) {
            out.add(StockFlag("The company made a loss last year", "A rising price with losses has to be explained by news. If there is none, be very careful."))
        }
        if (f.pe != null && f.pe > 40.0) {
            out.add(StockFlag("Very expensive (P/E ${f.pe.roundToInt()})", "Investors are paying many years of profit; that needs a strong, checkable story."))
        }
        if (!f.kse100) {
            out.add(StockFlag("Not in the KSE-100", "Outside the biggest 100 companies, there is less research and trading, so hype moves prices more."))
        }
        if (target != null && f.close != null && target > f.close * 1.3) {
            val pctAbove = ((target - f.close) / f.close * 100.0).roundToInt()
            out.add(StockFlag("Big target (Rs $target, $pctAbove% above the price)", "A large target with no valuation behind it is a hook. Ask how it was worked out."))
        }
        return out
    }

    fun betaFromHistory(rows: List<PriceLevelRow>): Double? {
        if (rows.size < 40) return null
        val returns = rows.zipWithNext().map { (prev, curr) ->
            Pair(curr.close / prev.close - 1.0, curr.level / prev.level - 1.0)
        }
        val mx = returns.map { it.second }.average()
        val ms = returns.map { it.first }.average()
        var cov = 0.0
        var varx = 0.0
        for ((s, x) in returns) {
            cov += (s - ms) * (x - mx)
            varx += (x - mx).pow(2)
        }
        return if (varx != 0.0) minOf(2.5, maxOf(0.2, cov / varx)) else null
    }
}

private fun Double.toFixed(digits: Int): String = String.format(java.util.Locale.US, "%.${digits}f", this)
private fun Double.roundToInt(): Int = Math.round(this).toInt()
