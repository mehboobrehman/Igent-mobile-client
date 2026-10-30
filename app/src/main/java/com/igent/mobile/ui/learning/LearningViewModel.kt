package com.igent.mobile.ui.learning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igent.mobile.data.local.entity.CourseEntity
import com.igent.mobile.data.local.entity.CourseModuleEntity
import com.igent.mobile.data.local.entity.QuizEntity
import com.igent.mobile.data.local.entity.QuizScoreEntity
import com.igent.mobile.domain.repository.LearningRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LearningUiState(
    val courses: List<CourseEntity> = emptyList(),
    val selectedCourse: CourseEntity? = null,
    val modules: List<CourseModuleEntity> = emptyList(),
    val quizzes: List<QuizEntity> = emptyList(),
    val quizScores: List<QuizScoreEntity> = emptyList(),
    val currentQuizIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val quizFeedback: QuizFeedback? = null,
    val isLoading: Boolean = false
)

data class QuizFeedback(
    val isCorrect: Boolean,
    val explanation: String
)

@HiltViewModel
class LearningViewModel @Inject constructor(
    private val learningRepository: LearningRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LearningUiState())
    val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()

    init {
        loadCourses()
        loadScores()
    }

    private fun loadCourses() {
        viewModelScope.launch {
            learningRepository.getAllCourses().collect { courses ->
                if (courses.isEmpty()) {
                    learningRepository.initializeDefaultCourses()
                } else {
                    _uiState.update { it.copy(courses = courses) }
                }
            }
        }
    }

    private fun loadScores() {
        viewModelScope.launch {
            learningRepository.getAllQuizScores().collect { scores ->
                _uiState.update { it.copy(quizScores = scores) }
            }
        }
    }

    fun selectCourse(course: CourseEntity) {
        _uiState.update { 
            it.copy(
                selectedCourse = course,
                currentQuizIndex = 0,
                selectedOptionIndex = null,
                quizFeedback = null
            )
        }
        viewModelScope.launch {
            learningRepository.getModulesForCourse(course.courseId).collect { modules ->
                _uiState.update { it.copy(modules = modules) }
            }
        }
        viewModelScope.launch {
            learningRepository.getQuizzesForCourse(course.courseId).collect { quizzes ->
                _uiState.update { it.copy(quizzes = quizzes) }
            }
        }
    }

    fun clearSelectedCourse() {
        _uiState.update { 
            it.copy(
                selectedCourse = null,
                modules = emptyList(),
                quizzes = emptyList(),
                selectedOptionIndex = null,
                quizFeedback = null
            )
        }
    }

    fun markModuleCompleted(moduleId: String) {
        viewModelScope.launch {
            learningRepository.markModuleCompleted(moduleId)
        }
    }

    fun selectQuizOption(index: Int) {
        _uiState.update { it.copy(selectedOptionIndex = index) }
    }

    fun submitQuizAnswer() {
        val course = _uiState.value.selectedCourse ?: return
        val quizzes = _uiState.value.quizzes
        val currentIndex = _uiState.value.currentQuizIndex
        val selectedIndex = _uiState.value.selectedOptionIndex ?: return

        if (currentIndex < quizzes.size) {
            val currentQuiz = quizzes[currentIndex]
            viewModelScope.launch {
                val isCorrect = learningRepository.submitQuizAnswer(
                    quizId = currentQuiz.quizId,
                    courseId = course.courseId,
                    selectedOptionIndex = selectedIndex
                )

                _uiState.update {
                    it.copy(
                        quizFeedback = QuizFeedback(
                            isCorrect = isCorrect,
                            explanation = currentQuiz.explanation
                        )
                    )
                }
            }
        }
    }

    fun nextQuiz() {
        _uiState.update {
            it.copy(
                currentQuizIndex = (it.currentQuizIndex + 1).coerceAtMost((it.quizzes.size - 1).coerceAtLeast(0)),
                selectedOptionIndex = null,
                quizFeedback = null
            )
        }
    }
}
