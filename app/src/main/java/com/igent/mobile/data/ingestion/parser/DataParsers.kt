package com.igent.mobile.data.ingestion.parser

import org.jsoup.Jsoup
import java.time.LocalDate
import java.util.regex.Pattern

class ParserMatchedNothing(what: String) : Exception("parser matched nothing ($what)")

object DataParsers {

    fun clean(s: String?): String {
        return (s ?: "").replace(Regex("\\s+"), " ").trim()
    }

    fun num(s: Any?): Double? {
        if (s == null) return null
        var t = s.toString().replace(Regex("[,\\s%]|Rs\\.?", RegexOption.IGNORE_CASE), "").trim()
        if (t.isEmpty() || t == "-" || t.equals("n/a", ignoreCase = true)) return null
        var neg = false
        if (t.startsWith("(") && t.endsWith(")")) {
            neg = true
            t = t.substring(1, t.length - 1)
        }
        val v = t.toDoubleOrNull() ?: return null
        return if (neg) -v else v
    }

    fun pkToday(): String {
        return LocalDate.now().toString()
    }

    fun parseSbp(html: String, today: String = pkToday()): List<Map<String, Any>> {
        val doc = Jsoup.parse(html)
        val text = clean(doc.body().text())
        val rows = mutableListOf<Map<String, Any>>()

        val prMatcher = Pattern.compile("SBP Policy Rate\\s+(\\d+(?:\\.\\d+)?)%", Pattern.CASE_INSENSITIVE).matcher(text)
        val prMatcher2 = Pattern.compile("policy rate (?:unchanged at|to|at)\\s+(\\d+(?:\\.\\d+)?)%", Pattern.CASE_INSENSITIVE).matcher(text)
        val prVal = if (prMatcher.find()) prMatcher.group(1) else if (prMatcher2.find()) prMatcher2.group(1) else null
        if (prVal != null) {
            val mpcMatcher = Pattern.compile("meeting held on (\\d{1,2} [A-Za-z]+,? \\d{4})", Pattern.CASE_INSENSITIVE).matcher(text)
            val mpcDate = if (mpcMatcher.find()) mpcMatcher.group(1) else null
            rows.add(mapOf("kind" to "policy_rate", "date" to today, "value" to prVal.toDouble(), "meta" to mapOf("mpcDate" to mpcDate)))
        }

        val ceilingMatcher = Pattern.compile("Reverse \\(Repo\\) Ceiling Rate\\s+(\\d+(?:\\.\\d+)?)%", Pattern.CASE_INSENSITIVE).matcher(text)
        if (ceilingMatcher.find()) {
            rows.add(mapOf("kind" to "sbp_ceiling_rate", "date" to today, "value" to ceilingMatcher.group(1)!!.toDouble()))
        }

        val floorMatcher = Pattern.compile("Reverse \\(Floor\\) Rate\\s+(\\d+(?:\\.\\d+)?)%", Pattern.CASE_INSENSITIVE).matcher(text)
        if (floorMatcher.find()) {
            rows.add(mapOf("kind" to "sbp_floor_rate", "date" to today, "value" to floorMatcher.group(1)!!.toDouble()))
        }

        val tables = doc.select("table")
        val kiborTable = tables.firstOrNull { t -> t.text().contains("BID", ignoreCase = true) && t.text().contains("Offer", ignoreCase = true) }
        if (kiborTable != null) {
            kiborTable.select("tr").forEach { tr ->
                val cells = tr.select("td").map { clean(it.text()) }
                if (cells.size >= 3) {
                    val tenorMatcher = Pattern.compile("^(\\d+)-M$", Pattern.CASE_INSENSITIVE).matcher(cells[0])
                    if (tenorMatcher.find()) {
                        val tenor = tenorMatcher.group(1)
                        val bid = num(cells[1])
                        val offer = num(cells[2])
                        if (offer != null) {
                            rows.add(mapOf("kind" to "kibor_${tenor}m", "date" to today, "value" to offer, "meta" to mapOf("bid" to (bid ?: 0.0))))
                        }
                    }
                }
            }
        }

        if (rows.none { it["kind"] == "policy_rate" || (it["kind"] as String).startsWith("kibor_") }) {
            throw ParserMatchedNothing("SBP policy rate / KIBOR")
        }
        return rows
    }

