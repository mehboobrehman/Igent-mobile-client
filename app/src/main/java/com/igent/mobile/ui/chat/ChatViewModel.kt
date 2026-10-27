package com.igent.mobile.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igent.mobile.domain.model.Message
import com.igent.mobile.domain.usecase.GetAiHistoryUseCase
import com.igent.mobile.domain.usecase.SendAiMessageUseCase
import com.igent.mobile.domain.repository.AiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val inputPrompt: String = "",
    val isLoading: Boolean = false,
    val streamingText: String = "",
    val errorMessage: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getAiHistoryUseCase: GetAiHistoryUseCase,
    private val sendAiMessageUseCase: SendAiMessageUseCase,
    private val aiRepository: AiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            getAiHistoryUseCase().collect { history ->
                _uiState.update { it.copy(messages = history) }
            }
        }
    }

    fun onInputChanged(prompt: String) {
        _uiState.update { it.copy(inputPrompt = prompt) }
    }

    fun sendMessage() {
        val prompt = _uiState.value.inputPrompt.trim()
        if (prompt.isBlank() || _uiState.value.isLoading) return

        _uiState.update { it.copy(inputPrompt = "", isLoading = true, streamingText = "", errorMessage = null) }

        viewModelScope.launch {
            try {
                var collectedText = ""
                aiRepository.sendMessageStream(prompt).catch { _ ->
                    // fallback handled below if collectedText remains blank
                }.collect { chunk ->
                    if (chunk.isNotBlank()) {
                        collectedText += chunk
                        _uiState.update { it.copy(streamingText = collectedText) }
                    }
                }

                if (collectedText.isBlank()) {
                    // Streaming failed or returned blank, smoothly fallback to non-streaming use case
                    val result = sendAiMessageUseCase(prompt)
                    if (result.isFailure) {
                        _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.message ?: "Failed to send message") }
                    }
                } else {
                    // Streaming succeeded, save the interaction to local database
                    aiRepository.saveAiInteraction(prompt, collectedText)
                }
            } catch (e: Exception) {
                // Global fallback to standard sendMessage use case
                val result = sendAiMessageUseCase(prompt)
                if (result.isFailure) {
                    _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.message ?: "An error occurred") }
                }
            } finally {
                _uiState.update { it.copy(isLoading = false, streamingText = "") }
            }
        }
    }
}
