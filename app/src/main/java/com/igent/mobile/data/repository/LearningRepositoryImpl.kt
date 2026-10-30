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
                description = "Master the structure, trading mechanisms, KSE-100 index, and regulatory bodies of the Pakistan Stock Exchange.",
                category = "PSX Basics",
                difficulty = "Beginner",
                estimatedMinutes = 20,
                progressPercent = 0
            ),
            CourseEntity(
                courseId = "tech_analysis_201",
                title = "Technical Analysis & Chart Patterns",
                description = "Learn how to read candlestick charts, RSI, MACD, support/resistance levels, and volume indicators for profitable swing trading.",
                category = "Technical Analysis",
                difficulty = "Intermediate",
                estimatedMinutes = 35,
                progressPercent = 0
            ),
            CourseEntity(
                courseId = "dividend_investing_301",
                title = "Dividend Yield & Value Investing",
                description = "Discover high-dividend blue-chip stocks in Pakistan (Energy, Banks, Fertilizers) and calculate dividend payout ratios.",
                category = "Fundamental Investing",
                difficulty = "Advanced",
                estimatedMinutes = 30,
                progressPercent = 0
            )
        )
        courseDao.insertCourses(defaultCourses)

        // Modules for Course 1
        val modules1 = listOf(
            CourseModuleEntity(
                moduleId = "mod_1_1",
                courseId = "psx_basics_101",
                title = "Introduction to Pakistan Stock Exchange (PSX)",
                content = "The Pakistan Stock Exchange (PSX) was formed in 2016 by the integration of Karachi, Lahore, and Islamabad Stock Exchanges. It is regulated by the Securities and Exchange Commission of Pakistan (SECP). KSE-100 is the benchmark index representing the top 100 companies by market capitalization.",
                orderIndex = 1
            ),
            CourseModuleEntity(
                moduleId = "mod_1_2",
                courseId = "psx_basics_101",
                title = "Trading Hours & Settlement (T+1 / T+2)",
                content = "PSX trading sessions run from Monday to Thursday (9:30 AM to 3:30 PM) and Friday (9:00 AM to 4:30 PM with a break). Transactions are settled through the Central Depository Company (CDC) of Pakistan under electronic book-entry systems.",
                orderIndex = 2
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
            )
        )
        courseDao.insertQuizzes(quizzes1)
    }
}
