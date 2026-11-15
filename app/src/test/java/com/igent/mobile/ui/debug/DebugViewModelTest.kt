package com.igent.mobile.ui.debug

import com.igent.mobile.data.local.dao.LogDao
import com.igent.mobile.data.local.entity.LogEntity
import com.igent.mobile.data.preferences.PreferencesDataSource
import com.igent.mobile.data.remote.config.ConfigRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DebugViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testDebugViewModelNetworkStatsAndFiltering() = runTest {
        val fakeLogs = listOf(
            LogEntity(id = 1, timestamp = 1000L, level = "INFO", tag = "NetworkTelemetry", message = "<-- GET /api/test Latency: 120ms"),
            LogEntity(id = 2, timestamp = 2000L, level = "ERROR", tag = "NetworkTelemetry", message = "<-- POST /api/fail [ClientError] Latency: 300ms"),
            LogEntity(id = 3, timestamp = 3000L, level = "WARN", tag = "General", message = "Warning message")
        )

        val logDao = object : LogDao {
            override suspend fun insert(log: LogEntity) {}
            override fun getLast50Logs(): Flow<List<LogEntity>> = flowOf(fakeLogs)
            override fun getLast200Logs(): Flow<List<LogEntity>> = flowOf(fakeLogs)
            override suspend fun clearAll() {}
            override suspend fun markAsSynced(ids: List<Long>) {}
            override suspend fun getUnsyncedLogs(): List<LogEntity> = emptyList()
            override suspend fun getUnsyncedCount(): Int = 0
            override suspend fun deleteOldestLogs(limit: Int) {}
            override suspend fun deleteSyncedLogs() {}
        }

        val configRepo = ConfigRepository()
        val dummyPreferences = PreferencesDataSource()

        val viewModel = DebugViewModel(logDao, configRepo, dummyPreferences)
        testDispatcher.scheduler.advanceUntilIdle()

        val stats = viewModel.networkStats.value
        assertEquals(2, stats.totalRequests)
        assertEquals(1, stats.failedRequests)
        assertEquals(210L, stats.averageLatencyMs)

        viewModel.setLevelFilter("ERROR")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("ERROR", viewModel.selectedLevel.value)
        assertEquals(1, viewModel.logs.value.size)
        assertEquals("ERROR", viewModel.logs.value[0].level)
    }
}
