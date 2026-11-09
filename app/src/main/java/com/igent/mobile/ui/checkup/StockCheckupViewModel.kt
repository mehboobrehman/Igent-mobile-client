package com.igent.mobile.ui.checkup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igent.mobile.domain.model.CheckupResult
import com.igent.mobile.domain.usecase.RunStockCheckupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StockCheckupUiState(
    val symbolQuery: String = "",
    val targetPriceQuery: String = "",
    val isLoading: Boolean = false,
    val result: CheckupResult? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class StockCheckupViewModel @Inject constructor(
    private val runStockCheckupUseCase: RunStockCheckupUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockCheckupUiState())
    val uiState: StateFlow<StockCheckupUiState> = _uiState.asStateFlow()

    fun onSymbolChanged(symbol: String) {
        _uiState.update { it.copy(symbolQuery = symbol) }
    }

    fun onTargetPriceChanged(target: String) {
        _uiState.update { it.copy(targetPriceQuery = target) }
    }

    fun runCheckup(symbolOverride: String? = null) {
        val symbol = symbolOverride ?: _uiState.value.symbolQuery
        if (symbol.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid stock symbol (e.g. LUCK, ENGRO)") }
            return
        }
        if (symbolOverride != null) {
            _uiState.update { it.copy(symbolQuery = symbolOverride) }
        }

        val targetPrice = _uiState.value.targetPriceQuery.toDoubleOrNull()

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, result = null) }
            val result = runStockCheckupUseCase(symbol.trim(), targetPrice)
            result.fold(
                onSuccess = { checkupResult ->
                    _uiState.update { it.copy(isLoading = false, result = checkupResult) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.localizedMessage ?: "Stock checkup failed.") }
                }
            )
        }
    }
}
