package com.igent.mobile.update

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val updateRepository: UpdateRepository,
    private val updatePreferences: UpdatePreferences
) : ViewModel() {

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    private val _showUpdateDialog = MutableStateFlow(false)
    val showUpdateDialog: StateFlow<Boolean> = _showUpdateDialog.asStateFlow()

    init {
        checkUpdatesOnStartup()
    }

    fun checkUpdatesOnStartup() {
        viewModelScope.launch {
            val autoCheck = updatePreferences.autoCheckEnabled.first()
            if (!autoCheck) return@launch
            checkForUpdates(isManual = false)
        }
    }

    fun checkForUpdates(isManual: Boolean = true) {
        viewModelScope.launch {
            _downloadState.value = DownloadState.Checking
            try {
                val state = updateRepository.checkForUpdate()
                _downloadState.value = state
                if (state is DownloadState.Available) {
                    val dismissed = updatePreferences.dismissedVersion.first()
                    if (isManual || dismissed != state.release.tagName) {
                        _showUpdateDialog.value = true
                    }
                }
            } catch (e: Exception) {
                _downloadState.value = DownloadState.Error(e.localizedMessage ?: "Failed to check for updates")
            }
        }
    }

    fun startDownload(release: GitHubReleaseDto, asset: GitHubAssetDto) {
        viewModelScope.launch {
            updateRepository.downloadUpdate(release, asset).collect { state ->
                _downloadState.value = state
            }
        }
    }

    fun installUpdate(context: Context, apkFile: File) {
        updateRepository.launchInstaller(context, apkFile)
    }

    fun dismissUpdate(version: String) {
        viewModelScope.launch {
            updatePreferences.saveDismissedVersion(version)
            _showUpdateDialog.value = false
        }
    }

    fun hideDialog() {
        _showUpdateDialog.value = false
    }
}
