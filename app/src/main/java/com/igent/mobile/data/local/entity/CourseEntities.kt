package com.igent.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey
    val courseId: String,
    val title: String,
    val description: String,
    val category: String, // e.g. "PSX Basics", "Technical Analysis", "Fundamental Investing", "Options & Futures"
    val difficulty: String, // "Beginner", "Intermediate", "Advanced"
    val estimatedMinutes: Int,
    val progressPercent: Int = 0,
    val isCompleted: Boolean = false
)

@Entity(tableName = "course_modules")
data class CourseModuleEntity(
    @PrimaryKey
    val moduleId: String,
    val courseId: String,
    val title: String,
    val content: String,
    val orderIndex: Int,
    val isCompleted: Boolean = false
)

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey
    val quizId: String,
    val courseId: String,
    val question: String,
    val optionsJson: String, // JSON array of 4 option strings
    val correctOptionIndex: Int,
    val explanation: String
)

@Entity(tableName = "quiz_scores")
data class QuizScoreEntity(
    @PrimaryKey
    val quizId: String,
    val courseId: String,
    val selectedOptionIndex: Int,
    val isCorrect: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
