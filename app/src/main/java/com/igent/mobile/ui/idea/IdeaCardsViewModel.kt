package com.igent.mobile.ui.idea

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igent.mobile.data.local.entity.IdeaCardEntity
import com.igent.mobile.data.local.entity.WeeklySummaryEntity
import com.igent.mobile.domain.repository.IdeaCardRepository
import com.igent.mobile.domain.repository.WeeklySummaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IdeaUiState(
    val cards: List<IdeaCardEntity> = emptyList(),
    val summaries: List<WeeklySummaryEntity> = emptyList(),
    val isLoading: Boolean = false,
    val selectedTab: Int = 0 // 0 = Daily Agent Ideas, 1 = Weekly Summaries
)

@HiltViewModel
class IdeaCardsViewModel @Inject constructor(
    private val ideaCardRepository: IdeaCardRepository,
    private val weeklySummaryRepository: WeeklySummaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(IdeaUiState())
    val uiState: StateFlow<IdeaUiState> = _uiState.asStateFlow()

    init {
        loadData()
        ensureDataGenerated()
    }

    private fun loadData() {
        viewModelScope.launch {
            ideaCardRepository.getAllIdeaCards().collect { cards ->
                _uiState.update { it.copy(cards = cards) }
            }
        }
        viewModelScope.launch {
            weeklySummaryRepository.getAllWeeklySummaries().collect { summaries ->
                _uiState.update { it.copy(summaries = summaries) }
            }
        }
    }

    private fun ensureDataGenerated() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                ideaCardRepository.initializeDefaultIdeaCards()
                weeklySummaryRepository.generateWeeklySummary()
            } catch (e: Exception) {
                // Ignore fallback
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                ideaCardRepository.generateDailyIdeaCards()
                weeklySummaryRepository.generateWeeklySummary()
            } catch (e: Exception) {
                // Log/handle
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
