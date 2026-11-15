package com.igent.mobile.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igent.mobile.domain.model.FeedItem
import com.igent.mobile.domain.model.PortfolioHolding
import com.igent.mobile.domain.model.Stock
import com.igent.mobile.domain.usecase.GetFeedItemsUseCase
import com.igent.mobile.domain.usecase.GetPortfolioUseCase
import com.igent.mobile.domain.usecase.GetStocksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = false,
    val stocks: List<Stock> = emptyList(),
    val holdings: List<PortfolioHolding> = emptyList(),
    val feedItems: List<FeedItem> = emptyList(),
    val recentIdea: com.igent.mobile.data.local.entity.IdeaCardEntity? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getStocksUseCase: GetStocksUseCase,
    private val getPortfolioUseCase: GetPortfolioUseCase,
    private val getFeedItemsUseCase: GetFeedItemsUseCase,
    private val ideaCardRepository: com.igent.mobile.domain.repository.IdeaCardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                val stocksResult = try { getStocksUseCase() } catch (e: Exception) { Result.success(emptyList()) }
                val feedResult = try { getFeedItemsUseCase() } catch (e: Exception) { Result.success(emptyList()) }

                val stocks = stocksResult.getOrDefault(emptyList())
                val feeds = feedResult.getOrDefault(emptyList())

                try {
                    ideaCardRepository.initializeDefaultIdeaCards()
                    val cards = ideaCardRepository.generateDailyIdeaCards()
                    _uiState.update { it.copy(recentIdea = cards.firstOrNull()) }
                } catch (e: Exception) {
                    // ignore
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        stocks = stocks,
                        feedItems = feeds.take(5)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Failed to load dashboard data"
                    )
                }
            }
        }

        viewModelScope.launch {
            try {
                getPortfolioUseCase()
                    .catch { emit(emptyList()) }
                    .collect { holdingsList ->
                        _uiState.update { it.copy(holdings = holdingsList) }
                    }
            } catch (e: Exception) {
                // Handle portfolio collection exception gracefully
            }
        }
    }
}
