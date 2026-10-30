package com.igent.mobile.domain.repository

import com.igent.mobile.data.local.entity.CourseEntity
import com.igent.mobile.data.local.entity.CourseModuleEntity
import com.igent.mobile.data.local.entity.QuizEntity
import com.igent.mobile.data.local.entity.QuizScoreEntity
import kotlinx.coroutines.flow.Flow

interface LearningRepository {
    fun getAllCourses(): Flow<List<CourseEntity>>
    suspend fun getCourseById(courseId: String): CourseEntity?
    fun getModulesForCourse(courseId: String): Flow<List<CourseModuleEntity>>
    fun getQuizzesForCourse(courseId: String): Flow<List<QuizEntity>>
    fun getAllQuizScores(): Flow<List<QuizScoreEntity>>
    suspend fun markModuleCompleted(moduleId: String)
    suspend fun submitQuizAnswer(quizId: String, courseId: String, selectedOptionIndex: Int): Boolean
    suspend fun initializeDefaultCourses()
}
