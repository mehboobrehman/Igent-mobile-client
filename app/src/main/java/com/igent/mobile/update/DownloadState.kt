package com.igent.mobile.update

import java.io.File

sealed interface DownloadState {
    data object Idle : DownloadState
    data object Checking : DownloadState
    data class Available(val release: GitHubReleaseDto, val asset: GitHubAssetDto, val version: SemVer) : DownloadState
    data class Downloading(val progress: Float, val bytesDownloaded: Long, val totalBytes: Long) : DownloadState
    data object Verifying : DownloadState
    data class ReadyToInstall(val apkFile: File, val release: GitHubReleaseDto) : DownloadState
    data class Error(val message: String) : DownloadState
}
