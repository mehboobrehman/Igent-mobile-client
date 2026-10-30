package com.igent.mobile.di

import com.igent.mobile.data.remote.config.ConfigApiService
import com.igent.mobile.data.remote.telemetry.TelemetryInterceptor
import com.igent.mobile.data.preferences.PreferencesDataSource
import com.igent.mobile.data.remote.AuthInterceptor
import com.igent.mobile.data.remote.SessionManager
import com.igent.mobile.data.remote.ai.GroqApiService
import com.igent.mobile.data.remote.ai.OpenRouterApiService
import com.igent.mobile.data.remote.ai.ZenApiService
import com.igent.mobile.data.remote.rss.RssApiService
import com.igent.mobile.data.remote.rss.RssParser
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
annotation class OpenRouterRetrofit

@Qualifier
annotation class GroqRetrofit

@Qualifier
annotation class ZenRetrofit

@Qualifier
annotation class RssRetrofit

@Qualifier
annotation class PsxPrimaryRetrofit

@Qualifier
annotation class PsxFallbackRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        telemetryInterceptor: TelemetryInterceptor
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(telemetryInterceptor)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    @OpenRouterRetrofit
    fun provideOpenRouterRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://openrouter.ai/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    @GroqRetrofit
    fun provideGroqRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://api.groq.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    @ZenRetrofit
    fun provideZenRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://api.zenai.network/") // Standard OpenAI-compatible Zen AI endpoint
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    @RssRetrofit
    fun provideRssRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://www.brecorder.com/")
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    @PsxPrimaryRetrofit
    fun providePsxPrimaryRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://psxlens.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    @PsxFallbackRetrofit
    fun providePsxFallbackRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://api.psxdata.pk/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenRouterApiService(
        @OpenRouterRetrofit retrofit: Retrofit
    ): OpenRouterApiService {
        return retrofit.create(OpenRouterApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideGroqApiService(
        @GroqRetrofit retrofit: Retrofit
    ): GroqApiService {
        return retrofit.create(GroqApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideZenApiService(
        @ZenRetrofit retrofit: Retrofit
    ): ZenApiService {
        return retrofit.create(ZenApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideRssApiService(
        @RssRetrofit retrofit: Retrofit
    ): RssApiService {
        return retrofit.create(RssApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePsxApiService(
        @PsxPrimaryRetrofit retrofit: Retrofit
    ): com.igent.mobile.data.remote.psx.PsxApiService {
        return retrofit.create(com.igent.mobile.data.remote.psx.PsxApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePsxDataApiFallbackService(
        @PsxFallbackRetrofit retrofit: Retrofit
    ): com.igent.mobile.data.remote.psx.PsxDataApiFallbackService {
        return retrofit.create(com.igent.mobile.data.remote.psx.PsxDataApiFallbackService::class.java)
    }

    @Provides
    @Singleton
    fun provideRssParser(): RssParser {
        return RssParser()
    }

    @Provides
    @Singleton
    fun provideConfigApiService(@PsxFallbackRetrofit retrofit: Retrofit): ConfigApiService {
        return retrofit.create(ConfigApiService::class.java)
    }
}
