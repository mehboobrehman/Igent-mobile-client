package com.igent.mobile.ui.idea

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igent.mobile.data.local.dao.IdeaCardDao
import com.igent.mobile.data.local.dao.WeeklySummaryDao
import com.igent.mobile.data.local.entity.IdeaCardEntity
import com.igent.mobile.data.local.entity.WeeklySummaryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IdeaUiState(
    val cards: List<IdeaCardEntity> = emptyList(),
    val summaries: List<WeeklySummaryEntity> = emptyList()
)

@HiltViewModel
class IdeaCardsViewModel @Inject constructor(
    private val ideaCardDao: IdeaCardDao,
    private val weeklySummaryDao: WeeklySummaryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(IdeaUiState())
    val uiState: StateFlow<IdeaUiState> = _uiState

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            ideaCardDao.getAllIdeaCards().collect { cards ->
                _uiState.value = _uiState.value.copy(cards = cards)
            }
        }
        viewModelScope.launch {
            weeklySummaryDao.getAllWeeklySummaries().collect { summaries ->
                _uiState.value = _uiState.value.copy(summaries = summaries)
            }
        }
    }
}
