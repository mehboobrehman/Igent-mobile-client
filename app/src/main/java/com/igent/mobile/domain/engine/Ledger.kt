package com.igent.mobile.domain.engine

import java.math.BigDecimal
import java.math.RoundingMode

data class TxRow(
    val id: String,
    val kind: String,
    val symbolOrName: String,
    val type: String, // "buy", "sell", "dividend"
    val qty: BigDecimal,
    val amount: BigDecimal,
    val fees: BigDecimal,
    val at: String,
    val lotId: String? = null
)

data class Position(
    val id: String?,
    val kind: String,
    val symbolOrName: String,
    var qty: BigDecimal,
    var cost: BigDecimal,
    val boughtAt: String
)

data class TxResult(
    val id: String,
    val kind: String,
    val symbolOrName: String,
    val type: String,
    val qty: BigDecimal,
    val amount: BigDecimal,
    val fees: BigDecimal,
    val at: String,
    val lotId: String?,
    val realized: BigDecimal?,
    val costBasis: BigDecimal?
)

data class LedgerResult(
    val positions: List<Position>,
    val tx: List<TxResult>
)

object Ledger {
    private val ZERO = BigDecimal.ZERO
    private val EPS = BigDecimal("1e-9")

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
                    p2.qty = p2.qty.add(r.qty)
                    p2.cost = p2.cost.add(r.amount).add(r.fees)
                } else {
                    pos[key] = Position(
                        id = r.id,
                        kind = r.kind,
                        symbolOrName = r.symbolOrName,
                        qty = r.qty,
                        cost = r.amount.add(r.fees),
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

            if (p == null || p.qty.compareTo(EPS) <= 0) {
                txOut.add(TxResult(r.id, r.kind, r.symbolOrName, r.type, r.qty, r.amount, r.fees, r.at, r.lotId, null, null))
                continue
            }

            val qtyToSell = if (r.qty < p.qty) r.qty else p.qty
            
            // basis = p.cost * (qtyToSell / p.qty)
            val basis = p.cost.multiply(qtyToSell.divide(p.qty, 18, RoundingMode.HALF_UP))
            
            // proceeds = r.amount * (qtyToSell / r.qty)
            val proceeds = r.amount.multiply(qtyToSell.divide(r.qty, 18, RoundingMode.HALF_UP))
            
            p.qty = p.qty.subtract(qtyToSell)
            p.cost = p.cost.subtract(basis)

            txOut.add(TxResult(
                r.id, r.kind, r.symbolOrName, r.type, r.qty, r.amount, r.fees, r.at, r.lotId, 
                proceeds.subtract(r.fees).subtract(basis), 
                basis
            ))

            if (p.qty.compareTo(EPS) <= 0) {
                pos.entries.removeIf { it.value == p }
            }
        }

        return LedgerResult(positions = pos.values.toList(), tx = txOut)
    }
}
