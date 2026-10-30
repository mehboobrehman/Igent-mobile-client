package com.igent.mobile.data.remote.config

import retrofit2.http.GET

interface ConfigApiService {
    @GET("api/config")
    suspend fun getConfig(): RemoteConfig
}
