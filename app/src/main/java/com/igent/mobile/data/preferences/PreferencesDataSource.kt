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
        val STOCK_ALERTS_ENABLED = booleanPreferencesKey("stock_alerts_enabled")
        val RSS_ALERTS_ENABLED = booleanPreferencesKey("rss_alerts_enabled")
        val PLACEHOLDER_CREDENTIALS = stringPreferencesKey("placeholder_credentials")
        val SELECTED_AI_PROVIDER = stringPreferencesKey("selected_ai_provider")
    }

    val apiKey: Flow<String> = context.dataStore.data
        .map { preferences ->
            val stored = preferences[PreferencesKeys.API_KEY]
            if (!stored.isNullOrBlank()) {
                stored
            } else {
                val defaultConfigKey = com.igent.mobile.BuildConfig.OPENROUTER_API_KEY_1
                if (!defaultConfigKey.isNullOrBlank()) {
                    com.igent.mobile.data.util.KeyObfuscator.obfuscate(defaultConfigKey)
                } else {
                    ""
                }
            }
        }

    val themeMode: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.THEME_MODE] ?: "system"
        }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
        }

    val stockAlertsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.STOCK_ALERTS_ENABLED] ?: true
        }

    val rssAlertsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.RSS_ALERTS_ENABLED] ?: true
        }

    val placeholderCredentials: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.PLACEHOLDER_CREDENTIALS] ?: ""
        }

    val selectedAiProvider: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SELECTED_AI_PROVIDER] ?: "openrouter"
        }

    suspend fun saveApiKey(key: String) {
        val obfuscated = com.igent.mobile.data.util.KeyObfuscator.obfuscate(key)
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.API_KEY] = obfuscated
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

    suspend fun saveStockAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.STOCK_ALERTS_ENABLED] = enabled
        }
    }

    suspend fun saveRssAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RSS_ALERTS_ENABLED] = enabled
        }
    }

    suspend fun savePlaceholderCredentials(credentials: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PLACEHOLDER_CREDENTIALS] = credentials
        }
    }

    suspend fun saveSelectedAiProvider(providerId: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_AI_PROVIDER] = providerId
        }
    }
}
