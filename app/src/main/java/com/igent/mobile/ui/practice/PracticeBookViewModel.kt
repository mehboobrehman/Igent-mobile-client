package com.igent.mobile.ui.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igent.mobile.domain.engine.PracticeBook
import com.igent.mobile.domain.engine.PracticePosition
import com.igent.mobile.domain.engine.PracticeTradeInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class PracticeBookUiState(
    val cash: Double = PracticeBook.PRACTICE_START,
    val positions: List<PracticePosition> = emptyList(),
    val trades: List<PracticeTradeInput> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class PracticeBookViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(PracticeBookUiState())
    val uiState: StateFlow<PracticeBookUiState> = _uiState.asStateFlow()

    fun executeTrade(symbol: String, side: String, qtyStr: String, priceStr: String) {
        val qty = qtyStr.toDoubleOrNull()
        val price = priceStr.toDoubleOrNull()
        if (symbol.isBlank() || qty == null || qty <= 0 || price == null || price <= 0) {
            _uiState.update { it.copy(errorMessage = "Invalid practice trade parameters.") }
            return
        }

        val trade = PracticeTradeInput(
            kind = "PSX",
            symbol = symbol.uppercase(),
            name = symbol.uppercase(),
            side = side.lowercase(),
            qty = qty,
            price = price,
            fees = 10.0,
            at = java.time.Instant.now().toString()
        )

        val newTrades = _uiState.value.trades + trade
        val (cash, positions) = PracticeBook.book(newTrades)

        _uiState.update {
            it.copy(
                cash = cash,
                positions = positions,
                trades = newTrades,
                errorMessage = null
            )
        }
    }
}
