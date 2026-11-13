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
import java.math.BigDecimal
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

    private suspend fun ensureDefaultStocksSeeded() {
        if (stockDao.getDirectStocks().isEmpty()) {
            val defaultStocks = listOf(
                Stock("KSE100", "KSE 100 Index", BigDecimal("66450.00"), BigDecimal("450.50"), BigDecimal("0.68"), BigDecimal("66800.00"), BigDecimal("65900.00"), 0L),
                Stock("KSE30", "KSE 30 Index", BigDecimal("22150.00"), BigDecimal("120.30"), BigDecimal("0.55"), BigDecimal("22300.00"), BigDecimal("21950.00"), 0L),
                Stock("ALLSHR", "All Share Index", BigDecimal("44000.00"), BigDecimal("210.10"), BigDecimal("0.48"), BigDecimal("44250.00"), BigDecimal("43750.00"), 0L),
                Stock("LUCK", "Lucky Cement Limited", BigDecimal("750.50"), BigDecimal("12.50"), BigDecimal("1.69"), BigDecimal("760.00"), BigDecimal("735.00"), 1250000L),
                Stock("ENGRO", "Engro Corporation Limited", BigDecimal("310.20"), BigDecimal("4.10"), BigDecimal("1.34"), BigDecimal("315.00"), BigDecimal("305.00"), 850000L),
                Stock("HUBC", "The Hub Power Company", BigDecimal("118.50"), BigDecimal("-0.80"), BigDecimal("-0.67"), BigDecimal("120.00"), BigDecimal("117.50"), 2400000L),
                Stock("OGDC", "Oil & Gas Development Co.", BigDecimal("145.30"), BigDecimal("2.30"), BigDecimal("1.61"), BigDecimal("147.00"), BigDecimal("142.50"), 3100000L),
                Stock("MCB", "MCB Bank Limited", BigDecimal("210.00"), BigDecimal("3.50"), BigDecimal("1.69"), BigDecimal("212.00"), BigDecimal("205.00"), 950000L),
                Stock("FFC", "Fauji Fertilizer Company", BigDecimal("135.40"), BigDecimal("1.20"), BigDecimal("0.89"), BigDecimal("137.00"), BigDecimal("134.00"), 1100000L),
                Stock("MEBL", "Meezan Bank Limited", BigDecimal("185.00"), BigDecimal("4.00"), BigDecimal("2.21"), BigDecimal("186.50"), BigDecimal("180.00"), 1500000L),
                Stock("UBL", "United Bank Limited", BigDecimal("165.20"), BigDecimal("1.80"), BigDecimal("1.10"), BigDecimal("167.00"), BigDecimal("163.00"), 900000L),
                Stock("HBL", "Habib Bank Limited", BigDecimal("105.80"), BigDecimal("0.90"), BigDecimal("0.86"), BigDecimal("107.00"), BigDecimal("104.50"), 1200000L),
                Stock("PSO", "Pakistan State Oil Company", BigDecimal("175.60"), BigDecimal("-1.50"), BigDecimal("-0.85"), BigDecimal("178.00"), BigDecimal("174.00"), 1800000L),
                Stock("MARI", "Mari Energies Limited", BigDecimal("1250.00"), BigDecimal("25.00"), BigDecimal("2.04"), BigDecimal("1270.00"), BigDecimal("1220.00"), 150000L),
                Stock("SYS", "Systems Limited", BigDecimal("420.00"), BigDecimal("15.00"), BigDecimal("3.70"), BigDecimal("425.00"), BigDecimal("400.00"), 2200000L),
                Stock("ATRL", "Attock Refinery Limited", BigDecimal("380.00"), BigDecimal("8.00"), BigDecimal("2.15"), BigDecimal("385.00"), BigDecimal("370.00"), 1600000L),
                Stock("PPL", "Pakistan Petroleum Limited", BigDecimal("95.40"), BigDecimal("1.10"), BigDecimal("1.17"), BigDecimal("96.50"), BigDecimal("94.00"), 2800000L),
                Stock("EFERT", "Engro Fertilizers Limited", BigDecimal("92.50"), BigDecimal("0.70"), BigDecimal("0.76"), BigDecimal("93.50"), BigDecimal("91.50"), 1400000L)
            )
            stockDao.insertStocks(defaultStocks.map { StockEntity.fromDomain(it) })
        }
    }

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
        ensureDefaultStocksSeeded()
        return try {
            val remoteStocks = try {
                primaryApi.getStocks().map { dto ->
                    Stock(
                        symbol = dto.symbol,
                        name = dto.name ?: dto.companyName ?: dto.symbol,
                        price = BigDecimal.valueOf(if (dto.price != 0.0) dto.price else (dto.current ?: 0.0)),
                        change = BigDecimal.valueOf(dto.change),
                        percentChange = BigDecimal.valueOf(dto.percentChange ?: dto.changePercent ?: 0.0),
                        high = BigDecimal.valueOf(dto.high),
                        low = BigDecimal.valueOf(dto.low),
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
                            price = BigDecimal.valueOf(if (dto.price != 0.0) dto.price else (dto.current ?: 0.0)),
                            change = BigDecimal.valueOf(dto.change),
                            percentChange = BigDecimal.valueOf(dto.percentChange ?: dto.changePercent ?: 0.0),
                            high = BigDecimal.valueOf(dto.high),
                            low = BigDecimal.valueOf(dto.low),
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
                    price = BigDecimal.valueOf(if (indexDto.value != 0.0) indexDto.value else (indexDto.index ?: 0.0)),
                    change = BigDecimal.valueOf(indexDto.change),
                    percentChange = BigDecimal.valueOf(indexDto.percentChange),
                    high = BigDecimal.valueOf(indexDto.high),
                    low = BigDecimal.valueOf(indexDto.low),
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
        ensureDefaultStocksSeeded()
        return try {
            val remoteDto = try {
                primaryApi.getStockDetail(symbol)
            } catch (e: Exception) {
                withRateLimit { fallbackApi.getStockDetail(symbol) }
            }
            val stock = Stock(
                symbol = remoteDto.symbol,
                name = remoteDto.name ?: remoteDto.companyName ?: remoteDto.symbol,
                price = BigDecimal.valueOf(if (remoteDto.price != 0.0) remoteDto.price else (remoteDto.current ?: 0.0)),
                change = BigDecimal.valueOf(remoteDto.change),
                percentChange = BigDecimal.valueOf(remoteDto.percentChange ?: remoteDto.changePercent ?: 0.0),
                high = BigDecimal.valueOf(remoteDto.high),
                low = BigDecimal.valueOf(remoteDto.low),
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
