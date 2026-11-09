package com.igent.mobile.data.ingestion

import com.igent.mobile.data.ingestion.parser.DataParsers
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ParserTest {

    private fun loadFixture(filename: String): String {
        val file = File("src/test/resources/fixtures/$filename")
        if (!file.exists()) {
            throw IllegalArgumentException("Fixture file not found: ${file.absolutePath}")
        }
        return file.readText(Charsets.UTF_8)
    }

    @Test
    fun testParseSbp() {
        val html = loadFixture("sbp_sample.html")
        val rates = DataParsers.parseSbp(html)
        assertTrue(rates.isNotEmpty())
        val policyRate = rates.find { it["kind"] == "policy_rate" }
        assertNotNull(policyRate)
        assertEquals(22.0, policyRate!!["value"])
    }

    @Test
    fun testParsePsxIndices() {
        val html = loadFixture("psx_indices_sample.html")
        val indices = DataParsers.parsePsxIndices(html)
        assertTrue(indices.isNotEmpty())
        val kse100 = indices.find { it["indexCode"] == "KSE100" }
        assertNotNull(kse100)
        assertEquals(90200.0, kse100!!["level"])
    }

    @Test
    fun testParseMufapNavs() {
        val html = loadFixture("mufap_sample.html")
        val navs = DataParsers.parseMufapNavs(html)
        assertTrue(navs.isNotEmpty())
        assertEquals("Meezan Cash Fund", navs[0]["fundName"])
        assertEquals(100.5, navs[0]["nav"])
    }

    @Test
    fun testParseGold() {
        val html = loadFixture("gold_sample.html")
        val gold = DataParsers.parseGold(html)
        assertTrue(gold.isNotEmpty())
        assertEquals("gold_24k_tola", gold[0]["kind"])
        assertEquals(240000.0, gold[0]["value"])
    }

    @Test
    fun testParseDollar() {
        val html = loadFixture("dollar_sample.html")
        val dollar = DataParsers.parseDollar(html)
        assertTrue(dollar.size >= 2)
        assertEquals("usd_open_sell", dollar[1]["kind"])
        assertEquals(280.0, dollar[1]["value"])
    }
}
