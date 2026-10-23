package com.igent.mobile.ui.rss

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igent.mobile.domain.model.FeedItem
import com.igent.mobile.domain.usecase.GetFeedItemsUseCase
import com.igent.mobile.data.repository.RssRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RssUiState(
    val items: List<FeedItem> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class RssViewModel @Inject constructor(
    private val getFeedItemsUseCase: GetFeedItemsUseCase,
    private val rssRepository: RssRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(RssUiState())
    val uiState: StateFlow<RssUiState> = _uiState.asStateFlow()

    init {
        loadFeed(initial = true)
        observeCachedFeed()
    }

    private fun observeCachedFeed() {
        viewModelScope.launch {
            rssRepository.getFeedItemsFlow().collect { cachedItems ->
                if (cachedItems.isNotEmpty()) {
                    _uiState.update { it.copy(items = cachedItems) }
                }
            }
        }
    }

    fun loadFeed(initial: Boolean = false, refreshing: Boolean = false) {
        viewModelScope.launch {
            if (initial) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            } else if (refreshing) {
                _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            }

            val result = getFeedItemsUseCase()
            result.fold(
                onSuccess = { items ->
                    _uiState.update {
                        it.copy(
                            items = if (items.isNotEmpty()) items else it.items,
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
                            errorMessage = error.message ?: "Failed to load RSS feed"
                        )
                    }
                }
            )
        }
    }

    fun refresh() {
        loadFeed(initial = false, refreshing = true)
    }
}
