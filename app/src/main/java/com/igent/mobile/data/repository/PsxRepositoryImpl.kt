package com.igent.mobile.data.repository

import com.igent.mobile.data.local.dao.StockDao
import com.igent.mobile.data.local.entity.StockEntity
import com.igent.mobile.data.remote.psx.PsxApiService
import com.igent.mobile.data.remote.psx.PsxDataApiFallbackService
import com.igent.mobile.domain.model.Stock
import com.igent.mobile.domain.model.MarketDataState
import com.igent.mobile.domain.repository.PsxRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class PsxRepositoryImpl @Inject constructor(
    private val stockDao: StockDao,
    private val primaryApi: PsxApiService,
    private val fallbackApi: PsxDataApiFallbackService
) : PsxRepository {

    private val rateLimitMutex = Mutex()
    private var lastFallbackCallTime = 0L
    private val minFallbackIntervalMs = 1000L // 60 req/min -> 1 req/sec

    private suspend fun <T> withRateLimit(block: suspend () -> T): T {
        rateLimitMutex.withLock {
            val now = System.currentTimeMillis()
            val elapsed = now - lastFallbackCallTime
            if (elapsed < minFallbackIntervalMs) {
                kotlinx.coroutines.delay(minFallbackIntervalMs - elapsed)
            }
            lastFallbackCallTime = System.currentTimeMillis()
        }
        return block()
    }

    override suspend fun getStocksState(): MarketDataState<List<Stock>> {
        return try {
            val remoteStocks = try {
                primaryApi.getStocks().map { dto ->
                    Stock(
                        symbol = dto.symbol,
                        name = dto.name ?: dto.companyName ?: dto.symbol,
                        price = if (dto.price != 0.0) dto.price else (dto.current ?: 0.0),
                        change = dto.change,
                        percentChange = dto.percentChange ?: dto.changePercent ?: 0.0,
                        high = dto.high,
                        low = dto.low,
                        volume = dto.volume
                    )
                }
            } catch (e: Exception) {
                // Fallback with 60 req/min rate limiting
                withRateLimit {
                    fallbackApi.getStocks().map { dto ->
                        Stock(
                            symbol = dto.symbol,
                            name = dto.name ?: dto.companyName ?: dto.symbol,
                            price = if (dto.price != 0.0) dto.price else (dto.current ?: 0.0),
                            change = dto.change,
                            percentChange = dto.percentChange ?: dto.changePercent ?: 0.0,
                            high = dto.high,
                            low = dto.low,
                            volume = dto.volume
                        )
                    }
                }
            }

            // Also fetch KSE-100 index
            val allList = try {
                val indexDto = try {
                    primaryApi.getKse100Index()
                } catch (e: Exception) {
                    withRateLimit { fallbackApi.getKse100Index() }
                }
                val kseStock = Stock(
                    symbol = indexDto.symbol,
                    name = indexDto.name,
                    price = if (indexDto.value != 0.0) indexDto.value else (indexDto.index ?: 0.0),
                    change = indexDto.change,
                    percentChange = indexDto.percentChange,
                    high = indexDto.high,
                    low = indexDto.low,
                    volume = 0L
                )
                remoteStocks + kseStock
            } catch (e: Exception) {
                remoteStocks
            }

            stockDao.insertStocks(allList.map { StockEntity.fromDomain(it) })
            MarketDataState.Success(allList)
        } catch (e: Exception) {
            // Both primary and fallback APIs failed
            val cached = stockDao.getDirectStocks()
            if (cached.isNotEmpty()) {
                // If we have cached data, return it as Stale
                // Note: In a real app, we'd store the timestamp in DB
                MarketDataState.Stale(cached.map { it.toDomain() }, System.currentTimeMillis() - 3600000)
            } else {
                MarketDataState.Empty
            }
        }
    }

    override suspend fun getStocks(): Result<List<Stock>> {
        return when (val state = getStocksState()) {
            is MarketDataState.Success -> Result.success(state.data)
            is MarketDataState.Stale -> Result.success(state.data)
            is MarketDataState.Empty -> Result.success(emptyList())
            is MarketDataState.Error -> Result.failure(Exception(state.message))
        }
    }

    override suspend fun getStockDetail(symbol: String): Result<Stock> {
        return try {
            val remoteDto = try {
                primaryApi.getStockDetail(symbol)
            } catch (e: Exception) {
                withRateLimit { fallbackApi.getStockDetail(symbol) }
            }
            val stock = Stock(
                symbol = remoteDto.symbol,
                name = remoteDto.name ?: remoteDto.companyName ?: remoteDto.symbol,
                price = if (remoteDto.price != 0.0) remoteDto.price else (remoteDto.current ?: 0.0),
                change = remoteDto.change,
                percentChange = remoteDto.percentChange ?: remoteDto.changePercent ?: 0.0,
                high = remoteDto.high,
                low = remoteDto.low,
                volume = remoteDto.volume
            )
            stockDao.insertStock(StockEntity.fromDomain(stock))
            Result.success(stock)
        } catch (e: Exception) {
            // Fallback to local DB cache
            val entity = stockDao.getStockBySymbol(symbol)
            if (entity != null) {
                Result.success(entity.toDomain())
            } else {
                Result.failure(Exception("Unable to connect to PSX stock servers. Please check your network connection."))
            }
        }
    }

    override fun getStockTickerFlow(): Flow<List<Stock>> {
        return stockDao.getAllStocks().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
