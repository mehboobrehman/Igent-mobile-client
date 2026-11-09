package com.igent.mobile.domain.usecase

import com.igent.mobile.domain.model.FinancialAnalysisResult
import com.igent.mobile.domain.model.FinancialAnalysisType
import com.igent.mobile.domain.repository.AiRepository
import com.igent.mobile.domain.repository.PsxRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class RunFinancialAnalysisUseCase @Inject constructor(
    private val aiRepository: AiRepository,
    private val psxRepository: PsxRepository
) {
    suspend operator fun invoke(
        type: FinancialAnalysisType,
        target: String
    ): Result<FinancialAnalysisResult> {
        if (target.isBlank()) {
            return Result.failure(IllegalArgumentException("Target symbol or fund name cannot be blank"))
        }

        return try {
            // Fetch live PSX stock data if applicable
            val stocks = try {
                psxRepository.getStocks().getOrDefault(emptyList())
            } catch (e: Exception) {
                emptyList()
            }

            val matchingStock = stocks.find { 
                it.symbol.equals(target, ignoreCase = true) || 
                it.name.contains(target, ignoreCase = true) 
            }

            val marketContext = if (matchingStock != null) {
                "Live Market Data for ${matchingStock.symbol} (${matchingStock.name}): Price = Rs. ${matchingStock.price}, Change = ${matchingStock.change} (${matchingStock.percentChange}%), High = ${matchingStock.high}, Low = ${matchingStock.low}, Volume = ${matchingStock.volume}"
            } else {
                "No direct live stock match found in PSX cache for '$target'. General market context applies."
            }

            val prompt = when (type) {
                FinancialAnalysisType.BUY_SELL -> """
                    Perform a rigorous 'Should I Buy/Sell' financial analysis for target: '$target'.
                    $marketContext
                    Provide analysis including:
                    1. Verdict (e.g. STRONG BUY, BUY, HOLD, SELL, STRONG SELL)
                    2. Score from 0 to 100
                    3. Detailed summary paragraph
                    4. 3-4 bullet key points
                    5. Key metrics (e.g. P/E Ratio estimate, Support/Resistance, Volatility, Dividend Yield)
                    Format strictly as JSON or clearly labeled structured sections if requested, or concise analysis.
                """.trimIndent()
                
                FinancialAnalysisType.FUND_PICKER -> """
                    Perform a 'Fund Picker' analysis comparing mutual funds or evaluating investment fund '$target' in Pakistan (e.g. Meezan Islamic Fund, Al-Meezan, NBP Funds, ABL, HBL Asset Management).
                    $marketContext
                    Provide analysis including:
                    1. Verdict (e.g. TOP PICK, RECOMMENDED, MODERATE, AVOID)
                    2. Score from 0 to 100
                    3. Detailed summary paragraph
                    4. 3-4 bullet key points regarding risk, historical return, and expense ratio
                    5. Key metrics (e.g. 1Y Return, Expense Ratio, Fund Category, Risk Profile)
                """.trimIndent()
                
                FinancialAnalysisType.SCAM_CHECK -> """
                    Perform a rigorous 'Scam Check' and legitimacy audit for investment opportunity or entity '$target'.
                    Evaluate against common financial scams in Pakistan (e.g. unauthorized multi-level marketing, unregistered investment advisory, unrealistic guaranteed daily returns, SECP/SBP regulatory status).
                    Provide analysis including:
                    1. Verdict (e.g. VERIFIED LEGITIMATE, CAUTION ADVISED, HIGH RISK / POTENTIAL SCAM)
                    2. Safety Score from 0 to 100 (where 100 is 100% safe/legitimate, 0 is outright fraudulent scam)
                    3. Detailed summary explaining legitimacy risks or regulatory compliance
                    4. 3-4 bullet red flags or green flags
                    5. Key metrics (e.g. SECP License Status, PSX Listing, Ponzi Risk Level)
                """.trimIndent()
            }

            val aiResult = aiRepository.sendMessage(prompt)
            aiResult.fold(
                onSuccess = { message ->
                    val content = message.content
                    // Parse or construct FinancialAnalysisResult from AI response
                    val verdict = extractVerdict(content, type)
                    val score = extractScore(content)
                    val keyPoints = extractBulletPoints(content)
                    val metrics = extractMetrics(content, matchingStock)

                    Result.success(
                        FinancialAnalysisResult(
                            toolType = type,
                            targetName = target.uppercase(),
                            verdict = verdict,
                            score = score,
                            summary = content,
                            keyPoints = keyPoints,
                            metrics = metrics
                        )
                    )
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractVerdict(content: String, type: FinancialAnalysisType): String {
        return "ANALYSIS COMPLETE"
    }

    private fun extractScore(content: String): Int {
        val regex = Regex("""(?i)(score|rating|safety)[:\s]+([0-9]{1,3})(/100)?""")
        val match = regex.find(content)
        if (match != null) {
            val num = match.groupValues[2].toIntOrNull()
            if (num != null && num in 0..100) return num
        }
        return 0
    }

    private fun extractBulletPoints(content: String): List<String> {
        val lines = content.lines()
        val points = mutableListOf<String>()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("-") || trimmed.startsWith("*") || trimmed.matches(Regex("""^[0-9]+\..*"""))) {
                val cleaned = trimmed.replace(Regex("""^[-*0-9]+\.\s*"""), "").trim()
                if (cleaned.length > 10) {
                    points.add(cleaned)
                }
            }
        }
        return points.take(5)
    }

    private fun extractMetrics(content: String, stock: com.igent.mobile.domain.model.Stock?): Map<String, String> {
        val map = mutableMapOf<String, String>()
        if (stock != null) {
            map["Current Price"] = "Rs. ${stock.price}"
            map["24h Change"] = "${stock.change} (${stock.percentChange}%)"
        }
        return map
    }
}
