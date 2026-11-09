package com.igent.mobile.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igent.mobile.domain.engine.QuizEngine
import com.igent.mobile.domain.engine.QuizQuestion
import com.igent.mobile.domain.engine.QuizResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class QuizUiState(
    val questions: List<QuizQuestion> = listOf(
        QuizQuestion("1", "What is the primary risk of investing in small-cap stocks with low free float?", listOf("Guaranteed dividends", "High price volatility and pump/dump vulnerability", "Low volatility", "Government backing"), 1, "Small-cap stocks with low free float can be easily manipulated by groups."),
        QuizQuestion("2", "What does a high P/E ratio typically indicate?", listOf("The stock is undervalued", "Investors are paying high multiples for projected growth", "The company has zero debt", "The stock is risk-free"), 1, "High P/E means investors expect high future earnings growth.")
    ),
    val currentIndex: Int = 0,
    val selectedAnswers: MutableMap<Int, Int> = mutableMapOf(),
    val isFinished: Boolean = false,
    val result: QuizResult? = null
)

@HiltViewModel
class QuizViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    fun selectOption(questionIndex: Int, optionIndex: Int) {
        val updatedAnswers = _uiState.value.selectedAnswers.toMutableMap()
        updatedAnswers[questionIndex] = optionIndex
        _uiState.update { it.copy(selectedAnswers = updatedAnswers) }
    }

    fun nextQuestion() {
        val state = _uiState.value
        if (state.currentIndex < state.questions.size - 1) {
            _uiState.update { it.copy(currentIndex = it.currentIndex + 1) }
        } else {
            // Submit quiz
            val answersList = state.questions.indices.map { state.selectedAnswers[it] ?: -1 }
            val res = QuizEngine.submitQuiz(state.questions, answersList)
            _uiState.update { it.copy(isFinished = true, result = res) }
        }
    }

    fun resetQuiz() {
        _uiState.update { QuizUiState() }
    }
}
