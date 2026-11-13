package com.igent.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.igent.mobile.domain.model.Stock
import java.math.BigDecimal

@Entity(tableName = "stocks")
data class StockEntity(
    @PrimaryKey
    val symbol: String,
    val name: String,
    val price: BigDecimal,
    val change: BigDecimal,
    val percentChange: BigDecimal,
    val high: BigDecimal,
    val low: BigDecimal,
    val volume: Long
) {
    fun toDomain(): Stock = Stock(
        symbol = symbol,
        name = name,
        price = price,
        change = change,
        percentChange = percentChange,
        high = high,
        low = low,
        volume = volume
    )

    companion object {
        fun fromDomain(stock: Stock): StockEntity = StockEntity(
            symbol = stock.symbol,
            name = stock.name,
            price = stock.price,
            change = stock.change,
            percentChange = stock.percentChange,
            high = stock.high,
            low = stock.low,
            volume = stock.volume
        )
    }
}
