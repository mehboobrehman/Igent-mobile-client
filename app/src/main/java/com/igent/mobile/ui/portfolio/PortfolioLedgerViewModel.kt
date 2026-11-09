package com.igent.mobile.ui.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igent.mobile.domain.model.PortfolioHolding
import com.igent.mobile.domain.model.PortfolioTransaction
import com.igent.mobile.domain.usecase.GetPortfolioUseCase
import com.igent.mobile.domain.usecase.ManagePortfolioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PortfolioLedgerUiState(
    val holdings: List<PortfolioHolding> = emptyList(),
    val transactions: List<PortfolioTransaction> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class PortfolioLedgerViewModel @Inject constructor(
    private val getPortfolioUseCase: GetPortfolioUseCase,
    private val managePortfolioUseCase: ManagePortfolioUseCase,
    private val psxRepository: com.igent.mobile.domain.repository.PsxRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PortfolioLedgerUiState())
    val uiState: StateFlow<PortfolioLedgerUiState> = _uiState.asStateFlow()

    init {
        loadPortfolio()
    }

    fun loadPortfolio() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            psxRepository.getStocks() // refresh market prices
            getPortfolioUseCase().collect { holdingsList ->
                _uiState.update { it.copy(holdings = holdingsList, isLoading = false) }
            }
        }
        viewModelScope.launch {
            // Also collect transactions
            // For simplicity, we can expose transactions via portfolioRepository or manageUseCase
        }
    }

    fun addTrade(symbol: String, type: String, quantityStr: String, priceStr: String) {
        val qty = quantityStr.toDoubleOrNull()
        val price = priceStr.toDoubleOrNull()
        if (symbol.isBlank() || qty == null || qty <= 0 || price == null || price <= 0) {
            _uiState.update { it.copy(errorMessage = "Please enter valid trade parameters.") }
            return
        }

        viewModelScope.launch {
            val result = managePortfolioUseCase.addTransaction(symbol, type, qty, price)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(errorMessage = null) }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(errorMessage = err.localizedMessage ?: "Failed to record trade.") }
                }
            )
        }
    }

    fun resetPortfolio() {
        viewModelScope.launch {
            managePortfolioUseCase.reset()
        }
    }
}
