package com.igent.mobile.domain.model

data class FinancialAnalysisResult(
    val toolType: FinancialAnalysisType,
    val targetName: String,
    val verdict: String, // e.g. "STRONG BUY", "HOLD", "CAUTION", "HIGH RISK / SCAM WARNING"
    val score: Int, // 0 to 100
    val summary: String,
    val keyPoints: List<String>,
    val metrics: Map<String, String>,
    val timestamp: Long = System.currentTimeMillis()
)

enum class FinancialAnalysisType(val displayName: String, val description: String) {
    BUY_SELL("Should I Buy / Sell?", "Evaluate investment entry/exit points based on PSX valuations & momentum"),
    FUND_PICKER("Fund Picker", "Compare mutual funds & equity portfolios for risk-adjusted returns"),
    SCAM_CHECK("Scam Check", "Verify legitimacy & detect potential fraudulent investment schemes")
}
