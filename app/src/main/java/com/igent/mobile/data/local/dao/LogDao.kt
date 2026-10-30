package com.igent.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.igent.mobile.data.local.entity.LogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Insert
    suspend fun insert(log: LogEntity)

    @Query("SELECT * FROM agent_logs ORDER BY timestamp DESC LIMIT 50")
    fun getLast50Logs(): Flow<List<LogEntity>>

    @Query("SELECT * FROM agent_logs ORDER BY timestamp DESC LIMIT 200")
    fun getLast200Logs(): Flow<List<LogEntity>>

    @Query("DELETE FROM agent_logs")
    suspend fun clearAll()

    @Query("UPDATE agent_logs SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)
}
