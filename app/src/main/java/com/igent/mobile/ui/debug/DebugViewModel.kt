package com.igent.mobile.ui.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igent.mobile.data.local.dao.LogDao
import com.igent.mobile.data.local.entity.LogEntity
import com.igent.mobile.data.remote.config.ConfigRepository
import com.igent.mobile.data.remote.config.RemoteConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebugViewModel @Inject constructor(
    private val logDao: LogDao,
    private val configRepository: ConfigRepository
) : ViewModel() {

    val logs: StateFlow<List<LogEntity>> = logDao.getLast50Logs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val config: StateFlow<RemoteConfig> = configRepository.config

    fun clearLogs() {
        viewModelScope.launch {
            logDao.clearAll()
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            configRepository.fetchConfig()
            // In a real app, this might also trigger log sync to remote server
        }
    }
}
