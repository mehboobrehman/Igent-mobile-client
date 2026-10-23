package com.igent.mobile.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore(name = "igent_preferences")

@Singleton
class PreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val API_KEY = stringPreferencesKey("api_key")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val PLACEHOLDER_CREDENTIALS = stringPreferencesKey("placeholder_credentials")
    }

    val apiKey: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.API_KEY] ?: ""
        }

    val themeMode: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.THEME_MODE] ?: "system"
        }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
        }

    val placeholderCredentials: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.PLACEHOLDER_CREDENTIALS] ?: ""
        }

    suspend fun saveApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.API_KEY] = key
        }
    }

    suspend fun saveThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    suspend fun saveNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun savePlaceholderCredentials(credentials: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PLACEHOLDER_CREDENTIALS] = credentials
        }
    }
}
