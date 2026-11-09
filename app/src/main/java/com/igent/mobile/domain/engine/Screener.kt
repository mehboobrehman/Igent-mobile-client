package com.igent.mobile.domain.engine

data class ScreenOptions(
    val sort: String,
    val periodDays: Int? = 30,
    val sector: String? = null,
    val index: String? = null,
    val minAvgVolume: Double? = null,
    val limit: Int? = 15
)

object Screener {
    // Pure screening calculation helpers can go here
}