    fun parsePsxIndices(html: String): List<Map<String, Any>> {
        val doc = Jsoup.parse(html)
        val text = clean(doc.body().text())
        val asOfMatcher = Pattern.compile("As of\\s+([A-Za-z]{3,9} \\d{1,2}, \\d{4})", Pattern.CASE_INSENSITIVE).matcher(text)
        val date = if (asOfMatcher.find()) asOfMatcher.group(1) ?: pkToday() else pkToday()

        val rows = mutableListOf<Map<String, Any>>()
        doc.select("table").forEach { t ->
            val head = clean(t.select("tr").first()?.text())
            if (head.contains("High", ignoreCase = true) && head.contains("Current", ignoreCase = true)) {
                t.select("tr").drop(1).forEach { tr ->
                    val c = tr.select("td").map { clean(it.text()) }
                    if (c.size >= 6) {
                        val code = c[0].split(" ")[0].uppercase()
                        val level = num(c[3])
                        if (code.isNotEmpty() && level != null) {
                            rows.add(mapOf(
                                "indexCode" to code,
                                "date" to date,
                                "level" to level,
                                "high" to (num(c[1]) ?: level),
                                "low" to (num(c[2]) ?: level),
                                "changeAbs" to (num(c[4]) ?: 0.0),
                                "changePct" to (num(c[5]) ?: 0.0)
                            ))
                        }
                    }
                }
            }
        }
        if (rows.isEmpty()) throw ParserMatchedNothing("PSX indices table")
        return rows
    }

    fun parseMufapNavs(html: String): List<Map<String, Any>> {
        val doc = Jsoup.parse(html)
        val table = if (doc.select("#table_id").isNotEmpty()) doc.select("#table_id").first()!! else doc.select("table").first()
        if (table == null) throw ParserMatchedNothing("MUFAP table")

        val head = table.select("tr").first()?.select("th,td")?.map { clean(it.text()).lowercase() } ?: emptyList()
        val fundIdx = head.indexOf("fund")
        val navIdx = head.indexOf("nav")
        val validityIdx = head.indexOf("validity date")
        if (fundIdx < 0 || navIdx < 0) throw ParserMatchedNothing("MUFAP header")

        val rows = mutableListOf<Map<String, Any>>()
        val seen = mutableSetOf<String>()

        table.select("tr").drop(1).forEach { tr ->
            val c = tr.select("td").map { clean(it.text()) }
            if (c.size > fundIdx && c.size > navIdx) {
                val fundName = c[fundIdx]
                val nav = num(c[navIdx])
                val date = if (validityIdx >= 0 && c.size > validityIdx) c[validityIdx] else pkToday()
                if (fundName.isNotEmpty() && nav != null && nav > 0) {
                    val key = "$fundName|$date"
                    if (!seen.contains(key)) {
                        seen.add(key)
                        rows.add(mapOf(
                            "fundName" to fundName,
                            "date" to date,
                            "amc" to (if (head.indexOf("amc") >= 0) c.getOrNull(head.indexOf("amc")) ?: "" else ""),
                            "category" to (if (head.indexOf("category") >= 0) c.getOrNull(head.indexOf("category")) ?: "" else ""),
                            "nav" to nav
                        ))
                    }
                }
            }
        }
        if (rows.isEmpty()) throw ParserMatchedNothing("MUFAP rows")
        return rows
    }

    fun parseGold(html: String): List<Map<String, Any>> {
        val doc = Jsoup.parse(html)
        val text = clean(doc.body().text())
        val k24Matcher = Pattern.compile("24 karat is Rs\\.?\\s*([\\d,.]+)", Pattern.CASE_INSENSITIVE).matcher(text)
        val v24 = if (k24Matcher.find()) num(k24Matcher.group(1)) else null
        val rows = mutableListOf<Map<String, Any>>()
        if (v24 != null) {
            rows.add(mapOf("kind" to "gold_24k_tola", "date" to pkToday(), "value" to v24, "meta" to mapOf("source" to "gold.pk")))
        }
        if (rows.isEmpty()) throw ParserMatchedNothing("gold.pk tola rate")
        return rows
    }

    fun parseDollar(html: String): List<Map<String, Any>> {
        val doc = Jsoup.parse(html)
        var buy: Double? = null
        var sell: Double? = null
        doc.select("tr").forEach { tr ->
            val c = tr.select("td, th").map { clean(it.text()) }
            if (c.size in 4..6 && c[1].equals("USD", ignoreCase = true)) {
                buy = num(c[2])
                sell = num(c[3])
                return@forEach
            }
        }
        if (buy == null || sell == null) throw ParserMatchedNothing("forex.pk USD row")
        return listOf(
            mapOf("kind" to "usd_open_buy", "date" to pkToday(), "value" to buy!!, "meta" to mapOf("source" to "forex.pk")),
            mapOf("kind" to "usd_open_sell", "date" to pkToday(), "value" to sell!!, "meta" to mapOf("source" to "forex.pk"))
        )
    }
}
