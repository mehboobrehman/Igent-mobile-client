package com.igent.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sensor_runs")
data class SensorRunEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val sourceId: String,
    val timestamp: Long,
    val status: String, // SUCCESS, FAILED, NOT_MODIFIED
    val etag: String? = null,
    val lastModified: String? = null,
    val warnings: String? = null,
    val recordsCount: Int = 0,
    val errorMessage: String? = null
)
