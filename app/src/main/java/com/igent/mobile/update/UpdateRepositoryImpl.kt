package com.igent.mobile.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.FileProvider
import com.igent.mobile.BuildConfig
import com.igent.mobile.util.AgentLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRepositoryImpl @Inject constructor(
    private val gitHubReleaseApi: GitHubReleaseApi,
    private val updatePreferences: UpdatePreferences,
    private val context: Context,
    private val logger: AgentLogger,
    private val isDebugBuild: Boolean = BuildConfig.DEBUG
) : UpdateRepository {

    constructor(
        gitHubReleaseApi: GitHubReleaseApi,
        updatePreferences: UpdatePreferences,
        context: Context
    ) : this(gitHubReleaseApi, updatePreferences, context, AgentLogger(), BuildConfig.DEBUG)

    constructor(
        gitHubReleaseApi: GitHubReleaseApi,
        updatePreferences: UpdatePreferences,
        context: Context,
        logger: AgentLogger
    ) : this(gitHubReleaseApi, updatePreferences, context, logger, BuildConfig.DEBUG)

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    override suspend fun checkForUpdate(): DownloadState {
        val targetVariant = if (isDebugBuild) "debug" else "release"
        logger.i("UpdateRepo", "Checking for updates. Build variant: ${if (isDebugBuild) "DEBUG" else "RELEASE"}, target artifact type: $targetVariant")

        return try {
            val release = gitHubReleaseApi.getLatestRelease()
            logger.d("UpdateRepo", "Latest release fetched: ${release.tagName}")
            logger.d("UpdateRepo", "Available assets in release ${release.tagName}: ${release.assets.map { it.name }}")

            if (release.draft || release.prerelease) {
                logger.i("UpdateRepo", "Skipping draft or pre-release: ${release.tagName}")
                return DownloadState.Idle // Skip if draft or pre-release
            }

            val releaseSemVer = SemVer.parse(release.tagName)
                ?: run {
                    logger.e("UpdateRepo", "Invalid release tag format: ${release.tagName}")
                    return DownloadState.Error("Invalid release tag format: ${release.tagName}")
                }

            val installedSemVer = SemVer.parse(BuildConfig.VERSION_NAME)
                ?: run {
                    logger.w("UpdateRepo", "Could not parse installed version name: ${BuildConfig.VERSION_NAME}. Assuming 1.0.0.")
                    SemVer(1, 0, 0) // Default to 1.0.0 if parsing fails
                }

            logger.d("UpdateRepo", "Installed version: $installedSemVer, Release version: $releaseSemVer")

            if (releaseSemVer <= installedSemVer) {
                logger.i("UpdateRepo", "No new update available. Installed version ($installedSemVer) is up to date.")
                return DownloadState.Idle
            }

            val apkAssets = release.assets.filter { it.name.endsWith(".apk", ignoreCase = true) }

            if (apkAssets.isEmpty()) {
                val errorMsg = if (isDebugBuild) "Debug APK asset not found for the latest release." else "Production APK asset not found for the latest release."
                logger.e("UpdateRepo", "No APK assets found for release ${release.tagName}. Available assets: ${release.assets.map { it.name }}")
                return DownloadState.Error(errorMsg)
            }

            val matchingAsset = if (isDebugBuild) {
                val debugAsset = apkAssets.find { it.name.contains("debug", ignoreCase = true) }
                val releaseAsset = apkAssets.find { 
                    it.name.contains("release", ignoreCase = true) || 
                    (!it.name.contains("debug", ignoreCase = true) && (it.name.startsWith("Igent", ignoreCase = true) || it.name.startsWith("app", ignoreCase = true))) 
                }

                if (debugAsset != null) {
                    debugAsset
                } else if (releaseAsset != null) {
                    logger.e("UpdateRepo", "Cross-variant update attempt: debug build cannot update with release APK (${releaseAsset.name})")
                    return DownloadState.Error("Cross-variant update denied: cannot update debug build with release APK.")
                } else {
                    apkAssets.find { it.name.contains("debug", ignoreCase = true) }
                }
            } else {
                val releaseAsset = apkAssets.find { 
                    val name = it.name
                    !name.contains("debug", ignoreCase = true) && 
                    (name.contains("release", ignoreCase = true) || name.startsWith("Igent", ignoreCase = true) || name.startsWith("app", ignoreCase = true))
                }
                val debugAsset = apkAssets.find { it.name.contains("debug", ignoreCase = true) }

                if (releaseAsset != null) {
                    releaseAsset
                } else if (debugAsset != null) {
                    logger.e("UpdateRepo", "Cross-variant update attempt: release build cannot update with debug APK (${debugAsset.name})")
                    return DownloadState.Error("Cross-variant update denied: cannot update release build with debug APK.")
                } else {
                    null
                }
            }

            if (matchingAsset == null) {
                val errorMsg = if (isDebugBuild) "Debug APK asset not found for the latest release." else "Production APK asset not found for the latest release."
                logger.e("UpdateRepo", errorMsg)
                return DownloadState.Error(errorMsg)
            }

            logger.d("UpdateRepo", "Found matching APK asset: ${matchingAsset.name} for release ${release.tagName} (variant: $targetVariant)")
            DownloadState.Available(release, matchingAsset, releaseSemVer)

        } catch (e: java.io.IOException) {
            logger.e("UpdateRepo", "Network error while checking for updates", e)
            DownloadState.Error("Network error: Please check your internet connection.")
        } catch (e: retrofit2.HttpException) {
            val errorCode = e.code()
            logger.e("UpdateRepo", "HTTP error while checking for updates: $errorCode", e)
            when (errorCode) {
                403, 429 -> DownloadState.Error("GitHub API rate limit exceeded. Please try again later.")
                else -> DownloadState.Error("Server error (${errorCode}): ${e.message()}")
            }
        } catch (e: Exception) {
            logger.e("UpdateRepo", "An unexpected error occurred while checking for updates", e)
            DownloadState.Error("Failed to check for updates: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    override fun downloadUpdate(release: GitHubReleaseDto, asset: GitHubAssetDto): Flow<DownloadState> = flow {
        emit(DownloadState.Downloading(0f, 0L, asset.size))

        val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
        val apkFile = File(updatesDir, asset.name)
        val tempFile = File(updatesDir, "${asset.name}.tmp")

        try {
            // 1. Find and download SHA256SUMS asset
            val sha256Asset = release.assets.find { it.name.equals("SHA256SUMS", ignoreCase = true) || it.name.equals("sha256sums.txt", ignoreCase = true) }
            val expectedChecksum = if (sha256Asset != null) {
                fetchChecksum(sha256Asset.browserDownloadUrl, asset.name)
            } else {
                null
            }

            // 2. Download APK file with progress streaming
            val request = Request.Builder().url(asset.browserDownloadUrl).build()
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                throw java.io.IOException("Failed to download APK: HTTP ${response.code}")
            }

            val body = response.body ?: throw java.io.IOException("Empty response body")
            val contentLength = body.contentLength().takeIf { it > 0 } ?: asset.size

            tempFile.outputStream().use { output ->
                body.byteStream().use { input ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesDownloaded = 0L
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        bytesDownloaded += read
                        val progress = if (contentLength > 0) bytesDownloaded.toFloat() / contentLength else 0f
                        emit(DownloadState.Downloading(progress, bytesDownloaded, contentLength))
                    }
                    output.flush()
                }
            }

            emit(DownloadState.Verifying)

            // 3. Verify SHA-256 checksum if available
            if (!expectedChecksum.isNullOrBlank()) {
                val actualChecksum = calculateSha256(tempFile)
                if (!actualChecksum.equals(expectedChecksum, ignoreCase = true)) {
                    throw SecurityException("SHA-256 checksum verification failed! Expected $expectedChecksum, got $actualChecksum")
                }
            }

            // 4. Verify APK signing certificate matches installed release certificate
            if (!verifyApkSignature(context, tempFile)) {
                throw SecurityException("APK signing certificate does not match installed application release certificate!")
            }

            // 5. Move temp file to final destination
            if (apkFile.exists()) apkFile.delete()
            if (!tempFile.renameTo(apkFile)) {
                throw java.io.IOException("Failed to finalize downloaded APK file.")
            }

            emit(DownloadState.ReadyToInstall(apkFile, release))

        } catch (e: Exception) {
            if (tempFile.exists()) tempFile.delete()
            if (apkFile.exists()) apkFile.delete()
            emit(DownloadState.Error(e.localizedMessage ?: "Update download or verification failed."))
        }
    }.flowOn(Dispatchers.IO)

    private fun fetchChecksum(url: String, targetFilename: String): String? {
        return try {
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) return null
            val content = response.body?.string() ?: return null
            for (line in content.lines()) {
                val parts = line.trim().split(Regex("\\s+"))
                if (parts.size >= 2 && parts[1].equals(targetFilename, ignoreCase = true)) {
                    return parts[0]
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8 * 1024)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    @Suppress("DEPRECATION")
    private fun verifyApkSignature(context: Context, apkFile: File): Boolean {
        val pm = context.packageManager
        val installedInfo = try {
            pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
        } catch (e: Exception) {
            try {
                pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            } catch (e2: Exception) {
                null
            }
        }

        val apkInfo = try {
            pm.getPackageArchiveInfo(apkFile.absolutePath, PackageManager.GET_SIGNING_CERTIFICATES)
        } catch (e: Exception) {
            try {
                pm.getPackageArchiveInfo(apkFile.absolutePath, PackageManager.GET_SIGNATURES)
            } catch (e2: Exception) {
                null
            }
        }

        if (installedInfo == null || apkInfo == null) return false

        val installedSigs = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            installedInfo.signingInfo?.let { signingInfo ->
                if (signingInfo.hasMultipleSigners()) signingInfo.apkContentsSigners else signingInfo.signingCertificateHistory
            }
        } else {
            installedInfo.signatures
        }

        val apkSigs = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            apkInfo.signingInfo?.let { signingInfo ->
                if (signingInfo.hasMultipleSigners()) signingInfo.apkContentsSigners else signingInfo.signingCertificateHistory
            }
        } else {
            apkInfo.signatures
        }

        if (installedSigs.isNullOrEmpty() || apkSigs.isNullOrEmpty()) return false

        val md = MessageDigest.getInstance("SHA-256")
        val installedDigests = installedSigs.map { md.digest(it.toByteArray()) }
        val apkDigests = apkSigs.map { md.digest(it.toByteArray()) }

        return installedDigests.any { installed -> apkDigests.any { apk -> installed.contentEquals(apk) } }
    }

    override fun launchInstaller(context: Context, apkFile: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apkFile)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    override suspend fun dismissUpdate(version: String) {
        updatePreferences.saveDismissedVersion(version)
    }
}
