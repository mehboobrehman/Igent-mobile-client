package com.igent.mobile.domain.engine

data class EntryRef(
    val rate: Double? = null,
    val price: Double? = null,
    val nav: Double? = null
)

data class CardInput(
    val kind: String,
    val createdAt: String,
    val entryRef: EntryRef
)

data class CurrentValue(
    val value: Double
)

object CardScoring {
    const val DAY_MILLIS = 86400000.0

    fun scoreFromExcess(excess: Double): Int {
        return when {
            excess >= 5.0 -> 2
            excess >= 1.0 -> 1
            excess > -1.0 -> 0
            excess > -5.0 -> -1
            else -> -2
        }
    }

    fun cardReturn(card: CardInput, current: CurrentValue?, now: Long): Double? {
        val days = maxOf(0.0, (now - parseDate(card.createdAt)) / DAY_MILLIS)
        if (card.kind == "savings" || card.kind == "cash") {
            val rate = card.entryRef.rate ?: current?.value
            return rate?.let { it * days / 365.0 }
        }
        val entry = card.entryRef.price ?: card.entryRef.nav
        if (entry == null || current == null || entry <= 0.0) return null
        val change = (current.value - entry) / entry * 100.0
        return if (card.kind == "avoid") -change else change
    }

    private fun parseDate(dateStr: String): Long {
        return try {
            java.time.Instant.parse(dateStr).toEpochMilli()
        } catch (e: Exception) {
            try {
                java.time.LocalDate.parse(dateStr.take(10)).atStartOfDay(java.time.ZoneId.of("UTC")).toInstant().toEpochMilli()
            } catch (e2: Exception) {
                0L
            }
        }
    }
}
