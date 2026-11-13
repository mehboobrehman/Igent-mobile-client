package com.igent.mobile.update

import retrofit2.http.GET
import retrofit2.http.Headers

interface GitHubReleaseApi {
    @Headers(
        "Accept: application/vnd.github+json",
        "X-GitHub-Api-Version: 2022-11-28",
        "User-Agent: Igent-Android-App"
    )
    @GET("repos/mehboobrehman/Igent-mobile-client/releases/latest")
    suspend fun getLatestRelease(): GitHubReleaseDto
}
