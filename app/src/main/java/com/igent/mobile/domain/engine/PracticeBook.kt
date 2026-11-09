package com.igent.mobile.domain.engine

data class PracticeTradeInput(
    val kind: String,
    val symbol: String,
    val name: String?,
    val side: String, // "buy" or "sell"
    val qty: Double,
    val price: Double,
    val fees: Double,
    val at: String
)

data class PracticePosition(
    val kind: String,
    val symbol: String,
    var name: String?,
    var qty: Double,
    var cost: Double
)

object PracticeBook {
    const val PRACTICE_START = 1000000.0
    private const val EPS = 1e-9

    fun book(trades: List<PracticeTradeInput>): Pair<Double, List<PracticePosition>> {
        var cash = PRACTICE_START
        val held = mutableMapOf<String, PracticePosition>()

        for (t in trades) {
            val key = "${t.kind}:${t.symbol}"
            val h = held.getOrPut(key) { PracticePosition(t.kind, t.symbol, t.name, 0.0, 0.0) }
            if (t.side == "buy") {
                cash -= t.qty * t.price + t.fees
                h.cost += t.qty * t.price + t.fees
                h.qty += t.qty
            } else {
                cash += t.qty * t.price - t.fees
                val avg = if (h.qty > 0.0) h.cost / h.qty else 0.0
                h.qty -= t.qty
                h.cost -= avg * t.qty
            }
            if (h.name == null && t.name != null) h.name = t.name
            if (h.qty > EPS) {
                held[key] = h
            } else {
                held.remove(key)
            }
        }
        return Pair(cash, held.values.toList())
    }
}
