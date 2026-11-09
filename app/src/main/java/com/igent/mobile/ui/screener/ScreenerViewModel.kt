package com.igent.mobile.ui.screener

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igent.mobile.domain.model.Stock
import com.igent.mobile.domain.usecase.RunScreenerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScreenerUiState(
    val isLoading: Boolean = false,
    val stocks: List<Stock> = emptyList(),
    val maxPe: Double = 20.0,
    val minGain: Double = 0.0,
    val errorMessage: String? = null
)

@HiltViewModel
class ScreenerViewModel @Inject constructor(
    private val runScreenerUseCase: RunScreenerUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScreenerUiState())
    val uiState: StateFlow<ScreenerUiState> = _uiState.asStateFlow()

    init {
        runScreening()
    }

    fun onMaxPeChanged(pe: Double) {
        _uiState.update { it.copy(maxPe = pe) }
    }

    fun onMinGainChanged(gain: Double) {
        _uiState.update { it.copy(minGain = gain) }
    }

    fun runScreening() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = runScreenerUseCase(_uiState.value.maxPe, _uiState.value.minGain)
            result.fold(
                onSuccess = { stocks ->
                    val filtered = stocks.filter { it.percentChange >= _uiState.value.minGain }
                    _uiState.update { it.copy(isLoading = false, stocks = filtered) }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = err.localizedMessage ?: "Screener failed.") }
                }
            )
        }
    }
}
