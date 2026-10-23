package com.igent.mobile.data.repository

import com.igent.mobile.data.local.dao.StockDao
import com.igent.mobile.data.local.entity.StockEntity
import com.igent.mobile.domain.model.Stock
import com.igent.mobile.domain.repository.PsxRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PsxRepositoryImpl @Inject constructor(
    private val stockDao: StockDao
) : PsxRepository {

    override suspend fun getStocks(): Result<List<Stock>> {
        return try {
            // Return mock / default data if empty or fetch
            val sampleStocks = listOf(
                Stock("OGDC", "Oil & Gas Development Company", 120.50, 2.10, 1.77, 122.0, 119.0, 1500000),
                Stock("ENGRO", "Engro Corporation", 310.00, -1.50, -0.48, 313.0, 308.0, 800000),
                Stock("LUCK", "Lucky Cement", 750.25, 10.25, 1.38, 755.0, 740.0, 450000),
                Stock("HUBC", "Hub Power Company", 115.80, 0.80, 0.70, 116.5, 114.5, 2200000)
            )
            stockDao.insertStocks(sampleStocks.map { StockEntity.fromDomain(it) })
            Result.success(sampleStocks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getStockDetail(symbol: String): Result<Stock> {
        return try {
            val entity = stockDao.getStockBySymbol(symbol)
            if (entity != null) {
                Result.success(entity.toDomain())
            } else {
                Result.failure(Exception("Stock not found: $symbol"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getStockTickerFlow(): Flow<List<Stock>> {
        return stockDao.getAllStocks().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
