package com.igent.mobile.telemetry

import android.content.Context
import android.os.Build
import android.util.Log
import com.igent.mobile.data.local.dao.LogDao
import com.igent.mobile.data.local.entity.LogEntity
import com.igent.mobile.data.remote.telemetry.HandshakeRequestDto
import com.igent.mobile.data.remote.telemetry.TelemetryApiService
import com.igent.mobile.data.remote.telemetry.TelemetryEnvelopeDto
import com.igent.mobile.data.util.NetworkConnectivityObserver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelemetryAgentCore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logDao: LogDao,
    private val telemetryApiService: TelemetryApiService,
    private val connectivityObserver: NetworkConnectivityObserver,
    private val rateLimiter: TelemetryRateLimiter
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _connectionState = MutableStateFlow(TelemetryConnectionState.DISCONNECTED)
    val connectionState: StateFlow<TelemetryConnectionState> = _connectionState.asStateFlow()

    private val _droppedEventCounter = AtomicLong(0)
    val droppedEventCounter: StateFlow<Long> = flow {
        while (true) {
            emit(_droppedEventCounter.get())
            delay(1000L)
        }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), 0L)

    private val backoff = TelemetryBackoff()
    private val isRunning = AtomicBoolean(false)
    private val _clockOffsetMs = AtomicLong(0L)
    val clockOffsetMs: Long get() = _clockOffsetMs.get()

    private val sessionId = "session_" + UUID.randomUUID().toString()
    private val deviceId = "android_${Build.MODEL.replace(" ", "_")}_${UUID.randomUUID().toString().take(8)}"
    private val seqCounter = AtomicLong(0)
    private val maxBufferCapacity = 1000

    init {
        start()
    }

    fun start() {
        if (!isRunning.compareAndSet(false, true)) return

        scope.launch {
            connectivityObserver.isConnected.collect { isOnline ->
                if (isOnline) {
                    handleConnectionRestored()
                } else {
                    transitionState(TelemetryConnectionState.OFFLINE)
                }
            }
        }
    }

    suspend fun handleConnectionRestored() {
        transitionState(TelemetryConnectionState.CONNECTING)
        try {
            val handshakeSuccess = performHandshake()
            if (handshakeSuccess) {
                transitionState(TelemetryConnectionState.CONNECTED)
                backoff.reset()
                flushBufferedEvents()
            } else {
                transitionState(TelemetryConnectionState.DEGRADED)
                scheduleReconnect()
            }
        } catch (e: Exception) {
            logInternalError("Handshake or connect failed", e)
            transitionState(TelemetryConnectionState.DEGRADED)
            scheduleReconnect()
        }
    }

    suspend fun performHandshake(): Boolean {
        return try {
            val clientTimestamp = System.currentTimeMillis()
            val request = HandshakeRequestDto(
                deviceId = deviceId,
                deviceModel = Build.MODEL ?: "Unknown",
                osApiLevel = Build.VERSION.SDK_INT,
                appVersion = "1.0.19",
                versionCode = 19,
                capabilities = listOf("logs", "crashes", "anrs", "network", "performance", "batching"),
                clientTimestamp = clientTimestamp
            )

            val response = telemetryApiService.handshake(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                try {
                    val serverTimeMillis = Instant.parse(body.serverTimestamp).toEpochMilli()
                    val offset = serverTimeMillis - clientTimestamp
                    _clockOffsetMs.set(offset)
                } catch (e: Exception) {
                    // Fallback if timestamp parsing fails
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            logInternalError("Handshake exception", e)
            false
        }
    }

    suspend fun logEvent(level: String, message: String, tag: String, category: String = "logs") {
        try {
            if (!rateLimiter.shouldAllow(category)) {
                _droppedEventCounter.incrementAndGet()
                return
            }

            val unsyncedCount = logDao.getUnsyncedCount()
            if (unsyncedCount >= maxBufferCapacity) {
                val dropCount = (unsyncedCount - maxBufferCapacity) + 1
                logDao.deleteOldestLogs(dropCount)
                _droppedEventCounter.addAndGet(dropCount.toLong())
            }

            val entity = LogEntity(
                timestamp = System.currentTimeMillis() + clockOffsetMs,
                level = level,
                tag = tag,
                message = message,
                isSynced = false
            )
            logDao.insert(entity)

            if (_connectionState.value == TelemetryConnectionState.CONNECTED) {
                flushBufferedEvents()
            }
        } catch (e: Exception) {
            logInternalError("Failed to log event", e)
        }
    }

    suspend fun flushBufferedEvents() {
        try {
            val unsynced = logDao.getUnsyncedLogs()
            if (unsynced.isEmpty()) return

            val dtos = unsynced.map { log ->
                TelemetryEnvelopeDto.createLog(
                    sessionId = sessionId,
                    deviceId = deviceId,
                    seq = seqCounter.incrementAndGet(),
                    level = log.level,
                    message = log.message,
                    tag = log.tag
                )
            }

            val response = telemetryApiService.postLogBatch(dtos)
            if (response.isSuccessful) {
                val ids = unsynced.map { it.id }
                logDao.markAsSynced(ids)
                if (_connectionState.value == TelemetryConnectionState.DEGRADED) {
                    transitionState(TelemetryConnectionState.CONNECTED)
                }
            } else {
                transitionState(TelemetryConnectionState.DEGRADED)
                scheduleReconnect()
            }
        } catch (e: Exception) {
            logInternalError("Flush failed", e)
            transitionState(TelemetryConnectionState.DEGRADED)
            scheduleReconnect()
        }
    }

    private fun scheduleReconnect() {
        scope.launch {
            val delayMs = backoff.nextDelayMs()
            delay(delayMs)
            if (connectivityObserver.getCurrentConnectivityStatus()) {
                handleConnectionRestored()
            }
        }
    }

    private fun transitionState(newState: TelemetryConnectionState) {
        _connectionState.value = newState
    }

    private fun logInternalError(message: String, e: Exception) {
        Log.e("TelemetryAgentCore", "$message: ${e.localizedMessage}", e)
    }

    fun getSyncedCounter(): Long = seqCounter.get()
    fun getSessionId(): String = sessionId
    val droppedCountValue: Long get() = _droppedEventCounter.get()
}
