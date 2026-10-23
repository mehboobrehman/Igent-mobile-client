package com.igent.mobile.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igent.mobile.data.preferences.PreferencesDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationSettingsUiState(
    val pushAlertsEnabled: Boolean = true,
    val stockAlertsEnabled: Boolean = true,
    val rssAlertsEnabled: Boolean = true
)

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val preferencesDataSource: PreferencesDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationSettingsUiState())
    val uiState: StateFlow<NotificationSettingsUiState> = _uiState.asStateFlow()

    init {
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            combine(
                preferencesDataSource.notificationsEnabled,
                preferencesDataSource.stockAlertsEnabled,
                preferencesDataSource.rssAlertsEnabled
            ) { push, stock, rss ->
                NotificationSettingsUiState(
                    pushAlertsEnabled = push,
                    stockAlertsEnabled = stock,
                    rssAlertsEnabled = rss
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setPushAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataSource.saveNotificationsEnabled(enabled)
        }
    }

    fun setStockAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataSource.saveStockAlertsEnabled(enabled)
        }
    }

    fun setRssAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataSource.saveRssAlertsEnabled(enabled)
        }
    }
}
