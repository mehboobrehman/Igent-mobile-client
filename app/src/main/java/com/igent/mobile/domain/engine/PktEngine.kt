package com.igent.mobile.domain.engine

import java.time.LocalDate
import java.time.ZonedDateTime

object PktEngine {
    fun taxYearStart(date: LocalDate): LocalDate {
        val year = if (date.monthValue >= 7) date.year else date.year - 1
        return LocalDate.of(year, 7, 1)
    }

    fun pktMinutes(d: ZonedDateTime): Int {
        val utcHour = d.withZoneSameInstant(java.time.ZoneId.of("UTC")).hour
        val utcMin = d.withZoneSameInstant(java.time.ZoneId.of("UTC")).minute
        val utcTotalMin = utcHour * 60 + utcMin
        return (utcTotalMin + 5 * 60) % (24 * 60)
    }

    fun inMarketHours(d: ZonedDateTime): Boolean {
        val dt = d.withZoneSameInstant(java.time.ZoneId.of("Asia/Karachi"))
        val day = dt.dayOfWeek.value // 1 = Monday, 7 = Sunday
        val minutes = dt.hour * 60 + dt.minute
        return day in 1..5 && minutes >= 9 * 60 + 15 && minutes <= 15 * 60 + 45
    }
}
