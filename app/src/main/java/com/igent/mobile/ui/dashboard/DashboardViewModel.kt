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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = false,
    val stocks: List<Stock> = emptyList(),
    val holdings: List<PortfolioHolding> = emptyList(),
    val feedItems: List<FeedItem> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getStocksUseCase: GetStocksUseCase,
    private val getPortfolioUseCase: GetPortfolioUseCase,
    private val getFeedItemsUseCase: GetFeedItemsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val stocksResult = getStocksUseCase()
            val feedResult = getFeedItemsUseCase()

            val stocks = stocksResult.getOrDefault(emptyList())
            val feeds = feedResult.getOrDefault(emptyList())

            _uiState.update {
                it.copy(
                    isLoading = false,
                    stocks = stocks,
                    feedItems = feeds.take(5)
                )
            }
        }

        viewModelScope.launch {
            getPortfolioUseCase().collect { holdingsList ->
                _uiState.update { it.copy(holdings = holdingsList) }
            }
        }
    }
}
