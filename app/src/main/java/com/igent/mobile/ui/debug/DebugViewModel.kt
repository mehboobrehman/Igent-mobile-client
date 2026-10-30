package com.igent.mobile.ui.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igent.mobile.data.local.dao.LogDao
import com.igent.mobile.data.local.entity.LogEntity
import com.igent.mobile.data.remote.config.ConfigRepository
import com.igent.mobile.data.remote.config.RemoteConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NetworkStats(
    val totalRequests: Int = 0,
    val failedRequests: Int = 0,
    val averageLatencyMs: Long = 0L
)

@HiltViewModel
class DebugViewModel @Inject constructor(
    private val logDao: LogDao,
    private val configRepository: ConfigRepository
) : ViewModel() {

    private val _selectedLevel = MutableStateFlow<String>("ALL")
    val selectedLevel: StateFlow<String> = _selectedLevel.asStateFlow()

    private val allLogs: StateFlow<List<LogEntity>> = logDao.getLast200Logs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val logs: StateFlow<List<LogEntity>> = combine(allLogs, _selectedLevel) { logs, level ->
        if (level == "ALL") {
            logs
        } else {
            logs.filter { it.level.equals(level, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val config: StateFlow<RemoteConfig> = configRepository.config

    val networkStats: StateFlow<NetworkStats> = allLogs.map { logList ->
        val networkLogs = logList.filter { it.tag == "NetworkTelemetry" }
        val requests = networkLogs.filter { it.message.startsWith("<--") }
        val total = requests.size
        val failed = requests.count { it.message.contains("[ClientError]") || it.message.contains("[ServerError]") || it.level == "ERROR" }
        
        var totalLatency = 0L
        var latencyCount = 0
        for (log in requests) {
            val match = Regex("Latency: (\\d+)ms").find(log.message)
            if (match != null) {
                val lat = match.groupValues[1].toLongOrNull() ?: 0L
                totalLatency += lat
                latencyCount++
            }
        }
        val avgLatency = if (latencyCount > 0) totalLatency / latencyCount else 0L
        NetworkStats(totalRequests = total, failedRequests = failed, averageLatencyMs = avgLatency)
    }.stateIn(viewModelScope, SharingStarted.Lazily, NetworkStats())

    fun setLevelFilter(level: String) {
        _selectedLevel.value = level
    }

    fun clearLogs() {
        viewModelScope.launch {
            logDao.clearAll()
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            configRepository.fetchConfig()
        }
    }

    fun exportLogsText(): String {
        val currentLogs = logs.value
        val sb = StringBuilder()
        sb.append("=== IGENT MOBILE TELEMETRY EXPORT ===\n")
        sb.append("Generated at: ${java.util.Date()}\n\n")
        for (log in currentLogs) {
            sb.append("[${log.level}] [${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date(log.timestamp))}] [${log.tag}]: ${log.message}\n")
        }
        return sb.toString()
    }
}
