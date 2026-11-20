package com.igent.mobile.update

import android.content.Context
import com.igent.mobile.util.AgentLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.whenever
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

@ExperimentalCoroutinesApi
class UpdateRepositoryImplTest {

    @Mock
    private lateinit var gitHubReleaseApi: GitHubReleaseApi
    @Mock
    private lateinit var updatePreferences: UpdatePreferences
    @Mock
    private lateinit var context: Context
    @Mock
    private lateinit var logger: AgentLogger

    private lateinit var updateRepository: UpdateRepositoryImpl

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        updateRepository = createRepository(false)
    }

    private fun createRepository(isDebugBuild: Boolean): UpdateRepositoryImpl {
        return UpdateRepositoryImpl(
            gitHubReleaseApi = gitHubReleaseApi,
            updatePreferences = updatePreferences,
            context = context,
            logger = logger,
            isDebugBuild = isDebugBuild
        )
    }

    private fun createRelease(
        tagName: String,
        assets: List<GitHubAssetDto> = emptyList(),
        prerelease: Boolean = false,
        draft: Boolean = false
    ): GitHubReleaseDto {
        return GitHubReleaseDto(
            tagName = tagName,
            name = "Release $tagName",
            body = "Release notes for $tagName",
            assets = assets,
            prerelease = prerelease,
            draft = draft
        )
    }

    private fun createAsset(name: String, size: Long = 1024L, url: String = "http://example.com/$name"): GitHubAssetDto {
        return GitHubAssetDto(name = name, size = size, browserDownloadUrl = url)
    }

    @Test
    fun `checkForUpdate should find Igent-v1_1_8-release APK asset when available`() = runTest {
        val releaseVersion = "1.1.8"
        val apkName = "Igent-v$releaseVersion-release.apk"
        val release = createRelease(
            tagName = releaseVersion,
            assets = listOf(createAsset(apkName))
        )

        whenever(gitHubReleaseApi.getLatestRelease()).thenReturn(release)

        val result = updateRepository.checkForUpdate()

        assert(result is DownloadState.Available)
        val availableState = result as DownloadState.Available
        assert(availableState.asset.name == apkName)
    }

    @Test
    fun `checkForUpdate should find app-release APK asset when available`() = runTest {
        val releaseVersion = "1.1.5"
        val apkName = "app-release.apk"
        val release = createRelease(
            tagName = releaseVersion,
            assets = listOf(createAsset(apkName))
        )

        whenever(gitHubReleaseApi.getLatestRelease()).thenReturn(release)

        val result = updateRepository.checkForUpdate()

        assert(result is DownloadState.Available)
        val availableState = result as DownloadState.Available
        assert(availableState.release.tagName == releaseVersion)
        assert(availableState.asset.name == apkName)
    }

    @Test
    fun `checkForUpdate should find Igent-vX-release APK asset when available`() = runTest {
        val releaseVersion = "1.1.6"
        val apkName = "Igent-v$releaseVersion-release.apk"
        val release = createRelease(
            tagName = releaseVersion,
            assets = listOf(createAsset(apkName))
        )

        whenever(gitHubReleaseApi.getLatestRelease()).thenReturn(release)

        val result = updateRepository.checkForUpdate()

        assert(result is DownloadState.Available)
        val availableState = result as DownloadState.Available
        assert(availableState.asset.name == apkName)
    }

    @Test
    fun `checkForUpdate should return Error when production APK asset is not found`() = runTest {
        val releaseVersion = "1.1.7"
        val release = createRelease(
            tagName = releaseVersion,
            assets = listOf(createAsset("some-other-asset.zip"))
        )

        whenever(gitHubReleaseApi.getLatestRelease()).thenReturn(release)

        val result = updateRepository.checkForUpdate()

        assert(result is DownloadState.Error)
        val errorState = result as DownloadState.Error
        assert(errorState.message.contains("Production APK asset not found for the latest release."))
    }

    @Test
    fun `checkForUpdate should return Error on network failure`() = runTest {
        doAnswer { throw IOException("Simulated network error") }.whenever(gitHubReleaseApi).getLatestRelease()

        val result = updateRepository.checkForUpdate()

        assert(result is DownloadState.Error)
        val errorState = result as DownloadState.Error
        assert(errorState.message.contains("Network error: Please check your internet connection."))
    }

    @Test
    fun `checkForUpdate should return Error for rate limit exceeded`() = runTest {
        val httpException = HttpException(
            Response.error<Any>(429, "".toResponseBody("text/plain".toMediaTypeOrNull()))
        )
        doAnswer { throw httpException }.whenever(gitHubReleaseApi).getLatestRelease()

        val result = updateRepository.checkForUpdate()

        assert(result is DownloadState.Error)
        val errorState = result as DownloadState.Error
        assert(errorState.message.contains("GitHub API rate limit exceeded. Please try again later."))
    }

    @Test
    fun `checkForUpdate should return Error for invalid release tag format`() = runTest {
        val invalidTagName = "not-a-valid-version"
        val release = createRelease(tagName = invalidTagName)
        whenever(gitHubReleaseApi.getLatestRelease()).thenReturn(release)

        val result = updateRepository.checkForUpdate()

        assert(result is DownloadState.Error)
        val errorState = result as DownloadState.Error
        assert(errorState.message.contains("Invalid release tag format: $invalidTagName"))
    }

    @Test
    fun `checkForUpdate should ignore draft or prerelease`() = runTest {
        val releaseVersion = "1.2.0-beta.1"
        val release = createRelease(
            tagName = releaseVersion,
            assets = listOf(createAsset("Igent-release-v$releaseVersion.apk")),
            prerelease = true
        )
        whenever(gitHubReleaseApi.getLatestRelease()).thenReturn(release)

        val repo = createRepository(isDebugBuild = false)
        val result = repo.checkForUpdate()

        assert(result is DownloadState.Idle)
    }

    @Test
    fun `checkForUpdate debug build should find debug APK asset`() = runTest {
        val releaseVersion = "1.1.8"
        val apkName = "Igent-v$releaseVersion-debug.apk"
        val release = createRelease(
            tagName = releaseVersion,
            assets = listOf(createAsset(apkName))
        )

        whenever(gitHubReleaseApi.getLatestRelease()).thenReturn(release)

        val repo = createRepository(isDebugBuild = true)
        val result = repo.checkForUpdate()

        assert(result is DownloadState.Available)
        val availableState = result as DownloadState.Available
        assert(availableState.asset.name == apkName)
    }

    @Test
    fun `checkForUpdate debug build should reject release APK asset with cross-variant error`() = runTest {
        val releaseVersion = "1.1.8"
        val apkName = "Igent-v$releaseVersion-release.apk"
        val release = createRelease(
            tagName = releaseVersion,
            assets = listOf(createAsset(apkName))
        )

        whenever(gitHubReleaseApi.getLatestRelease()).thenReturn(release)

        val repo = createRepository(isDebugBuild = true)
        val result = repo.checkForUpdate()

        assert(result is DownloadState.Error)
        val errorState = result as DownloadState.Error
        assert(errorState.message.contains("Cross-variant update denied: cannot update debug build with release APK."))
    }

    @Test
    fun `checkForUpdate release build should find release APK asset`() = runTest {
        val releaseVersion = "1.1.8"
        val apkName = "Igent-v$releaseVersion-release.apk"
        val release = createRelease(
            tagName = releaseVersion,
            assets = listOf(createAsset(apkName))
        )

        whenever(gitHubReleaseApi.getLatestRelease()).thenReturn(release)

        val repo = createRepository(isDebugBuild = false)
        val result = repo.checkForUpdate()

        assert(result is DownloadState.Available)
        val availableState = result as DownloadState.Available
        assert(availableState.asset.name == apkName)
    }

    @Test
    fun `checkForUpdate release build should reject debug APK asset with cross-variant error`() = runTest {
        val releaseVersion = "1.1.8"
        val apkName = "Igent-v$releaseVersion-debug.apk"
        val release = createRelease(
            tagName = releaseVersion,
            assets = listOf(createAsset(apkName))
        )

        whenever(gitHubReleaseApi.getLatestRelease()).thenReturn(release)

        val repo = createRepository(isDebugBuild = false)
        val result = repo.checkForUpdate()

        assert(result is DownloadState.Error)
        val errorState = result as DownloadState.Error
        assert(errorState.message.contains("Cross-variant update denied: cannot update release build with debug APK."))
    }
}
