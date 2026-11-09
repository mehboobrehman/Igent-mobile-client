package com.igent.mobile.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.IsoFields
import java.util.Locale

object Pkt {
    val ZONE_ID: ZoneId = ZoneId.of("Asia/Karachi")

    fun now(): ZonedDateTime = ZonedDateTime.now(ZONE_ID)

    fun nowLocalDate(): LocalDate = LocalDate.now(ZONE_ID)

    fun isMarketOpen(zonedDateTime: ZonedDateTime = now()): Boolean {
        val dt = zonedDateTime.withZoneSameInstant(ZONE_ID)
        val dayOfWeek = dt.dayOfWeek
        val time = dt.toLocalTime()

        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return false
        }

        val openTime: LocalTime
        val closeTime: LocalTime

        when (dayOfWeek) {
            DayOfWeek.FRIDAY -> {
                openTime = LocalTime.of(9, 15)
                closeTime = LocalTime.of(16, 30)
            }
            else -> {
                openTime = LocalTime.of(9, 30)
                closeTime = LocalTime.of(15, 30)
            }
        }

        return !time.isBefore(openTime) && !time.isAfter(closeTime)
    }

    fun taxYearStart(date: LocalDate = nowLocalDate()): LocalDate {
        val year = if (date.monthValue >= 7) date.year else date.year - 1
        return LocalDate.of(year, 7, 1)
    }

    fun isoWeekId(date: LocalDate = nowLocalDate()): String {
        val weekYear = date.get(IsoFields.WEEK_BASED_YEAR)
        val weekOfWeek = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
        return String.format(Locale.US, "%d-W%02d", weekYear, weekOfWeek)
    }
}
