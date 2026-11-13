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
import java.math.BigDecimal
import java.math.RoundingMode
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
            val map = mutableMapOf<String, Triple<BigDecimal, BigDecimal, BigDecimal>>() // symbol -> (totalQty, totalCost, lastPrice)
            val stocks = psxRepository.getStocks().getOrDefault(emptyList())
            val priceMap = stocks.associate { it.symbol to it.price }

            for (tx in entities) {
                val currentPrice = priceMap[tx.symbol] ?: tx.price
                val current = map[tx.symbol] ?: Triple(BigDecimal.ZERO, BigDecimal.ZERO, currentPrice)
                if (tx.type.uppercase() == "BUY") {
                    val newQty = current.first.add(tx.quantity)
                    val newCost = current.second.add(tx.quantity.multiply(tx.price))
                    map[tx.symbol] = Triple(newQty, newCost, current.third)
                } else {
                    val newQty = current.first.subtract(tx.quantity).max(BigDecimal.ZERO)
                    val avgCost = if (current.first > BigDecimal.ZERO) current.second.divide(current.first, 4, RoundingMode.HALF_UP) else BigDecimal.ZERO
                    val newCost = newQty.multiply(avgCost)
                    map[tx.symbol] = Triple(newQty, newCost, current.third)
                }
            }

            map.filter { it.value.first > BigDecimal.ZERO }.map { (symbol, triple) ->
                val qty = triple.first
                val totalCost = triple.second
                val avgPrice = if (qty > BigDecimal.ZERO) totalCost.divide(qty, 4, RoundingMode.HALF_UP) else BigDecimal.ZERO
                val currPrice = priceMap[symbol] ?: avgPrice
                val totalVal = qty.multiply(currPrice)
                val pnl = totalVal.subtract(totalCost)
                val pnlPct = if (totalCost > BigDecimal.ZERO) pnl.divide(totalCost, 4, RoundingMode.HALF_UP).multiply(BigDecimal("100")) else BigDecimal.ZERO
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
                quantity = BigDecimal.valueOf(quantity),
                price = BigDecimal.valueOf(price),
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
