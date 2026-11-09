package com.igent.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "portfolio_transactions")
data class PortfolioTransactionEntity(
    @PrimaryKey
    val id: String,
    val symbol: String,
    val type: String, // "BUY" or "SELL"
    val quantity: Double,
    val price: Double,
    val timestamp: Long
)
