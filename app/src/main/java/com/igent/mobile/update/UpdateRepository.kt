package com.igent.mobile.update

import android.content.Context
import kotlinx.coroutines.flow.Flow
import java.io.File

interface UpdateRepository {
    suspend fun checkForUpdate(): DownloadState
    fun downloadUpdate(release: GitHubReleaseDto, asset: GitHubAssetDto): Flow<DownloadState>
    fun launchInstaller(context: Context, apkFile: File)
    suspend fun dismissUpdate(version: String)
}
