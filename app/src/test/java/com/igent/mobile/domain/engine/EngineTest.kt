package com.igent.mobile.domain.engine

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class EngineTest {

    @Test
    fun testA1_ScamRules() {
        val high = ScamRules.highReturnFlag("Earn 5% monthly profit!")
        assertNotNull(high)
        assertTrue(high!!.flag.contains("5% monthly"))

        val low = ScamRules.highReturnFlag("Earn 1% monthly profit.")
        assertNull(low)

        val flags = ScamRules.ruleFlags("Guaranteed return and advance fee required.")
        assertTrue(flags.isNotEmpty())

        val verdict = ScamRules.ruleVerdict(flags)
        assertTrue(verdict == "likely_scam" || verdict == "suspicious")
        assertFalse(ScamRules.RULES.isEmpty())
        assertFalse(ScamRules.STANDARD_CHECKS.isEmpty())
    }

    @Test
    fun testA2_GoalPlanner() {
        val months = GoalPlanner.monthsBetween("2026-01-01", "2027-01-01")
        assertEquals(12, months)

        val plan = GoalPlanner.goalPlan(
            GoalInput(
                targetAmount = 1000000.0,
                targetDate = "2028-01-01",
                inflationPct = 5.0,
                returnPct = 10.0,
                saved = 100000.0,
                monthly = 10000.0
            ),
            today = "2026-01-01"
        )
        assertEquals(24, plan.months)
        assertTrue(plan.futureTarget > 0.0)
        assertTrue(plan.projected > 0.0)
    }

    @Test
    fun testA3_SafetyNet() {
        val netInput = NetInput(
            monthlyIncome = 150000.0,
            monthlyExpenses = 100000.0,
            bankSavings = 400000.0,
            goldTola = 2.0,
            propertyValue = 5000000.0,
            debt = 200000.0,
            age = 30,
            retireAge = 60
        )
        val report = SafetyNet.safetyReport(netInput, cash = 50000.0, holdingsValue = 300000.0, goldPerTola = 250000.0)
        assertEquals(4.0, report.months!!, 1e-9)
        assertEquals("ok", report.level)
        assertEquals(50000.0, report.surplus!!, 1e-9)
        assertEquals(30, report.yearsToRetire)
        assertTrue(report.gross > 0.0)
    }

    @Test
    fun testA4_Ledger() {
        val rows = listOf(
            TxRow("1", "stock", "OGDC", "buy", 100.0, 10000.0, 50.0, "2026-01-01"),
            TxRow("2", "stock", "OGDC", "sell", 50.0, 6000.0, 30.0, "2026-02-01")
        )
        val result = Ledger.replay(rows)
        assertEquals(1, result.positions.size)
        assertEquals(50.0, result.positions[0].qty, 1e-9)
        assertEquals(2, result.tx.size)
        assertNotNull(result.tx[1].realized)
    }

    @Test
    fun testA5_CardScoring() {
        assertEquals(2, CardScoring.scoreFromExcess(6.0))
        assertEquals(1, CardScoring.scoreFromExcess(2.0))
        assertEquals(0, CardScoring.scoreFromExcess(0.0))
        assertEquals(-1, CardScoring.scoreFromExcess(-2.0))
        assertEquals(-2, CardScoring.scoreFromExcess(-6.0))

        val card = CardInput("stock", "2026-01-01T00:00:00Z", EntryRef(price = 100.0))
        val ret = CardScoring.cardReturn(card, CurrentValue(110.0), java.time.Instant.parse("2026-01-11T00:00:00Z").toEpochMilli())
        assertNotNull(ret)
        assertEquals(10.0, ret!!, 1e-9)
    }

    @Test
    fun testA6_FundRanker() {
        val fund = FundInput("Al-Meezan", "Islamic Equity", 20.0, 1.5)
        val score = FundRanker.fundScore(fund, "equity")
        assertTrue(score < 20.0)

        assertEquals("safe", FundRanker.fundGroup("Money Market Fund"))
        assertEquals("equity", FundRanker.fundGroup("Islamic Equity"))
        assertTrue(FundRanker.isShariahFund("Meezan Islamic Fund"))
    }

    @Test
    fun testA7_ThresholdEngine() {
        val watchlist = listOf(WatchlistItem("stock", "OGDC", 3.0))
        val stockPrices = listOf(
            PriceRow("OGDC", "2026-01-02", 105.0),
            PriceRow("OGDC", "2026-01-01", 100.0)
        )
        val hits = ThresholdEngine.computeThresholdHits(watchlist, stockPrices, emptyList(), emptyList(), emptyList())
        assertEquals(1, hits.size)
        assertEquals("OGDC", hits[0].subject)
        assertEquals(5.0, hits[0].pct, 1e-9)
    }

    @Test
    fun testA8_Calculators() {
        val fv = Calculators.futureValue(100000.0, 10.0, 5.0, 5000.0)
        assertTrue(fv > 100000.0)

        val saving = Calculators.monthlySavingFor(1000000.0, 10.0, 5.0, 0.0)
        assertTrue(saving > 0.0)

        val realRet = Calculators.realReturnPct(15.0, 10.0)
        assertEquals(4.545454545454547, realRet, 1e-7)

        val cagr = Calculators.cagrPct(100.0, 200.0, 2.0)
        assertEquals(41.42135623730951, cagr, 1e-7)

        val evalResult = Calculators.evaluate("2 + 3 * (4 - 1)")
        assertEquals(11.0, evalResult, 1e-9)
    }

    @Test
    fun testA9_StockCheckup() {
        val stock = StockFact(
            symbol = "OGDC",
            marketCap = 4e9,
            freeFloatPct = 10.0,
            change1mPct = 35.0,
            close = 150.0,
            w52High = 150.0,
            w52Low = 50.0,
            lastVolume = 500000.0,
            avgVolume60 = 100000.0,
            avgVolume20 = null,
            sessions = 15,
            eps = -5.0,
            pe = 45.0,
            kse100 = false,
            sector = "Oil & Gas",
            sectorKey = "oil-gas-exploration"
        )
        val flags = StockCheckup.stockFlags(stock, 250.0)
        assertTrue(flags.isNotEmpty())
        assertEquals("oil-gas-exploration", StockCheckup.sectorKey("Oil & Gas Exploration"))
    }

    @Test
    fun testA10_Screener() {
        val opts = ScreenOptions(sort = "risers")
        assertEquals("risers", opts.sort)
    }

    @Test
    fun testA11_QuizEngine() {
        val q = QuizQuestion("1", "What is stock?", listOf("A", "B", "C", "D"), 1, "Explanation")
        val res = QuizEngine.submitQuiz(listOf(q), listOf(1))
        assertEquals(1, res.correct)
        assertEquals(1, res.total)
    }

    @Test
    fun testA12_PracticeBook() {
        val trades = listOf(
            PracticeTradeInput("stock", "OGDC", "Oil & Gas", "buy", 100.0, 100.0, 20.0, "2026-01-01")
        )
        val (cash, held) = PracticeBook.book(trades)
        assertTrue(cash < PracticeBook.PRACTICE_START)
        assertEquals(1, held.size)
        assertEquals(100.0, held[0].qty, 1e-9)
    }

    @Test
    fun testA13_PktEngine() {
        val date = LocalDate.of(2026, 11, 9)
        assertEquals(LocalDate.of(2026, 7, 1), PktEngine.taxYearStart(date))

        val zdt = ZonedDateTime.of(2026, 11, 9, 10, 0, 0, 0, ZoneId.of("Asia/Karachi"))
        assertTrue(PktEngine.inMarketHours(zdt))
    }

    @Test
    fun testA14_Notifier() {
        val settings = QuietSettings(quietHoursEnabled = true, quietStart = "22:00", quietEnd = "06:00")
        val zdtNight = ZonedDateTime.of(2026, 11, 9, 3, 0, 0, 0, ZoneId.of("Asia/Karachi"))
        assertTrue(Notifier.isQuietTime(settings, zdtNight))

        val zdtDay = ZonedDateTime.of(2026, 11, 9, 14, 0, 0, 0, ZoneId.of("Asia/Karachi"))
        assertFalse(Notifier.isQuietTime(settings, zdtDay))
    }
}
