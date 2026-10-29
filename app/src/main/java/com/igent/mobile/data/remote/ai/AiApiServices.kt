package com.igent.mobile.data.remote.ai

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Streaming

interface OpenRouterApiService {
    @Headers("Content-Type: application/json")
    @POST("api/v1/chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Header("HTTP-Referer") referer: String = "https://igent.mobile",
        @Header("X-Title") title: String = "Igent Mobile",
        @Body request: ChatRequest
    ): ChatResponse

    @Streaming
    @Headers("Content-Type: application/json")
    @POST("api/v1/chat/completions")
    suspend fun createChatCompletionStream(
        @Header("Authorization") authorization: String,
        @Header("HTTP-Referer") referer: String = "https://igent.mobile",
        @Header("X-Title") title: String = "Igent Mobile",
        @Body request: ChatRequest
    ): Response<ResponseBody>
}

interface GroqApiService {
    @Headers("Content-Type: application/json")
    @POST("openai/v1/chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: ChatRequest
    ): ChatResponse

    @Streaming
    @Headers("Content-Type: application/json")
    @POST("openai/v1/chat/completions")
    suspend fun createChatCompletionStream(
        @Header("Authorization") authorization: String,
        @Body request: ChatRequest
    ): Response<ResponseBody>
}

interface ZenApiService {
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: ChatRequest
    ): ChatResponse

    @Streaming
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    suspend fun createChatCompletionStream(
        @Header("Authorization") authorization: String,
        @Body request: ChatRequest
    ): Response<ResponseBody>
}
