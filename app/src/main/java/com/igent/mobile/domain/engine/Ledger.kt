package com.igent.mobile.domain.engine

data class TxRow(
    val id: String,
    val kind: String,
    val symbolOrName: String,
    val type: String, // "buy", "sell", "dividend"
    val qty: Double,
    val amount: Double,
    val fees: Double,
    val at: String,
    val lotId: String? = null
)

data class Position(
    val id: String?,
    val kind: String,
    val symbolOrName: String,
    var qty: Double,
    var cost: Double,
    val boughtAt: String
)

data class TxResult(
    val id: String,
    val kind: String,
    val symbolOrName: String,
    val type: String,
    val qty: Double,
    val amount: Double,
    val fees: Double,
    val at: String,
    val lotId: String?,
    val realized: Double?,
    val costBasis: Double?
)

data class LedgerResult(
    val positions: List<Position>,
    val tx: List<TxResult>
)

object Ledger {
    private const val EPS = 1e-9

    fun replay(rows: List<TxRow>): LedgerResult {
        val pos = mutableMapOf<String, Position>()
        val txOut = mutableListOf<TxResult>()

        for (r in rows) {
            val key = if (r.kind == "savings") {
                "savings:${if (r.type == "buy") r.id else r.lotId ?: ""}"
            } else {
                "${r.kind}:${r.symbolOrName}"
            }

            if (r.type == "buy") {
                val p2 = if (r.kind == "savings") null else pos[key]
                if (p2 != null) {
                    p2.qty += r.qty
                    p2.cost += r.amount + r.fees
                } else {
                    pos[key] = Position(
                        id = r.id,
                        kind = r.kind,
                        symbolOrName = r.symbolOrName,
                        qty = r.qty,
                        cost = r.amount + r.fees,
                        boughtAt = r.at
                    )
                }
                txOut.add(TxResult(r.id, r.kind, r.symbolOrName, r.type, r.qty, r.amount, r.fees, r.at, r.lotId, null, null))
                continue
            }

            if (r.type == "dividend") {
                txOut.add(TxResult(r.id, r.kind, r.symbolOrName, r.type, r.qty, r.amount, r.fees, r.at, r.lotId, null, null))
                continue
            }

            var p = pos[key]
            if (r.kind == "savings" && p == null) {
                p = pos.values.find { it.kind == "savings" && it.symbolOrName == r.symbolOrName }
            }

            if (p == null || p.qty <= EPS) {
                txOut.add(TxResult(r.id, r.kind, r.symbolOrName, r.type, r.qty, r.amount, r.fees, r.at, r.lotId, null, null))
                continue
            }

            val qtyToSell = minOf(r.qty, p.qty)
            val basis = p.cost * (qtyToSell / p.qty)
            val proceeds = r.amount * (qtyToSell / r.qty)
            p.qty -= qtyToSell
            p.cost -= basis

            txOut.add(TxResult(r.id, r.kind, r.symbolOrName, r.type, r.qty, r.amount, r.fees, r.at, r.lotId, proceeds - r.fees - basis, basis))

            if (p.qty <= EPS) {
                pos.entries.removeIf { it.value == p }
            }
        }

        return LedgerResult(positions = pos.values.toList(), tx = txOut)
    }
}
