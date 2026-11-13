package com.igent.mobile.ui.update

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.igent.mobile.update.DownloadState
import com.igent.mobile.update.UpdateViewModel

@Composable
fun UpdateSection(
    viewModel: UpdateViewModel = hiltViewModel()
) {
    val downloadState by viewModel.downloadState.collectAsState()
    val showDialog by viewModel.showUpdateDialog.collectAsState()
    val context = LocalContext.current

    Card(
        onClick = { viewModel.checkForUpdates(isManual = true) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.SystemUpdate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = "Check for Updates",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = when (downloadState) {
                            is DownloadState.Checking -> "Checking for updates..."
                            is DownloadState.Available -> "New version available!"
                            is DownloadState.Error -> "Check failed (tap to retry)"
                            else -> "Tap to check latest release"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showDialog && downloadState is DownloadState.Available) {
        val available = downloadState as DownloadState.Available
        AlertDialog(
            onDismissRequest = { viewModel.hideDialog() },
            title = { Text("Update v${available.version} Available") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(available.release.name ?: "A new version of Igent is available.")
                    if (!available.release.body.isNullOrBlank()) {
                        Text(
                            text = available.release.body,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 6
                        )
                    }
                    if (downloadState is DownloadState.Downloading) {
                        val state = downloadState as DownloadState.Downloading
                        LinearProgressIndicator(
                            progress = { state.progress },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Downloading: ${(state.progress * 100).toInt()}%")
                    } else if (downloadState is DownloadState.Verifying) {
                        Text("Verifying SHA-256 and signature...", color = MaterialTheme.colorScheme.primary)
                    } else if (downloadState is DownloadState.ReadyToInstall) {
                        val state = downloadState as DownloadState.ReadyToInstall
                        Text("Update ready to install!", color = MaterialTheme.colorScheme.primary)
                        Button(
                            onClick = { viewModel.installUpdate(context, state.apkFile) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Install Now")
                        }
                    } else if (downloadState is DownloadState.Error) {
                        Text("Error: ${(downloadState as DownloadState.Error).message}", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                if (downloadState !is DownloadState.ReadyToInstall && downloadState !is DownloadState.Downloading) {
                    TextButton(
                        onClick = { viewModel.startDownload(available.release, available.asset) }
                    ) {
                        Text("Download")
                    }
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { viewModel.dismissUpdate(available.release.tagName) }) {
                        Text("Dismiss")
                    }
                    TextButton(onClick = { viewModel.hideDialog() }) {
                        Text("Later")
                    }
                }
            }
        )
    }
}
