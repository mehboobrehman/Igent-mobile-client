package com.igent.mobile.update

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubReleaseDto(
    @SerialName("tag_name") val tagName: String,
    @SerialName("name") val name: String? = null,
    @SerialName("body") val body: String? = null,
    @SerialName("assets") val assets: List<GitHubAssetDto> = emptyList(),
    @SerialName("prerelease") val prerelease: Boolean = false,
    @SerialName("draft") val draft: Boolean = false,
    @SerialName("html_url") val htmlUrl: String? = null
)

@Serializable
data class GitHubAssetDto(
    @SerialName("name") val name: String,
    @SerialName("size") val size: Long = 0L,
    @SerialName("browser_download_url") val browserDownloadUrl: String
)
