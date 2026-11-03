package com.igent.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.igent.mobile.data.local.entity.SnapshotEntity

@Dao
interface SnapshotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(snapshot: SnapshotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSync(snapshot: SnapshotEntity)

    @Query("SELECT * FROM snapshots WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsyncedSnapshots(): List<SnapshotEntity>

    @Query("SELECT * FROM snapshots ORDER BY timestamp DESC")
    suspend fun getAllSnapshots(): List<SnapshotEntity>

    @Query("UPDATE snapshots SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM snapshots WHERE isSynced = 0")
    suspend fun getUnsyncedCount(): Int

    @Query("DELETE FROM snapshots WHERE id IN (SELECT id FROM snapshots ORDER BY timestamp ASC LIMIT :count)")
    suspend fun deleteOldestSnapshots(count: Int)
}
