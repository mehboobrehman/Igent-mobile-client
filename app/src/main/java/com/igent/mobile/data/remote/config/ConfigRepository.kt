package com.igent.mobile.data.remote.config

import com.igent.mobile.util.AgentLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigRepository @Inject constructor(
    private val apiService: ConfigApiService,
    private val logger: AgentLogger
) {
    private val _config = MutableStateFlow(RemoteConfig())
    val config: StateFlow<RemoteConfig> = _config

    suspend fun fetchConfig() {
        try {
            val remoteConfig = apiService.getConfig()
            _config.value = remoteConfig
            logger.i("Config", "Remote config fetched successfully")
        } catch (e: Exception) {
            logger.e("Config", "Failed to fetch remote config, using defaults", e)
            // Fallback to default or simulated values if needed
        }
    }
}
