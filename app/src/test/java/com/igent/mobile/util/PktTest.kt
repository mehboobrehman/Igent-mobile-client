package com.igent.mobile.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class PktTest {

    @Test
    fun testZoneId() {
        assertEquals(ZoneId.of("Asia/Karachi"), Pkt.ZONE_ID)
    }

    @Test
    fun testMarketOpenMonToThu() {
        // Monday 2026-11-09 at 10:00 AM PKT -> Open
        val dtOpen = ZonedDateTime.of(2026, 11, 9, 10, 0, 0, 0, Pkt.ZONE_ID)
        assertTrue(Pkt.isMarketOpen(dtOpen))

        // Monday 2026-11-09 at 16:00 PM PKT -> Closed (after 15:30)
        val dtClosed = ZonedDateTime.of(2026, 11, 9, 16, 0, 0, 0, Pkt.ZONE_ID)
        assertFalse(Pkt.isMarketOpen(dtClosed))
    }

    @Test
    fun testMarketOpenFriday() {
        // Friday 2026-11-13 at 10:00 AM PKT -> Open (Friday hours: 09:15 - 16:30)
        val dtOpen = ZonedDateTime.of(2026, 11, 13, 10, 0, 0, 0, Pkt.ZONE_ID)
        assertTrue(Pkt.isMarketOpen(dtOpen))

        // Friday 2026-11-13 at 16:00 PM PKT -> Open
        val dtFridayOpen = ZonedDateTime.of(2026, 11, 13, 16, 0, 0, 0, Pkt.ZONE_ID)
        assertTrue(Pkt.isMarketOpen(dtFridayOpen))

        // Friday 2026-11-13 at 17:00 PM PKT -> Closed
        val dtClosed = ZonedDateTime.of(2026, 11, 13, 17, 0, 0, 0, Pkt.ZONE_ID)
        assertFalse(Pkt.isMarketOpen(dtClosed))
    }

    @Test
    fun testMarketClosedWeekend() {
        // Saturday 2026-11-14 at 11:00 AM PKT -> Closed
        val dtSat = ZonedDateTime.of(2026, 11, 14, 11, 0, 0, 0, Pkt.ZONE_ID)
        assertFalse(Pkt.isMarketOpen(dtSat))

        // Sunday 2026-11-15 at 11:00 AM PKT -> Closed
        val dtSun = ZonedDateTime.of(2026, 11, 15, 11, 0, 0, 0, Pkt.ZONE_ID)
        assertFalse(Pkt.isMarketOpen(dtSun))
    }

    @Test
    fun testTaxYearStart() {
        // May 2026 -> Tax year start is July 1, 2025
        val dateMay = LocalDate.of(2026, 5, 15)
        assertEquals(LocalDate.of(2025, 7, 1), Pkt.taxYearStart(dateMay))

        // August 2026 -> Tax year start is July 1, 2026
        val dateAug = LocalDate.of(2026, 8, 10)
        assertEquals(LocalDate.of(2026, 7, 1), Pkt.taxYearStart(dateAug))

        // July 1, 2026 -> Tax year start is July 1, 2026
        val dateJuly1 = LocalDate.of(2026, 7, 1)
        assertEquals(LocalDate.of(2026, 7, 1), Pkt.taxYearStart(dateJuly1))
    }

    @Test
    fun testIsoWeekId() {
        // 2026-11-09 is Monday of week 46 in 2026 (or check ISO week)
        val date = LocalDate.of(2026, 11, 9)
        val weekId = Pkt.isoWeekId(date)
        assertTrue(weekId.startsWith("2026-W"))
    }
}
