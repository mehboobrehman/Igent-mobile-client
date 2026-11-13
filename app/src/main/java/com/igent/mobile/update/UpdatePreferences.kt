package com.igent.mobile.update

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.updateDataStore by preferencesDataStore(name = "igent_update_preferences")

@Singleton
class UpdatePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val LAST_CHECK_TIMESTAMP = longPreferencesKey("last_check_timestamp")
        val DISMISSED_VERSION = stringPreferencesKey("dismissed_version")
        val AUTO_CHECK_ENABLED = booleanPreferencesKey("auto_check_enabled")
    }

    val lastCheckTimestamp: Flow<Long> = context.updateDataStore.data
        .map { prefs -> prefs[Keys.LAST_CHECK_TIMESTAMP] ?: 0L }

    val dismissedVersion: Flow<String> = context.updateDataStore.data
        .map { prefs -> prefs[Keys.DISMISSED_VERSION] ?: "" }

    val autoCheckEnabled: Flow<Boolean> = context.updateDataStore.data
        .map { prefs -> prefs[Keys.AUTO_CHECK_ENABLED] ?: true }

    suspend fun saveLastCheckTimestamp(timestamp: Long) {
        context.updateDataStore.edit { prefs ->
            prefs[Keys.LAST_CHECK_TIMESTAMP] = timestamp
        }
    }

    suspend fun saveDismissedVersion(version: String) {
        context.updateDataStore.edit { prefs ->
            prefs[Keys.DISMISSED_VERSION] = version
        }
    }

    suspend fun saveAutoCheckEnabled(enabled: Boolean) {
        context.updateDataStore.edit { prefs ->
            prefs[Keys.AUTO_CHECK_ENABLED] = enabled
        }
    }
}
