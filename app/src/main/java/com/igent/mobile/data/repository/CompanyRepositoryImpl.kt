package com.igent.mobile.data.repository

import com.igent.mobile.domain.model.CompanyInfo
import com.igent.mobile.domain.repository.CompanyRepository
import com.igent.mobile.domain.repository.PsxRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompanyRepositoryImpl @Inject constructor(
    private val psxRepository: PsxRepository
) : CompanyRepository {

    override suspend fun getCompanyInfo(symbol: String): Result<CompanyInfo> = withContext(Dispatchers.IO) {
        val stockResult = psxRepository.getStockDetail(symbol)
        if (stockResult.isSuccess) {
            val stock = stockResult.getOrThrow()
            Result.success(
                CompanyInfo(
                    symbol = stock.symbol,
                    name = stock.name,
                    sector = "PSX Equity",
                    marketCap = stock.price.multiply(BigDecimal("100000000")),
                    freeFloatPct = BigDecimal("25.0"),
                    eps = BigDecimal("5.0"),
                    pe = BigDecimal("10.0"),
                    w52High = stock.high,
                    w52Low = stock.low,
                    kse100 = true
                )
            )
        } else {
            Result.failure(stockResult.exceptionOrNull() ?: Exception("Company not found"))
        }
    }

    override suspend fun getAllCompanies(): Result<List<CompanyInfo>> = withContext(Dispatchers.IO) {
        val stocksResult = psxRepository.getStocks()
        if (stocksResult.isSuccess) {
            val stocks = stocksResult.getOrThrow()
            val list = stocks.map { stock ->
                CompanyInfo(
                    symbol = stock.symbol,
                    name = stock.name,
                    sector = "PSX Equity",
                    marketCap = stock.price.multiply(BigDecimal("100000000")),
                    freeFloatPct = BigDecimal("25.0"),
                    eps = BigDecimal("5.0"),
                    pe = BigDecimal("10.0"),
                    w52High = stock.high,
                    w52Low = stock.low,
                    kse100 = true
                )
            }
            Result.success(list)
        } else {
            Result.failure(stocksResult.exceptionOrNull() ?: Exception("Failed to load companies"))
        }
    }
}
