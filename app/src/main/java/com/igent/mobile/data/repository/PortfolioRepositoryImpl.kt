package com.igent.mobile.data.repository

import com.igent.mobile.data.local.dao.PortfolioDao
import com.igent.mobile.data.local.entity.PortfolioTransactionEntity
import com.igent.mobile.domain.model.PortfolioHolding
import com.igent.mobile.domain.model.PortfolioTransaction
import com.igent.mobile.domain.repository.PortfolioRepository
import com.igent.mobile.domain.repository.PsxRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PortfolioRepositoryImpl @Inject constructor(
    private val portfolioDao: PortfolioDao,
    private val psxRepository: PsxRepository
) : PortfolioRepository {

    override fun getHoldings(): Flow<List<PortfolioHolding>> {
        return portfolioDao.getTransactions().map { entities ->
            // Group transactions by symbol to calculate holdings
            val map = mutableMapOf<String, Triple<Double, Double, Double>>() // symbol -> (totalQty, totalCost, lastPrice)
            val stocks = psxRepository.getStocks().getOrDefault(emptyList())
            val priceMap = stocks.associate { it.symbol to it.price }

            for (tx in entities) {
                val current = map[tx.symbol] ?: Triple(0.0, 0.0, priceMap[tx.symbol] ?: tx.price)
                if (tx.type.uppercase() == "BUY") {
                    val newQty = current.first + tx.quantity
                    val newCost = current.second + (tx.quantity * tx.price)
                    map[tx.symbol] = Triple(newQty, newCost, current.third)
                } else {
                    val newQty = maxOf(0.0, current.first - tx.quantity)
                    val avgCost = if (current.first > 0) current.second / current.first else 0.0
                    val newCost = newQty * avgCost
                    map[tx.symbol] = Triple(newQty, newCost, current.third)
                }
            }

            map.filter { it.value.first > 0.0 }.map { (symbol, triple) ->
                val qty = triple.first
                val totalCost = triple.second
                val avgPrice = if (qty > 0) totalCost / qty else 0.0
                val currPrice = priceMap[symbol] ?: avgPrice
                val totalVal = qty * currPrice
                val pnl = totalVal - totalCost
                val pnlPct = if (totalCost > 0) (pnl / totalCost) * 100.0 else 0.0
                val stockName = stocks.find { it.symbol == symbol }?.name ?: symbol

                PortfolioHolding(
                    symbol = symbol,
                    name = stockName,
                    quantity = qty,
                    averagePrice = avgPrice,
                    currentPrice = currPrice,
                    totalValue = totalVal,
                    unrealizedPnl = pnl,
                    unrealizedPnlPct = pnlPct
                )
            }
        }
    }

    override fun getTransactions(): Flow<List<PortfolioTransaction>> {
        return portfolioDao.getTransactions().map { list ->
            list.map {
                PortfolioTransaction(
                    id = it.id,
                    symbol = it.symbol,
                    type = it.type,
                    quantity = it.quantity,
                    price = it.price,
                    timestamp = it.timestamp
                )
            }
        }
    }

    override suspend fun addTransaction(symbol: String, type: String, quantity: Double, price: Double): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val entity = PortfolioTransactionEntity(
                id = UUID.randomUUID().toString(),
                symbol = symbol.uppercase(),
                type = type.uppercase(),
                quantity = quantity,
                price = price,
                timestamp = System.currentTimeMillis()
            )
            portfolioDao.insertTransaction(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetPortfolio(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            portfolioDao.clearTransactions()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
