package com.igent.mobile.data.remote.psx

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PsxApiService {
    @GET("api/stocks")
    suspend fun getStocks(): List<PsxStockDto>

    @GET("api/stocks/{symbol}")
    suspend fun getStockDetail(
        @Path("symbol") symbol: String
    ): PsxStockDto

    @GET("api/index/kse100")
    suspend fun getKse100Index(): PsxIndexDto

    @GET("api/stocks/{symbol}/history")
    suspend fun getStockHistory(
        @Path("symbol") symbol: String,
        @Query("period") period: String = "1M"
    ): List<PsxHistoryDto>
}

interface PsxDataApiFallbackService {
    @GET("v1/psx/stocks")
    suspend fun getStocks(): List<PsxStockDto>

    @GET("v1/psx/stocks/{symbol}")
    suspend fun getStockDetail(
        @Path("symbol") symbol: String
    ): PsxStockDto

    @GET("v1/psx/index/kse100")
    suspend fun getKse100Index(): PsxIndexDto

    @GET("v1/psx/stocks/{symbol}/history")
    suspend fun getStockHistory(
        @Path("symbol") symbol: String,
        @Query("period") period: String = "1M"
    ): List<PsxHistoryDto>
}
