package com.igent.mobile.domain.engine

data class QuizQuestion(
    val id: String,
    val stem: String,
    val options: List<String>,
    val answer: Int,
    val explanation: String?,
    val lessonId: String? = null
)

data class QuizResult(
    val correct: Int,
    val total: Int
)

object QuizEngine {
    fun submitQuiz(questions: List<QuizQuestion>, answers: List<Int>): QuizResult {
        val total = questions.size
        val correct = questions.indices.count { i -> answers.getOrNull(i) == questions[i].answer }
        return QuizResult(correct, total)
    }
}
