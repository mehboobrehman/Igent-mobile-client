package com.igent.mobile.ui.psx

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igent.mobile.data.repository.PsxRepositoryImpl
import com.igent.mobile.domain.model.Stock
import com.igent.mobile.domain.usecase.GetStocksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PsxUiState(
    val stocks: List<Stock> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class PsxViewModel @Inject constructor(
    private val getStocksUseCase: GetStocksUseCase,
    private val psxRepository: PsxRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(PsxUiState())
    val uiState: StateFlow<PsxUiState> = _uiState.asStateFlow()

    init {
        loadStocks(initial = true)
        observeStockTicker()
    }

    private fun observeStockTicker() {
        viewModelScope.launch {
            psxRepository.getStockTickerFlow().collect { cachedStocks ->
                if (cachedStocks.isNotEmpty()) {
                    _uiState.update { it.copy(stocks = cachedStocks) }
                }
            }
        }
    }

    fun loadStocks(initial: Boolean = false, refreshing: Boolean = false) {
        viewModelScope.launch {
            if (initial) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            } else if (refreshing) {
                _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            }

            val result = getStocksUseCase()
            result.fold(
                onSuccess = { fetchedStocks ->
                    _uiState.update {
                        it.copy(
                            stocks = if (fetchedStocks.isNotEmpty()) fetchedStocks else it.stocks,
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = error.message ?: "Failed to load PSX stocks"
                        )
                    }
                }
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun refresh() {
        loadStocks(initial = false, refreshing = true)
    }
}
