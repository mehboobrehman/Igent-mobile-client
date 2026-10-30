package com.igent.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.igent.mobile.data.local.entity.CourseEntity
import com.igent.mobile.data.local.entity.CourseModuleEntity
import com.igent.mobile.data.local.entity.QuizEntity
import com.igent.mobile.data.local.entity.QuizScoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE courseId = :courseId LIMIT 1")
    suspend fun getCourseById(courseId: String): CourseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity)

    @Query("UPDATE courses SET progressPercent = :progress, isCompleted = :completed WHERE courseId = :courseId")
    suspend fun updateCourseProgress(courseId: String, progress: Int, completed: Boolean)

    @Query("SELECT * FROM course_modules WHERE courseId = :courseId ORDER BY orderIndex ASC")
    fun getModulesForCourse(courseId: String): Flow<List<CourseModuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModules(modules: List<CourseModuleEntity>)

    @Query("UPDATE course_modules SET isCompleted = 1 WHERE moduleId = :moduleId")
    suspend fun markModuleCompleted(moduleId: String)

    @Query("SELECT * FROM quizzes WHERE courseId = :courseId")
    fun getQuizzesForCourse(courseId: String): Flow<List<QuizEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizzes(quizzes: List<QuizEntity>)

    @Query("SELECT * FROM quiz_scores")
    fun getAllQuizScores(): Flow<List<QuizScoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizScore(score: QuizScoreEntity)
}
