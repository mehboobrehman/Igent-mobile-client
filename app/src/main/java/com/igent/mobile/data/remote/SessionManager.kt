package com.igent.mobile.data.remote

import android.content.Context
import com.igent.mobile.data.preferences.PreferencesDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

sealed class SessionEvent {
    object Unauthorized : SessionEvent()
}

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesDataSource: PreferencesDataSource
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _sessionEvents = MutableSharedFlow<SessionEvent>()
    val sessionEvents: SharedFlow<SessionEvent> = _sessionEvents.asSharedFlow()

    fun handleUnauthorized() {
        scope.launch {
            try {
                // Clear invalid credentials / token
                preferencesDataSource.saveApiKey("")
                preferencesDataSource.savePlaceholderCredentials("")
                _sessionEvents.emit(SessionEvent.Unauthorized)
            } catch (e: Exception) {
                // Log or handle error
            }
        }
    }

    suspend fun validateToken(): Boolean {
        return try {
            val key = preferencesDataSource.apiKey.first()
            key.isNotBlank()
        } catch (e: Exception) {
            false
        }
    }
}
