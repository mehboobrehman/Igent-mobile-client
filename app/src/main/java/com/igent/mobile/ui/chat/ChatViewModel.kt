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
            // First try streaming if supported / available, or fallback to regular send message use case
            try {
                var collectedText = ""
                aiRepository.sendMessageStream(prompt).catch { e ->
                    // fallback or error in stream
                }.collect { chunk ->
                    collectedText += chunk
                    _uiState.update { it.copy(streamingText = collectedText) }
                }

                // If streaming didn't produce content or we want to ensure robust storage via use case
                if (collectedText.isBlank()) {
                    val result = sendAiMessageUseCase(prompt)
                    if (result.isFailure) {
                        _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.message ?: "Failed to send message") }
                    }
                } else {
                    // Save AI response message to db via repository/use case if not already saved by repo
                    // Actually AiRepositoryImpl sendMessage already saves to DB. Let's make sure sendMessage is invoked or simulated if streaming was raw.
                    // To keep it super clean and robust: let's invoke sendAiMessageUseCase which handles DB storage & API fallback robustly.
                    sendAiMessageUseCase(prompt)
                }
            } catch (e: Exception) {
                // Fallback to standard sendMessage use case
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
