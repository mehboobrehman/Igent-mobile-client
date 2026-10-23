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
    val placeholderEmail: String = "",
    val placeholderPassword: String = "",
    val isLoginMode: Boolean = true,
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val isApiKeyValid: Boolean = false,
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
                
                val rawCredentials = preferencesDataSource.placeholderCredentials.first()
                var email = ""
                var pass = ""
                if (rawCredentials.contains(":")) {
                    val parts = rawCredentials.split(":", limit = 2)
                    email = parts[0]
                    pass = parts[1]
                } else if (rawCredentials.isNotBlank()) {
                    email = rawCredentials
                }

                _uiState.update {
                    it.copy(
                        openRouterKey = deobfuscatedApiKey,
                        groqKey = deobfuscatedApiKey, // Can also support separate if needed, using primary api key for both or mapping
                        placeholderEmail = email,
                        placeholderPassword = pass,
                        isLoggedIn = deobfuscatedApiKey.isNotBlank() || email.isNotBlank()
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

    fun updatePlaceholderEmail(email: String) {
        _uiState.update { it.copy(placeholderEmail = email, errorMessage = null, successMessage = null) }
    }

    fun updatePlaceholderPassword(password: String) {
        _uiState.update { it.copy(placeholderPassword = password, errorMessage = null, successMessage = null) }
    }

    fun toggleAuthMode() {
        _uiState.update { it.copy(isLoginMode = !it.isLoginMode, errorMessage = null, successMessage = null) }
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

                // Validate key format roughly
                if (keyToSave.length < 10) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "API Key appears too short or invalid.") }
                    return@launch
                }

                preferencesDataSource.saveApiKey(keyToSave)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isApiKeyValid = true,
                        isLoggedIn = true,
                        successMessage = "API Keys saved and obfuscated securely!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to save API keys: ${e.localizedMessage}") }
            }
        }
    }

    fun loginPlaceholder() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val state = _uiState.value
            if (state.placeholderEmail.isBlank() || !state.placeholderEmail.contains("@")) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Please enter a valid email address.") }
                return@launch
            }
            if (state.placeholderPassword.length < 4) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Password must be at least 4 characters.") }
                return@launch
            }

            try {
                val credentialString = "${state.placeholderEmail}:${state.placeholderPassword}"
                preferencesDataSource.savePlaceholderCredentials(credentialString)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        successMessage = if (state.isLoginMode) "Logged in successfully!" else "Account created successfully!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Authentication failed: ${e.localizedMessage}") }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                preferencesDataSource.saveApiKey("")
                preferencesDataSource.savePlaceholderCredentials("")
                _uiState.update {
                    AuthUiState()
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
