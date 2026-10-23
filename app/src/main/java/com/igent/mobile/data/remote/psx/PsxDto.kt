package com.igent.mobile.data.remote.psx

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PsxStockDto(
    @SerialName("symbol") val symbol: String,
    @SerialName("name") val name: String? = null,
    @SerialName("company_name") val companyName: String? = null,
    @SerialName("price") val price: Double = 0.0,
    @SerialName("current") val current: Double? = null,
    @SerialName("change") val change: Double = 0.0,
    @SerialName("percent_change") val percentChange: Double? = null,
    @SerialName("change_percent") val changePercent: Double? = null,
    @SerialName("high") val high: Double = 0.0,
    @SerialName("low") val low: Double = 0.0,
    @SerialName("volume") val volume: Long = 0L,
    @SerialName("market_cap") val marketCap: Double? = null
)

@Serializable
data class PsxIndexDto(
    @SerialName("symbol") val symbol: String = "KSE-100",
    @SerialName("name") val name: String = "KSE-100 Index",
    @SerialName("value") val value: Double = 0.0,
    @SerialName("index") val index: Double? = null,
    @SerialName("change") val change: Double = 0.0,
    @SerialName("percent_change") val percentChange: Double = 0.0,
    @SerialName("high") val high: Double = 0.0,
    @SerialName("low") val low: Double = 0.0
)

@Serializable
data class PsxHistoryDto(
    @SerialName("date") val date: String,
    @SerialName("open") val open: Double,
    @SerialName("high") val high: Double,
    @SerialName("low") val low: Double,
    @SerialName("close") val close: Double,
    @SerialName("volume") val volume: Long
)
