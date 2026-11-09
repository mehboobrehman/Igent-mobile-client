package com.igent.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.igent.mobile.data.local.entity.SensorRunEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorRunDao {
    @Query("SELECT * FROM sensor_runs ORDER BY timestamp DESC")
    fun getAllSensorRuns(): Flow<List<SensorRunEntity>>

    @Query("SELECT * FROM sensor_runs WHERE sourceId = :sourceId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestRunForSource(sourceId: String): SensorRunEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: SensorRunEntity)

    @Query("DELETE FROM sensor_runs WHERE timestamp < :cutoffTime")
    suspend fun pruneOldRuns(cutoffTime: Long)
}
