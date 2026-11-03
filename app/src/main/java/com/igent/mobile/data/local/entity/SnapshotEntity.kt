package com.igent.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "snapshots")
data class SnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val snapshotId: String,
    val sessionId: String,
    val deviceId: String,
    val triggerId: String,
    val triggerCategory: String,
    val timestamp: Long,
    val signature: String,
    val occurrenceCount: Int,
    val payloadJson: String,
    val isSynced: Boolean = false,
    val isCrashSnapshot: Boolean = false
)
