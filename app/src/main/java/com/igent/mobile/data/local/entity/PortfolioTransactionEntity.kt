package com.igent.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "portfolio_transactions")
data class PortfolioTransactionEntity(
    @PrimaryKey
    val id: String,
    val symbol: String,
    val type: String, // "BUY" or "SELL"
    val quantity: BigDecimal,
    val price: BigDecimal,
    val timestamp: Long
)
