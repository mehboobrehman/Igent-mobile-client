package com.igent.mobile.data.repository

import com.igent.mobile.data.local.dao.CourseDao
import com.igent.mobile.data.local.entity.CourseEntity
import com.igent.mobile.data.local.entity.CourseModuleEntity
import com.igent.mobile.data.local.entity.QuizEntity
import com.igent.mobile.data.local.entity.QuizScoreEntity
import com.igent.mobile.domain.repository.LearningRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LearningRepositoryImpl @Inject constructor(
    private val courseDao: CourseDao
) : LearningRepository {

    override fun getAllCourses(): Flow<List<CourseEntity>> = courseDao.getAllCourses()

    override suspend fun getCourseById(courseId: String): CourseEntity? = withContext(Dispatchers.IO) {
        courseDao.getCourseById(courseId)
    }

    override fun getModulesForCourse(courseId: String): Flow<List<CourseModuleEntity>> =
        courseDao.getModulesForCourse(courseId)

    override fun getQuizzesForCourse(courseId: String): Flow<List<QuizEntity>> =
        courseDao.getQuizzesForCourse(courseId)

    override fun getAllQuizScores(): Flow<List<QuizScoreEntity>> = courseDao.getAllQuizScores()

    override suspend fun markModuleCompleted(moduleId: String) = withContext(Dispatchers.IO) {
        courseDao.markModuleCompleted(moduleId)
    }

    override suspend fun submitQuizAnswer(quizId: String, courseId: String, selectedOptionIndex: Int): Boolean = withContext(Dispatchers.IO) {
        val quizzes = courseDao.getQuizzesForCourse(courseId)
        // Check correctness against stored quiz
        // For simplicity, query or retrieve
        // Since we want correctness:
        val quizList = quizzes.first()
        val quiz = quizList.find { it.quizId == quizId }
        val isCorrect = quiz != null && quiz.correctOptionIndex == selectedOptionIndex

        val scoreEntity = QuizScoreEntity(
            quizId = quizId,
            courseId = courseId,
            selectedOptionIndex = selectedOptionIndex,
            isCorrect = isCorrect,
            timestamp = System.currentTimeMillis()
        )
        courseDao.insertQuizScore(scoreEntity)
        isCorrect
    }

    override suspend fun initializeDefaultCourses() = withContext(Dispatchers.IO) {
        val defaultCourses = listOf(
            CourseEntity(
                courseId = "psx_basics_101",
                title = "PSX & Pakistan Market Fundamentals",
                description = "Master the structure, trading mechanisms, KSE-100 index, regulatory bodies, and investor protection funds of the Pakistan Stock Exchange.",
                category = "PSX Basics",
                difficulty = "Beginner",
                estimatedMinutes = 30,
                progressPercent = 0
            ),
            CourseEntity(
                courseId = "tech_analysis_201",
                title = "Technical Analysis & Chart Patterns",
                description = "Learn how to read candlestick charts, RSI, MACD, support/resistance levels, and volume indicators for profitable swing trading on PSX.",
                category = "Technical Analysis",
                difficulty = "Intermediate",
                estimatedMinutes = 45,
                progressPercent = 0
            ),
            CourseEntity(
                courseId = "dividend_investing_301",
                title = "Dividend Yield & Value Investing",
                description = "Discover high-dividend blue-chip stocks in Pakistan (Energy, Banks, Fertilizers) and calculate dividend payout ratios, P/E, and intrinsic value.",
                category = "Fundamental Investing",
                difficulty = "Advanced",
                estimatedMinutes = 40,
                progressPercent = 0
            ),
            CourseEntity(
                courseId = "derivatives_futures_401",
                title = "Derivatives, Futures & Leverage Trading",
                description = "Understand deliverable and cash-settled futures contracts on PSX, leverage mechanisms, margin calls, and risk management strategies.",
                category = "Derivatives & Leverage",
                difficulty = "Advanced",
                estimatedMinutes = 30,
                progressPercent = 0
            )
        )
        courseDao.insertCourses(defaultCourses)

        // Modules for Course 1: PSX & Pakistan Market Fundamentals
        val modules1 = listOf(
            CourseModuleEntity(
                moduleId = "mod_1_1",
                courseId = "psx_basics_101",
                title = "Introduction to Pakistan Stock Exchange (PSX)",
                content = "The Pakistan Stock Exchange (PSX) was formed in 2016 by the integration of Karachi, Lahore, and Islamabad Stock Exchanges. It is regulated by the Securities and Exchange Commission of Pakistan (SECP). KSE-100 is the benchmark index representing the top 100 companies by market capitalization across various economic sectors.",
                orderIndex = 1
            ),
            CourseModuleEntity(
                moduleId = "mod_1_2",
                courseId = "psx_basics_101",
                title = "Trading Hours, Settlement & CDC",
                content = "PSX trading sessions run from Monday to Thursday (9:30 AM to 3:30 PM) and Friday (9:00 AM to 4:30 PM with a prayer break). Transactions are settled through the Central Depository Company (CDC) of Pakistan under electronic book-entry systems on a T+1 / T+2 settlement cycle, ensuring secure share custody.",
                orderIndex = 2
            ),
            CourseModuleEntity(
                moduleId = "mod_1_3",
                courseId = "psx_basics_101",
                title = "Investor Protection & NCCPL Risk Management",
                content = "The National Clearing Company of Pakistan Limited (NCCPL) acts as the central counterparty for clearing and settlement. Investor Protection Fund (IPF) and Jamapunji investor education portal safeguard retail investors against brokerage defaults and enhance financial literacy in Pakistan.",
                orderIndex = 3
            )
        )
        courseDao.insertModules(modules1)

        // Quizzes for Course 1
        val quizzes1 = listOf(
            QuizEntity(
                quizId = "quiz_1_1",
                courseId = "psx_basics_101",
                question = "What does the benchmark KSE-100 index represent on the PSX?",
                optionsJson = "[\"Top 100 dividend-paying stocks\",\"Top 100 companies by market capitalization\",\"All listed companies on PSX\",\"Top 10 banking stocks\"]",
                correctOptionIndex = 1,
                explanation = "The KSE-100 index comprises the top 100 companies representing over 85% of total market capitalization on the PSX."
            ),
            QuizEntity(
                quizId = "quiz_1_2",
                courseId = "psx_basics_101",
                question = "Which entity acts as the central depository for holding securities electronically in Pakistan?",
                optionsJson = "[\"State Bank of Pakistan (SBP)\",\"Central Depository Company (CDC)\",\"Securities and Exchange Commission (SECP)\",\"National Clearing Company (NCCPL)\"]",
                correctOptionIndex = 1,
                explanation = "The Central Depository Company (CDC) operates the Central Depository System (CDS) for electronic custody and transfer of securities."
            ),
            QuizEntity(
                quizId = "quiz_1_3",
                courseId = "psx_basics_101",
                question = "What is the primary role of NCCPL in the Pakistan capital market?",
                optionsJson = "[\"Approving IPO prospectuses\",\"Clearing and settlement of trades and risk management\",\"Setting monetary policy and interest rates\",\"Issuing brokerage licenses\"]",
                correctOptionIndex = 1,
                explanation = "NCCPL performs clearing and settlement of trades executed on stock exchanges, managing counterparty risk and margin requirements."
            )
        )
        courseDao.insertQuizzes(quizzes1)

        // Modules for Course 2: Technical Analysis & Chart Patterns
        val modules2 = listOf(
            CourseModuleEntity(
                moduleId = "mod_2_1",
                courseId = "tech_analysis_201",
                title = "Candlestick Anatomy & Support/Resistance",
                content = "Candlestick charts show open, high, low, and close prices. Support is a price level where downward price movement tends to halt due to concentrated demand, while resistance is where upward movement pauses due to selling pressure.",
                orderIndex = 1
            ),
            CourseModuleEntity(
                moduleId = "mod_2_2",
                courseId = "tech_analysis_201",
                title = "Momentum Oscillators: RSI & MACD",
                content = "The Relative Strength Index (RSI) measures speed and change of price movements on a 0-100 scale (overbought >70, oversold <30). Moving Average Convergence Divergence (MACD) shows trend momentum through the convergence and divergence of exponential moving averages.",
                orderIndex = 2
            ),
            CourseModuleEntity(
                moduleId = "mod_2_3",
                courseId = "tech_analysis_201",
                title = "Volume Analysis & Chart Patterns",
                content = "Volume confirms price trends; rising prices accompanied by high volume validate bullish momentum. Classic patterns like Double Bottoms, Head and Shoulders, and Ascending Triangles provide reliable entry and exit signals.",
                orderIndex = 3
            )
        )
        courseDao.insertModules(modules2)

        // Quizzes for Course 2
        val quizzes2 = listOf(
            QuizEntity(
                quizId = "quiz_2_1",
                courseId = "tech_analysis_201",
                question = "What does an RSI reading above 70 typically indicate?",
                optionsJson = "[\"Oversold conditions indicating a buying opportunity\",\"Overbought conditions where a pullback or reversal may occur\",\"High market volatility with zero trend direction\",\"Neutral market equilibrium\"]",
                correctOptionIndex = 1,
                explanation = "An RSI above 70 indicates that the asset may be overbought, suggesting potential profit-taking or a corrective pullback."
            ),
            QuizEntity(
                quizId = "quiz_2_2",
                courseId = "tech_analysis_201",
                question = "How does trading volume validate a breakout in technical analysis?",
                optionsJson = "[\"High volume confirms the strength and sustainability of the breakout\",\"Low volume indicates institutional accumulation\",\"Volume has no impact on price breakouts\",\"Volume only matters in crypto markets\"]",
                correctOptionIndex = 0,
                explanation = "A breakout accompanied by high volume indicates strong market participation and conviction, making it more reliable."
            ),
            QuizEntity(
                quizId = "quiz_2_3",
                courseId = "tech_analysis_201",
                question = "What is a 'Support' level in stock charts?",
                optionsJson = "[\"A price ceiling where sellers dominate\",\"A price floor where buying interest is strong enough to halt price declines\",\"The all-time high price of a stock\",\"The average daily trading volume\"]",
                correctOptionIndex = 1,
                explanation = "Support is a price level where demand is strong enough to prevent the price from falling further."
            )
        )
        courseDao.insertQuizzes(quizzes2)

        // Modules for Course 3: Dividend Yield & Value Investing
        val modules3 = listOf(
            CourseModuleEntity(
                moduleId = "mod_3_1",
                courseId = "dividend_investing_301",
                title = "Evaluating Dividend Yield & Payout Ratios",
                content = "Dividend yield is annual dividends per share divided by the share price. A sustainable dividend payout ratio (typically 40%-70% of earnings) ensures companies retain enough capital for growth while rewarding shareholders.",
                orderIndex = 1
            ),
            CourseModuleEntity(
                moduleId = "mod_3_2",
                courseId = "dividend_investing_301",
                title = "PSX High-Yield Sectors: Energy, Banks & Fertilizers",
                content = "Pakistan's high-dividend sectors include Oil & Exploration (OGDC, PPL), Commercial Banks (HBL, UBL, MCB), and Fertilizer manufacturers (ENGRO, FFC). These companies generate robust free cash flows and regular quarterly payouts.",
                orderIndex = 2
            ),
            CourseModuleEntity(
                moduleId = "mod_3_3",
                courseId = "dividend_investing_301",
                title = "Value Investing & Margin of Safety",
                content = "Benjamin Graham's value investing philosophy focuses on buying stocks trading below their intrinsic value. The 'Margin of Safety' protects investors against analytical errors and adverse market downturns.",
                orderIndex = 3
            )
        )
        courseDao.insertModules(modules3)

        // Quizzes for Course 3
        val quizzes3 = listOf(
            QuizEntity(
                quizId = "quiz_3_1",
                courseId = "dividend_investing_301",
                question = "How is Dividend Yield calculated?",
                optionsJson = "[\"Annual Dividend per Share / Market Price per Share\",\"Net Income / Total Shareholders Equity\",\"Total Debt / Market Capitalization\",\"Earnings per Share / Stock Price\"]",
                correctOptionIndex = 0,
                explanation = "Dividend yield is calculated by dividing the annual dividends paid per share by the current share price."
            ),
            QuizEntity(
                quizId = "quiz_3_2",
                courseId = "dividend_investing_301",
                question = "Which PSX sectors are traditionally known for consistent high dividend payouts?",
                optionsJson = "[\"Technology and Startup companies\",\"Commercial Banks, Oil & Gas Exploration, and Fertilizers\",\"Speculative Small-cap Textile firms\",\"Retail and E-commerce platforms\"]",
                correctOptionIndex = 1,
                explanation = "Banks, O&G exploration companies, and fertilizer producers in Pakistan consistently offer attractive dividend yields."
            ),
            QuizEntity(
                quizId = "quiz_3_3",
                courseId = "dividend_investing_301",
                question = "What is the concept of 'Margin of Safety' in value investing?",
                optionsJson = "[\"Buying stocks at a significant discount to their estimated intrinsic value\",\"Hedging positions using futures contracts\",\"Maintaining 50% cash in your portfolio at all times\",\"Insuring brokerage accounts against market crashes\"]",
                correctOptionIndex = 0,
                explanation = "Margin of safety is the difference between intrinsic value and market price, protecting against miscalculations."
            )
        )
        courseDao.insertQuizzes(quizzes3)

        // Modules for Course 4: Derivatives, Futures & Leverage Trading
        val modules4 = listOf(
            CourseModuleEntity(
                moduleId = "mod_4_1",
                courseId = "derivatives_futures_401",
                title = "Introduction to Futures Contracts on PSX",
                content = "Futures contracts are standardized agreements to buy or sell an asset at a predetermined price on a specified future date. On PSX, Single Stock Futures (SSF) allow leveraged exposure to underlying equities with monthly expiration cycles.",
                orderIndex = 1
            ),
            CourseModuleEntity(
                moduleId = "mod_4_2",
                courseId = "derivatives_futures_401",
                title = "Margin Requirements, Mark-to-Market & Risk",
                content = "Trading futures requires initial margin and maintenance margin. Daily mark-to-market (MTM) settlements calculate profit or loss daily. Insufficient margin triggers margin calls and mandatory liquidation by clearing houses.",
                orderIndex = 2
            )
        )
        courseDao.insertModules(modules4)

        // Quizzes for Course 4
        val quizzes4 = listOf(
            QuizEntity(
                quizId = "quiz_4_1",
                courseId = "derivatives_futures_401",
                question = "What is a Single Stock Futures (SSF) contract?",
                optionsJson = "[\"An option to buy bonds at maturity\",\"A leveraged agreement to buy or sell a specific stock at a future date on PSX\",\"A mutual fund investing in government securities\",\"A fixed-term bank deposit\"]",
                correctOptionIndex = 1,
                explanation = "Single Stock Futures are leveraged derivative contracts traded on PSX linked to underlying equities."
            ),
            QuizEntity(
                quizId = "quiz_4_2",
                courseId = "derivatives_futures_401",
                question = "What happens during a daily Mark-to-Market (MTM) settlement in futures trading?",
                optionsJson = "[\"Positions are automatically closed out every morning\",\"Profits and losses on open futures contracts are settled daily in cash/margin accounts\",\"Dividends are paid out to futures holders\",\"Stock prices are frozen for the next trading day\"]",
                correctOptionIndex = 1,
                explanation = "MTM settles gains and losses daily, adjusting margin accounts to reflect current market prices."
            )
        )
        courseDao.insertQuizzes(quizzes4)
    }
}
