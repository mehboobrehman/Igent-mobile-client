package com.igent.mobile.ui.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igent.mobile.domain.model.FinancialAnalysisResult
import com.igent.mobile.domain.model.FinancialAnalysisType
import com.igent.mobile.domain.usecase.RunFinancialAnalysisUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FinancialAnalysisUiState(
    val selectedType: FinancialAnalysisType = FinancialAnalysisType.BUY_SELL,
    val targetQuery: String = "",
    val isLoading: Boolean = false,
    val result: FinancialAnalysisResult? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class FinancialAnalysisViewModel @Inject constructor(
    private val runFinancialAnalysisUseCase: RunFinancialAnalysisUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinancialAnalysisUiState())
    val uiState: StateFlow<FinancialAnalysisUiState> = _uiState.asStateFlow()

    fun onTypeChanged(type: FinancialAnalysisType) {
        _uiState.update { it.copy(selectedType = type, result = null, errorMessage = null) }
    }

    fun onTargetQueryChanged(query: String) {
        _uiState.update { it.copy(targetQuery = query) }
    }

    fun runAnalysis(prefilledTarget: String? = null) {
        val target = prefilledTarget ?: _uiState.value.targetQuery
        if (target.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a stock symbol, fund name, or entity.") }
            return
        }

        if (prefilledTarget != null) {
            _uiState.update { it.copy(targetQuery = prefilledTarget) }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, result = null) }
            val result = runFinancialAnalysisUseCase(_uiState.value.selectedType, target)
            result.fold(
                onSuccess = { analysisResult ->
                    _uiState.update { it.copy(isLoading = false, result = analysisResult) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.localizedMessage ?: "Analysis failed. Please check connection.") }
                }
            )
        }
    }
}
