package com.igent.mobile.domain.engine

import java.time.ZonedDateTime

data class QuietSettings(
    val quietHoursEnabled: Boolean,
    val quietStart: String = "22:00",
    val quietEnd: String = "06:00"
)

object Notifier {
    fun isQuietTime(s: QuietSettings, d: ZonedDateTime): Boolean {
        if (!s.quietHoursEnabled) return false
        val nowMin = PktEngine.pktMinutes(d)
        val startMin = toMin(s.quietStart)
        val endMin = toMin(s.quietEnd)
        if (startMin == endMin) return false
        return if (startMin < endMin) {
            nowMin >= startMin && nowMin < endMin
        } else {
            nowMin >= startMin || nowMin < endMin
        }
    }

    private fun toMin(timeStr: String): Int {
        val parts = timeStr.split(":").mapNotNull { it.toIntOrNull() }
        if (parts.size < 2) return 0
        return parts[0] * 60 + parts[1]
    }
}
