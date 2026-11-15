package com.igent.mobile.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igent.mobile.data.preferences.PreferencesDataSource
import com.igent.mobile.data.util.KeyObfuscator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val openRouterKey: String = "",
    val groqKey: String = "",
    val googleUserEmail: String = "",
    val googleAuthToken: String = "",
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val hasEnteredApiKey: Boolean = false,
    val isLoggedIn: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val preferencesDataSource: PreferencesDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        loadStoredCredentials()
    }

    private fun loadStoredCredentials() {
        viewModelScope.launch {
            try {
                val rawApiKey = preferencesDataSource.apiKey.first()
                val deobfuscatedApiKey = KeyObfuscator.deobfuscate(rawApiKey)
                
                val email = preferencesDataSource.googleUserEmail.first()
                val token = preferencesDataSource.googleAuthToken.first()
                val hasEnteredKey = preferencesDataSource.hasEnteredApiKey.first()
                val placeholder = preferencesDataSource.placeholderCredentials.first()

                val loggedIn = email.isNotBlank() || deobfuscatedApiKey.isNotBlank() || placeholder.isNotBlank()

                _uiState.update {
                    it.copy(
                        openRouterKey = deobfuscatedApiKey,
                        groqKey = deobfuscatedApiKey,
                        googleUserEmail = email,
                        googleAuthToken = token,
                        hasEnteredApiKey = hasEnteredKey,
                        isLoggedIn = loggedIn
                    )
                }
            } catch (e: Exception) {
                // Ignore load error
            }
        }
    }

    fun updateOpenRouterKey(key: String) {
        _uiState.update { it.copy(openRouterKey = key, errorMessage = null, successMessage = null) }
    }

    fun updateGroqKey(key: String) {
        _uiState.update { it.copy(groqKey = key, errorMessage = null, successMessage = null) }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            try {
                kotlinx.coroutines.delay(800) // Simulate OAuth handshake
                val mockEmail = "user.igent@gmail.com"
                val mockToken = "oauth2_google_token_${System.currentTimeMillis()}"

                preferencesDataSource.saveGoogleUserSession(mockEmail, mockToken)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        googleUserEmail = mockEmail,
                        googleAuthToken = mockToken,
                        isLoggedIn = true,
                        successMessage = "Signed in with Google successfully!"
                    )
                }
            } catch (e: java.io.IOException) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Network failure during Google sign-in. Please check your connection.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Google sign-in failed: ${e.localizedMessage}") }
            }
        }
    }

    fun saveApiKeys() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            try {
                val keyToSave = _uiState.value.openRouterKey.ifBlank { _uiState.value.groqKey }
                if (keyToSave.isBlank()) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Please enter at least one valid API Key (OpenRouter or Groq).") }
                    return@launch
                }

                if (keyToSave.length < 10) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "API Key appears too short or invalid.") }
                    return@launch
                }

                preferencesDataSource.saveApiKey(keyToSave)
                preferencesDataSource.saveHasEnteredApiKey(true)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        hasEnteredApiKey = true,
                        isLoggedIn = true,
                        successMessage = "API Keys saved and obfuscated securely!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to save API keys: ${e.localizedMessage}") }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                preferencesDataSource.saveApiKey("")
                preferencesDataSource.saveGoogleUserSession("", "")
                preferencesDataSource.savePlaceholderCredentials("")
                preferencesDataSource.saveHasEnteredApiKey(false)
                _uiState.update {
                    AuthUiState()
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
